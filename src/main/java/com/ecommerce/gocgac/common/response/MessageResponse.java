package com.ecommerce.gocgac.common.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
    private String message;
    private Integer status; // HTTP status code (200, 201, 400, etc.)
    private Object data;
    private String error;

    /** Phản hồi tùy biến status + data. */
    public static MessageResponse of(int status, String message, Object data) {
        return new MessageResponse(message, status, data, null);
    }

    /** Phản hồi thành công HTTP 200 kèm dữ liệu. */
    public static MessageResponse ok(String message, Object data) {
        return of(200, message, data);
    }

    /** Phản hồi thành công HTTP 200 không kèm dữ liệu. */
    public static MessageResponse ok(String message) {
        return of(200, message, null);
    }

    /** Phản hồi tạo mới thành công HTTP 201 kèm dữ liệu. */
    public static MessageResponse created(String message, Object data) {
        return of(201, message, data);
    }
}
