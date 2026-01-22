package com.ecommerce.gocgac.exception;

public class StoreCategoryException extends RuntimeException {

    public StoreCategoryException(String message) {
        super(message);
    }

    public StoreCategoryException(String message, Throwable cause) {
        super(message, cause);
    }
}

