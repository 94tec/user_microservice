package com.warmUP.user_Auth.exception;

public class InvalidPasswordException extends RuntimeException  {
    public InvalidPasswordException(String message) {
        super(message);
    }
}
