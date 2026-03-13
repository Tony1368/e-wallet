package com.hust.thailq.exception;

public class ServiceCommunicationException extends RuntimeException {

    public ServiceCommunicationException() {
        super();
    }

    public ServiceCommunicationException(String message) {
        super(message);
    }

    public ServiceCommunicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
