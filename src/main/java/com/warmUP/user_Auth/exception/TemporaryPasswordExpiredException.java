package com.warmUP.user_Auth.exception;

public class TemporaryPasswordExpiredException extends RuntimeException {
    public TemporaryPasswordExpiredException(String message) {
        super(message);
    }
}
