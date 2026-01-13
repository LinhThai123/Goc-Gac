package com.ecommerce.gocgac.dto.catalog;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddProductToCatalogRequest {
    
    @NotNull(message = "Product ID không được để trống")
    private Long productId;
    
    private Integer displayOrder = 0;
    
    private Boolean isFeatured = false;
}

