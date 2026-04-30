package com.civicconnect.notification.exception;
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String r, Long id) { super(r + " not found with id: " + id); }
    public ResourceNotFoundException(String m) { super(m); }
}
