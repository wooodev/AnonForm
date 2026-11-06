package com.anonform.common.exception;

public class DuplicateResponseException extends RuntimeException {
    public DuplicateResponseException(String message) {
        super(message);
    }
}
