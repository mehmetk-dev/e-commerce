package com.mehmetkerem.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends BaseException {
    public BadRequestException(String message, HttpStatus httpStatus) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
