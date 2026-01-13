package com.ecommerce.gocgac.dto.catalog;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCatalogRequest {
    
    @Size(max = 255, message = "Catalog name không được vượt quá 255 ký tự")
    private String catalogName;
    
    @Size(max = 100, message = "Catalog code không được vượt quá 100 ký tự")
    @Pattern(regexp = "^[A-Z0-9_-]*$", message = "Catalog code chỉ chứa chữ hoa, số, dấu gạch ngang và gạch dưới")
    private String catalogCode;
    
    @Size(max = 500, message = "Short description không được vượt quá 500 ký tự")
    private String shortDescription;
    
    private String description;
    
    private String imageUrl;
    
    private String bannerUrl;
    
    private Integer displayOrder;
    
    private Boolean isFeatured;
    
    private Boolean isActive;
    
    private String catalogSettings; // JSON string
}

