package com.ecommerce.gocgac.dto.cooperative;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmitRegistrationRequest {
    
    @NotNull(message = "ID đơn đăng ký không được để trống")
    private Long registrationId;
}

