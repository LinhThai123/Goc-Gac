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
}
