package com.ecommerce.gocgac.exception;

public class ChannelException extends RuntimeException {
    
    public ChannelException(String message) {
        super(message);
    }
    
    public ChannelException(String message, Throwable cause) {
        super(message, cause);
    }
}

