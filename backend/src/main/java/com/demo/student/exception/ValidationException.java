package com.demo.student.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

/** 400 - a validation rule that Bean Validation annotations cannot express. */
public class ValidationException extends ApiException {

    public ValidationException(String field, String message) {
        super(HttpStatus.BAD_REQUEST, "Validation failed", Map.of(field, message));
    }
}
