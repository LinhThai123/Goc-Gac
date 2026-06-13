package com.ecommerce.gocgac.exception;

/**
 * Ném ra khi dữ liệu đầu vào không hợp lệ về mặt nghiệp vụ.
 * Được {@code GlobalExceptionHandler} ánh xạ thành HTTP 400.
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
