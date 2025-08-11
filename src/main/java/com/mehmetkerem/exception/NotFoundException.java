package com.mehmetkerem.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends BaseException {
    public NotFoundException(String message, HttpStatus httpStatus) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
