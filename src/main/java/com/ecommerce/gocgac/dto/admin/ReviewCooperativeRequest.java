package com.ecommerce.gocgac.dto.admin;

import com.ecommerce.gocgac.entity.enums.ApprovalStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewCooperativeRequest {
    
    @NotNull(message = "Trạng thái không được để trống")
    private ApprovalStatus status;
    
    private String rejectionReason; // Bắt buộc nếu status = REJECTED
}

