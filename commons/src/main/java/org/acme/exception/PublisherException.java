package org.acme.exception;

public class PublisherException extends RuntimeException {

    public PublisherException(String message) {
        super(message);
    }

    public  PublisherException(String message, Throwable cause) {
        super(message, cause);
    }

}

