package com.ecommerce.gocgac.exception;

/**
 * Ném ra khi vi phạm một quy tắc nghiệp vụ (vd: hết tồn kho, voucher hết hạn).
 * Được {@code GlobalExceptionHandler} ánh xạ thành HTTP 400.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
