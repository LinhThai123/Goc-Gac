package com.ecommerce.gocgac.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation để đánh dấu field cần được sanitize để chống XSS
 * Có thể sử dụng trong DTOs
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Sanitize {
    /**
     * Loại sanitize: strict (loại bỏ tất cả HTML) hoặc html (cho phép một số HTML tags an toàn)
     */
    SanitizeType value() default SanitizeType.STRICT;
    
    enum SanitizeType {
        STRICT,  // Loại bỏ tất cả HTML tags
        HTML     // Cho phép một số HTML tags an toàn
    }
}

