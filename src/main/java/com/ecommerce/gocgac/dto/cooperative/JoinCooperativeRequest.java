package com.ecommerce.gocgac.dto.cooperative;

import com.ecommerce.gocgac.entity.enums.CooperativeMemberRole;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JoinCooperativeRequest {
    
    @NotNull(message = "ID HTX không được để trống")
    private Long cooperativeId;
    
    private CooperativeMemberRole role; // Vai trò trong HTX - optional (mặc định là MEMBER)
}

