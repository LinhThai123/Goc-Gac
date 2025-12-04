package com.ecommerce.gocgac.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "affiliate_links", indexes = {
    @Index(name = "idx_affiliate_links_partner", columnList = "partner_id"),
    @Index(name = "idx_affiliate_links_code", columnList = "affiliate_code")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AffiliateLink {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "partner_id", nullable = false)
    private Long partnerId;
    
    @Column(name = "product_id")
    private Long productId;
    
    @Column(name = "store_id")
    private Long storeId;
    
    @Column(name = "affiliate_code", nullable = false, length = 50)
    private String affiliateCode;
    
    @Column(name = "link_url", nullable = false, columnDefinition = "TEXT")
    private String linkUrl;
    
    @Column(name = "click_count", nullable = false)
    private Integer clickCount = 0;
    
    @Column(name = "conversion_count", nullable = false)
    private Integer conversionCount = 0;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}

