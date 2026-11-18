package com.demo.student.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Base class for expected errors. The GlobalExceptionHandler turns it into a
 * JSON response with the given HTTP status.
 */
public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final Map<String, String> fieldErrors;

    public ApiException(HttpStatus status, String message) {
        this(status, message, null);
    }

    public ApiException(HttpStatus status, String message, Map<String, String> fieldErrors) {
        super(message);
        this.status = status;
        this.fieldErrors = fieldErrors;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
}
