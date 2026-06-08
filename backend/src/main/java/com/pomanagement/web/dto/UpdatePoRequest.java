package com.pomanagement.web.dto;

import com.pomanagement.domain.enums.Category;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record UpdatePoRequest(
        String title,
        String description,
        @Positive @Digits(integer = 10, fraction = 2) BigDecimal amount,
        Category category
) {
}
