package com.civicconnect.compliance.exception;

public class ServiceUnavailableException extends RuntimeException {
    public ServiceUnavailableException(String serviceName) {
        super(serviceName + " is currently unavailable. Please try again later.");
    }
}

