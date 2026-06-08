package com.pomanagement.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pomanagement.domain.repository.UserRepository;
import com.pomanagement.web.dto.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@Component
public class SessionAuthFilter extends OncePerRequestFilter {

    static final String CURRENT_USER_ATTR = "currentUser";

    // Paths under /api/** that do not require an authenticated session
    private static final Set<String> PUBLIC_PREFIXES = Set.of(
            "/api/users",
            "/api/auth/login",
            "/api/auth/logout"
    );

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public SessionAuthFilter(UserRepository userRepository, ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Long userId = (Long) session.getAttribute(AuthController.SESSION_USER_KEY);
            if (userId != null) {
                userRepository.findById(userId)
                        .ifPresent(user -> request.setAttribute(CURRENT_USER_ATTR, user));
            }
        }

        // CORS preflights must reach the CorsFilter without an auth check
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();
        boolean isProtected = path.startsWith("/api/")
                && PUBLIC_PREFIXES.stream().noneMatch(path::startsWith);

        if (isProtected && request.getAttribute(CURRENT_USER_ATTR) == null) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getWriter(),
                    ErrorResponse.of("UNAUTHORIZED", "Authentication required"));
            return;
        }

        chain.doFilter(request, response);
    }
}
