package com.demo.student.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

/** 409 - e.g. an email address that is already registered. */
public class DuplicateResourceException extends ApiException {

    public DuplicateResourceException(String field, String message) {
        super(HttpStatus.CONFLICT, message, Map.of(field, message));
    }
}
