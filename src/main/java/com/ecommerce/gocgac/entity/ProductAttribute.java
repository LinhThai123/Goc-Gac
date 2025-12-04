package com.ecommerce.gocgac.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_attributes", 
    indexes = @Index(name = "idx_product_attr", columnList = "product_id, attribute_name"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductAttribute {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "product_id", nullable = false)
    private Long productId;
    
    @Column(name = "attribute_name", nullable = false, length = 100)
    private String attributeName;
    
    @Column(name = "attribute_value", nullable = false, columnDefinition = "TEXT")
    private String attributeValue;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}

