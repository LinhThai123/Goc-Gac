package com.ecommerce.gocgac.exception;

public class CooperativeException extends RuntimeException {
    
    public CooperativeException(String message) {
        super(message);
    }
    
    public CooperativeException(String message, Throwable cause) {
        super(message, cause);
    }
}

