package com.pomanagement.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pomanagement.domain.entity.User;
import com.pomanagement.domain.enums.Role;
import com.pomanagement.domain.repository.UserRepository;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@ExtendWith(MockitoExtension.class)
class SessionAuthFilterTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private FilterChain chain;

    private SessionAuthFilter filter;

    private final User testUser = User.builder()
            .id(1L).name("Alice").email("alice@example.com").role(Role.CREATOR).build();

    @BeforeEach
    void setUp() {
        filter = new SessionAuthFilter(userRepository, new ObjectMapper());
    }

    // --- unauthenticated access ---

    @Test
    void unauthenticated_protectedPath_returns401() throws Exception {
        var request = new MockHttpServletRequest("GET", "/api/pos");
        var response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(UNAUTHORIZED.value());
        assertThat(response.getContentAsString()).contains("UNAUTHORIZED");
        verifyNoInteractions(chain);
    }

    @Test
    void unauthenticated_nestedProtectedPath_returns401() throws Exception {
        var request = new MockHttpServletRequest("POST", "/api/pos/10/approve");
        var response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(UNAUTHORIZED.value());
        verifyNoInteractions(chain);
    }

    // --- allowlisted paths pass without auth ---

    @Test
    void unauthenticated_usersPath_passesThrough() throws Exception {
        var request = new MockHttpServletRequest("GET", "/api/users");
        var response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(OK.value());
        verify(chain).doFilter(request, response);
    }

    @Test
    void unauthenticated_loginPath_passesThrough() throws Exception {
        var request = new MockHttpServletRequest("POST", "/api/auth/login");
        var response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(OK.value());
        verify(chain).doFilter(request, response);
    }

    @Test
    void unauthenticated_logoutPath_passesThrough() throws Exception {
        var request = new MockHttpServletRequest("POST", "/api/auth/logout");
        var response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(OK.value());
        verify(chain).doFilter(request, response);
    }

    // --- authenticated requests ---

    @Test
    void authenticated_setsCurrentUserAttribute() throws Exception {
        var session = new MockHttpSession();
        session.setAttribute(AuthController.SESSION_USER_KEY, 1L);
        var request = new MockHttpServletRequest("GET", "/api/pos");
        request.setSession(session);
        var response = new MockHttpServletResponse();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        filter.doFilterInternal(request, response, chain);

        assertThat(request.getAttribute(SessionAuthFilter.CURRENT_USER_ATTR)).isEqualTo(testUser);
        verify(chain).doFilter(request, response);
    }

    @Test
    void authenticated_requestContinuesToChain() throws Exception {
        var session = new MockHttpSession();
        session.setAttribute(AuthController.SESSION_USER_KEY, 1L);
        var request = new MockHttpServletRequest("POST", "/api/pos/10/approve");
        request.setSession(session);
        var response = new MockHttpServletResponse();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        filter.doFilterInternal(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(OK.value());
        verify(chain).doFilter(request, response);
    }

    @Test
    void sessionWithUnknownUserId_treatedAsUnauthenticated() throws Exception {
        var session = new MockHttpSession();
        session.setAttribute(AuthController.SESSION_USER_KEY, 99L);
        var request = new MockHttpServletRequest("GET", "/api/pos");
        request.setSession(session);
        var response = new MockHttpServletResponse();

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        filter.doFilterInternal(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(UNAUTHORIZED.value());
        verifyNoInteractions(chain);
    }
}
