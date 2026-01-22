package com.ecommerce.gocgac.dto.product;

import com.ecommerce.gocgac.entity.enums.ProductType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductRequest {
    
    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(max = 500, message = "Tên sản phẩm không được vượt quá 500 ký tự")
    private String productName;
    
    @Size(max = 100, message = "Mã sản phẩm không được vượt quá 100 ký tự")
    private String productCode; // Optional, có thể tự generate
    
    @Size(max = 500, message = "Slug không được vượt quá 500 ký tự")
    private String slug; // Optional, có thể tự generate từ productName
    
    private String description; // Mô tả chi tiết
    
    @Size(max = 500, message = "Mô tả ngắn không được vượt quá 500 ký tự")
    private String shortDescription; // Mô tả ngắn
    
    @NotNull(message = "Loại sản phẩm không được để trống")
    private ProductType productType = ProductType.PHYSICAL;
    
    private Long categoryId; // Category chính
    
    private Long storeCategoryId; // Category của store
    
    // Các trường cho sản phẩm phi vật lý (khóa học, dịch vụ...)
    private Integer durationHours; // Thời lượng (giờ) - cho khóa học
    
    private Integer accessPeriodDays; // Thời gian truy cập (ngày)
    
    private Boolean isUnlimitedAccess = false; // Truy cập không giới hạn
    
    @Size(max = 50, message = "Phương thức giao hàng không được vượt quá 50 ký tự")
    private String deliveryMethod; // Phương thức giao hàng: "download", "email", "online_access", etc.
    
    // Các flag
    private Boolean isCombo = false; // Có phải combo không
    
    private Boolean ocopCertified = false; // Có chứng nhận OCOP không
    
    @Size(max = 10, message = "Cấp độ OCOP không được vượt quá 10 ký tự")
    private String ocopLevel; // Cấp độ OCOP
    
    private Boolean hasOriginTracking = false; // Có truy xuất nguồn gốc không
    
    // SKU - BẮT BUỘC: Mỗi Product phải có ít nhất 1 SKU
    @NotEmpty(message = "Sản phẩm phải có ít nhất 1 SKU")
    @Valid
    private List<SkuRequest> skus;
    
    // Cho biết sản phẩm có nhiều biến thể hay không
    // Nếu có nhiều hơn 1 SKU, tự động set hasVariants = true
    private Boolean hasVariants = false;
}

