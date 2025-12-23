package com.ecommerce.gocgac.dto.cooperative;

import com.ecommerce.gocgac.entity.enums.ApprovalStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data object để đưa vào MessageResponse.data
 * Chứa thông tin về registration step
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationStepData {
    private Long registrationId;
    private Integer step;
    private ApprovalStatus status;
}

