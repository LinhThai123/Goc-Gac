package com.ecommerce.gocgac.dto.cooperative;

import com.ecommerce.gocgac.entity.enums.ApprovalStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewMemberRequest {
    
    @NotNull(message = "Status không được để trống")
    private ApprovalStatus status;
    
    @Size(max = 1000, message = "Lý do từ chối không được vượt quá 1000 ký tự")
    private String rejectionReason; // Required nếu status = REJECTED
}

