package com.ecommerce.gocgac.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_variants", indexes = {
    @Index(name = "idx_variant_product", columnList = "product_id"),
    @Index(name = "idx_variant_sku", columnList = "sku"),
    @Index(name = "idx_variant_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariant {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "product_id", nullable = false)
    private Long productId;
    
    @Column(name = "variant_name", length = 255)
    private String variantName; // Tên biến thể: "Đỏ - Size L", "500g", etc.
    
    @Column(unique = true, length = 100, nullable = false)
    private String sku; // Stock Keeping Unit - mã SKU duy nhất
    
    // Các thuộc tính biến thể phổ biến (lưu trực tiếp để đơn giản và query nhanh)
    @Column(length = 50)
    private String size; // Size: S, M, L, XL, etc.
    
    @Column(length = 50)
    private String color; // Màu sắc: Đỏ, Xanh, Đen, etc.
    
    @Column(name = "material", length = 100)
    private String material; // Chất liệu: Cotton, Polyester, etc.
    
    @Column(name = "weight_variant", length = 50)
    private String weightVariant; // Trọng lượng biến thể: "500g", "1kg", etc.
    
    @Column(name = "barcode", length = 100)
    private String barcode; // Mã vạch (nếu có)
    
    // Giá và tồn kho của SKU
    // Nếu NULL, có thể dùng giá từ Product (nếu Product có)
    // Nếu có giá trị, sẽ override giá từ Product
    @Column(precision = 15, scale = 2)
    private BigDecimal price; // Giá riêng của SKU này
    
    @Column(name = "compare_price", precision = 15, scale = 2)
    private BigDecimal comparePrice; // Giá so sánh riêng của SKU
    
    @Column(name = "cost_price", precision = 15, scale = 2)
    private BigDecimal costPrice; // Giá vốn riêng của SKU
    
    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity = 0;
    
    @Column(name = "reserved_quantity", nullable = false)
    private Integer reservedQuantity = 0; // Số lượng đã được đặt hàng nhưng chưa thanh toán
    
    @Column(name = "available_quantity", nullable = false)
    private Integer availableQuantity = 0; // Số lượng có sẵn = stockQuantity - reservedQuantity
    
    // Hình ảnh riêng cho SKU này (nếu có)
    @Column(name = "image_url", length = 500)
    private String imageUrl;
    
    // Trọng lượng và kích thước riêng cho SKU này
    // Nếu NULL, sẽ dùng giá trị từ Product (nếu Product có)
    // Nếu có giá trị, sẽ override giá trị từ Product
    @Column(name = "weight_kg", precision = 10, scale = 3)
    private BigDecimal weightKg; // Trọng lượng riêng của SKU (kg)
    
    @Column(name = "length_cm", precision = 10, scale = 2)
    private BigDecimal lengthCm; // Chiều dài riêng của SKU (cm)
    
    @Column(name = "width_cm", precision = 10, scale = 2)
    private BigDecimal widthCm; // Chiều rộng riêng của SKU (cm)
    
    @Column(name = "height_cm", precision = 10, scale = 2)
    private BigDecimal heightCm; // Chiều cao riêng của SKU (cm)
    
    @Column(nullable = false, length = 50)
    private String status = "active"; // Trạng thái SKU: "active", "inactive", "discontinued"
    // Lưu ý: Khác với Product.status (ProductStatus enum), đây là trạng thái riêng của từng SKU
    
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0; // Thứ tự hiển thị
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
        // Tự động tính available quantity
        this.availableQuantity = Math.max(0, this.stockQuantity - this.reservedQuantity);
    }
    
    @PrePersist
    public void prePersist() {
        this.availableQuantity = Math.max(0, this.stockQuantity - this.reservedQuantity);
    }
}

