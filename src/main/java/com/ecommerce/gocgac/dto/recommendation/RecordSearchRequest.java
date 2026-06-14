package com.ecommerce.gocgac.dto.recommendation;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RecordSearchRequest {

    @NotBlank(message = "Từ khóa không được để trống")
    private String keyword;

    private Integer resultCount;
}
