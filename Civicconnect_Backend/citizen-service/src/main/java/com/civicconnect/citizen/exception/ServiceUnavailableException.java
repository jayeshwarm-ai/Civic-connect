package com.civicconnect.citizen.exception;

public class ServiceUnavailableException extends RuntimeException {

    public ServiceUnavailableException(String serviceName) {
        super(serviceName + " is currently unavailable. Please try again later.");
    }

    public ServiceUnavailableException(String serviceName, String detail) {
        super(serviceName + " is currently unavailable: " + detail);
    }
}
