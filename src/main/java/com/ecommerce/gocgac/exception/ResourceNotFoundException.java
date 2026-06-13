package com.ecommerce.gocgac.exception;

/**
 * Ném ra khi không tìm thấy tài nguyên (entity) yêu cầu.
 * Được {@code GlobalExceptionHandler} ánh xạ thành HTTP 404.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("Không tìm thấy %s với %s = '%s'", resourceName, fieldName, fieldValue));
    }
}
