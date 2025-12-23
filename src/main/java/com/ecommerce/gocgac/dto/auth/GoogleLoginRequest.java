package com.ecommerce.gocgac.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoogleLoginRequest {

    /**
     * Google ID token được trả về sau khi người dùng đăng nhập Google
     */
    @NotBlank(message = "Thiếu idToken của Google")
    private String idToken;
}

