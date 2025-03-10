package com.warmUP.user_Auth.exception;

// Custom Exception: UserNotFoundException.java
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String message) {
        super(message);
    }
}
