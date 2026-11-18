package com.demo.student.exception;

import org.springframework.http.HttpStatus;

/** 404 - the resource does not exist, or it belongs to another user. */
public class ResourceNotFoundException extends ApiException {

    public ResourceNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
