package com.task.management.exception;

//404 Not Found
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException() {
        super("User not found in the database");
    }

    public UserNotFoundException(String message) {
        super(message);
    }
}