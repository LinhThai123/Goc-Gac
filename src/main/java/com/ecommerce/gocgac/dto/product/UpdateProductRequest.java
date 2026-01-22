package com.ecommerce.gocgac.dto.product;

import com.ecommerce.gocgac.entity.enums.ProductType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO cho việc cập nhật sản phẩm
 * Tất cả các field đều optional - chỉ cập nhật những field được gửi lên
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductRequest {
    
    @Size(max = 500, message = "Tên sản phẩm không được vượt quá 500 ký tự")
    private String productName;
    
    @Size(max = 100, message = "Mã sản phẩm không được vượt quá 100 ký tự")
    private String productCode; // Optional, có thể thay đổi
    
    @Size(max = 500, message = "Slug không được vượt quá 500 ký tự")
    private String slug; // Optional, có thể thay đổi
    
    private String description; // Mô tả chi tiết
    
    @Size(max = 500, message = "Mô tả ngắn không được vượt quá 500 ký tự")
    private String shortDescription; // Mô tả ngắn
    
    private ProductType productType;
    
    private Long categoryId; // Category chính
    
    private Long storeCategoryId; // Category của store
    
    // Các trường cho sản phẩm phi vật lý (khóa học, dịch vụ...)
    private Integer durationHours; // Thời lượng (giờ) - cho khóa học
    
    private Integer accessPeriodDays; // Thời gian truy cập (ngày)
    
    private Boolean isUnlimitedAccess; // Truy cập không giới hạn
    
    @Size(max = 50, message = "Phương thức giao hàng không được vượt quá 50 ký tự")
    private String deliveryMethod; // Phương thức giao hàng: "download", "email", "online_access", etc.
    
    // Các flag
    private Boolean isCombo; // Có phải combo không
    
    private Boolean ocopCertified; // Có chứng nhận OCOP không
    
    @Size(max = 10, message = "Cấp độ OCOP không được vượt quá 10 ký tự")
    private String ocopLevel; // Cấp độ OCOP
    
    private Boolean hasOriginTracking; // Có truy xuất nguồn gốc không
    
    // SKU - Optional: Nếu có, sẽ cập nhật/thêm/xóa SKUs
    // Nếu không có, giữ nguyên SKUs hiện tại
    // Mỗi SKU trong list phải có id nếu muốn cập nhật, không có id nếu muốn thêm mới
    // Để xóa SKU, không gửi SKU đó trong list
    @Valid
    private List<SkuRequest> skus;
    
    // Cho biết sản phẩm có nhiều biến thể hay không
    // Nếu có nhiều hơn 1 SKU, tự động set hasVariants = true
    private Boolean hasVariants;
    
    // Status - có thể cập nhật status của sản phẩm
    private com.ecommerce.gocgac.entity.enums.ProductStatus status;
}

