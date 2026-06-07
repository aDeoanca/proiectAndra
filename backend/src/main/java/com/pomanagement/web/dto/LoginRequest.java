package com.pomanagement.web.dto;

import jakarta.validation.constraints.NotNull;

public record LoginRequest(@NotNull Long userId) {
}
