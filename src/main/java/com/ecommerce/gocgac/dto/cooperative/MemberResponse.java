package com.ecommerce.gocgac.dto.cooperative;

import com.ecommerce.gocgac.entity.enums.ApprovalStatus;
import com.ecommerce.gocgac.entity.enums.CooperativeMemberRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberResponse {
    
    private Long id;
    private Long cooperativeId;
    private String cooperativeName; // Tên HTX
    private Long userId; // ID của User (CUSTOMER hoặc SELLER) - Thành viên HTX
    private String userName; // Tên User
    private String userEmail; // Email User
    private CooperativeMemberRole role; // Vai trò trong HTX
    private ApprovalStatus status;
    private String rejectionReason;
    private LocalDateTime appliedAt;
    private LocalDateTime joinedAt;
    private LocalDateTime reviewedAt;
}

