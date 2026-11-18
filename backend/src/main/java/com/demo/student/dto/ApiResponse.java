package com.demo.student.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Map;

/**
 * The single JSON envelope used by every endpoint, for both success and errors:
 * <pre>
 * { "success": true,  "message": "...", "data": {...},  "timestamp": "..." }
 * { "success": false, "message": "...", "errors": {...}, "timestamp": "..." }
 * </pre>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(boolean success, String message, T data, Map<String, String> errors, Instant timestamp) {

    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, message, data, null, Instant.now());
    }

    public static <T> ApiResponse<T> ok(String message) {
        return new ApiResponse<>(true, message, null, null, Instant.now());
    }

    public static ApiResponse<Void> error(String message) {
        return new ApiResponse<>(false, message, null, null, Instant.now());
    }

    public static ApiResponse<Void> error(String message, Map<String, String> errors) {
        return new ApiResponse<>(false, message, null, errors, Instant.now());
    }
}
