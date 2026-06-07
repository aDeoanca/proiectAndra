package com.pomanagement.web.dto;

import com.pomanagement.domain.entity.User;
import com.pomanagement.domain.enums.Role;

public record UserDto(Long id, String name, Role role) {

    public static UserDto from(User user) {
        return new UserDto(user.getId(), user.getName(), user.getRole());
    }
}
