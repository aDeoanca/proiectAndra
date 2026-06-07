package com.pomanagement.web;

import com.pomanagement.domain.repository.UserRepository;
import com.pomanagement.web.dto.UserDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<UserDto> listUsers() {
        return userRepository.findAll().stream()
                .map(UserDto::from)
                .toList();
    }
}
