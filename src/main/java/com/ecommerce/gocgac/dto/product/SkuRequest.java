package com.ecommerce.gocgac.dto.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkuRequest {
    
    // ID của SKU (optional) - Nếu có ID thì là update, nếu không có ID thì là create mới
    private Long id;
    
    @NotBlank(message = "SKU không được để trống")
    @Size(max = 100, message = "SKU không được vượt quá 100 ký tự")
    private String sku;
    
    @Size(max = 255, message = "Tên biến thể không được vượt quá 255 ký tự")
    private String variantName; // Tên biến thể: "Đỏ - Size L", "500g", etc.
    
    // Các thuộc tính biến thể
    @Size(max = 50, message = "Size không được vượt quá 50 ký tự")
    private String size; // Size: S, M, L, XL, etc.
    
    @Size(max = 50, message = "Màu sắc không được vượt quá 50 ký tự")
    private String color; // Màu sắc: Đỏ, Xanh, Đen, etc.
    
    @Size(max = 100, message = "Chất liệu không được vượt quá 100 ký tự")
    private String material; // Chất liệu: Cotton, Polyester, etc.
    
    @Size(max = 50, message = "Trọng lượng biến thể không được vượt quá 50 ký tự")
    private String weightVariant; // Trọng lượng biến thể: "500g", "1kg", etc.
    
    @Size(max = 100, message = "Mã vạch không được vượt quá 100 ký tự")
    private String barcode; // Mã vạch (nếu có)
    
    // Giá và tồn kho
    @NotNull(message = "Giá không được để trống")
    private BigDecimal price; // Giá riêng của SKU này
    
    private BigDecimal comparePrice; // Giá so sánh riêng của SKU
    
    private BigDecimal costPrice; // Giá vốn riêng của SKU
    
    @NotNull(message = "Số lượng tồn kho không được để trống")
    private Integer stockQuantity = 0;
    
    @Size(max = 500, message = "URL hình ảnh không được vượt quá 500 ký tự")
    private String imageUrl; // Hình ảnh riêng cho SKU này (nếu có)
    
    // Kích thước và trọng lượng
    private BigDecimal weightKg; // Trọng lượng riêng của SKU (kg)
    
    private BigDecimal lengthCm; // Chiều dài riêng của SKU (cm)
    
    private BigDecimal widthCm; // Chiều rộng riêng của SKU (cm)
    
    private BigDecimal heightCm; // Chiều cao riêng của SKU (cm)
    
    private String status = "active"; // Trạng thái SKU: "active", "inactive", "discontinued"
    
    private Integer displayOrder = 0; // Thứ tự hiển thị
}

