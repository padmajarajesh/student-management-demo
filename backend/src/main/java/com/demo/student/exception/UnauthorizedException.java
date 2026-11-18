package com.demo.student.exception;

import org.springframework.http.HttpStatus;

/** 401 - bad credentials or a missing/expired/revoked token. */
public class UnauthorizedException extends ApiException {

    public UnauthorizedException(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }
}
