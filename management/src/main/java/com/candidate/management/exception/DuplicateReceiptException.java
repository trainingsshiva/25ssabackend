package com.candidate.management.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class DuplicateReceiptException extends RuntimeException {
    public DuplicateReceiptException(String message) {
        super(message);
    }
}
