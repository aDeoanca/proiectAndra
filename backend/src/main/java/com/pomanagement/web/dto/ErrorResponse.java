package com.pomanagement.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

public record ErrorResponse(ErrorDetail error) {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ErrorDetail(String code, String message, Map<String, String> details) {
    }

    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(new ErrorDetail(code, message, null));
    }

    public static ErrorResponse of(String code, String message, Map<String, String> details) {
        return new ErrorResponse(new ErrorDetail(code, message, details));
    }
}
