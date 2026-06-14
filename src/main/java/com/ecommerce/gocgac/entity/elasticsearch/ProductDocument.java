package com.ecommerce.gocgac.entity.elasticsearch;

import com.ecommerce.gocgac.entity.enums.ApprovalStatus;
import com.ecommerce.gocgac.entity.enums.ProductStatus;
import com.ecommerce.gocgac.entity.enums.ProductType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ProductDocument - Document model cho Elasticsearch
 * 
 * Index name: products
 * Sử dụng để lưu trữ và tìm kiếm sản phẩm trong Elasticsearch
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "products")
@Setting(settingPath = "elasticsearch/product-settings.json")
public class ProductDocument {
    
    @Id
    private Long id;
    
    @Field(type = FieldType.Long)
    private Long storeId;
    
    @Field(type = FieldType.Long)
    private Long categoryId;
    
    @Field(type = FieldType.Long)
    private Long storeCategoryId;
    
    @Field(type = FieldType.Keyword)
    private String productCode;
    
    @Field(type = FieldType.Text, analyzer = "vi_analyzer", searchAnalyzer = "vi_analyzer")
    private String productName;

    @Field(type = FieldType.Keyword)
    private String slug;

    @Field(type = FieldType.Text, analyzer = "vi_analyzer", searchAnalyzer = "vi_analyzer")
    private String description;

    @Field(type = FieldType.Text, analyzer = "vi_analyzer", searchAnalyzer = "vi_analyzer")
    private String shortDescription;
    
    @Field(type = FieldType.Keyword)
    private ProductType productType;
    
    // Các trường cho sản phẩm phi vật lý
    @Field(type = FieldType.Integer)
    private Integer durationHours;
    
    @Field(type = FieldType.Integer)
    private Integer accessPeriodDays;
    
    @Field(type = FieldType.Boolean)
    private Boolean isUnlimitedAccess;
    
    @Field(type = FieldType.Keyword)
    private String deliveryMethod;
    
    @Field(type = FieldType.Boolean)
    private Boolean hasVariants;
    
    @Field(type = FieldType.Boolean)
    private Boolean isCombo;
    
    @Field(type = FieldType.Boolean)
    private Boolean ocopCertified;
    
    @Field(type = FieldType.Keyword)
    private String ocopLevel;
    
    @Field(type = FieldType.Boolean)
    private Boolean isVerified;
    
    @Field(type = FieldType.Boolean)
    private Boolean hasOriginTracking;
    
    @Field(type = FieldType.Keyword)
    private ApprovalStatus approvalStatus;
    
    @Field(type = FieldType.Integer)
    private Integer soldCount;
    
    @Field(type = FieldType.Integer)
    private Integer viewCount;
    
    @Field(type = FieldType.Double)
    private BigDecimal ratingAverage;
    
    @Field(type = FieldType.Integer)
    private Integer ratingCount;
    
    @Field(type = FieldType.Keyword)
    private ProductStatus status;
    
    // Thông tin giá từ SKU (lấy giá thấp nhất và cao nhất)
    @Field(type = FieldType.Double)
    private BigDecimal minPrice;
    
    @Field(type = FieldType.Double)
    private BigDecimal maxPrice;
    
    @Field(type = FieldType.Double)
    private BigDecimal comparePrice; // Giá so sánh (nếu có)
    
    // Tổng số lượng tồn kho từ tất cả SKUs
    @Field(type = FieldType.Integer)
    private Integer totalStockQuantity;
    
    // Danh sách SKU codes để search
    @Field(type = FieldType.Keyword)
    private List<String> skuCodes;
    
    // Danh sách các thuộc tính variant để filter
    @Field(type = FieldType.Keyword)
    private List<String> sizes;
    
    @Field(type = FieldType.Keyword)
    private List<String> colors;
    
    @Field(type = FieldType.Keyword)
    private List<String> materials;
    
    // Timestamps
    @Field(type = FieldType.Date, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @Field(type = FieldType.Date, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    
    @Field(type = FieldType.Date, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime deletedAt;
    
    // Field để boost relevance khi search
    // Sản phẩm có rating cao, bán chạy sẽ được ưu tiên
    @Field(type = FieldType.Double)
    private Double relevanceScore;
}

