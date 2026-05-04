package com.task.management.exception;

public class TenantCreationException extends RuntimeException {
    public TenantCreationException(String message) {
        super(message);
    }
}