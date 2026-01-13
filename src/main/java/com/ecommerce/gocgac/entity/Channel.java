package com.ecommerce.gocgac.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity cho Channel (Micro-site riêng cho shop)
 * Mỗi shop có một channel riêng với slug, banner, theme customization
 */
@Entity
@Table(name = "channels", indexes = {
    @Index(name = "idx_channels_store", columnList = "store_id"),
    @Index(name = "idx_channels_cooperative", columnList = "cooperative_id"),
    @Index(name = "idx_channels_slug", columnList = "channel_slug"),
    @Index(name = "idx_channels_active", columnList = "is_active"),
    @Index(name = "idx_channels_featured", columnList = "is_featured")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Channel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "store_id", nullable = false)
    private Long storeId;
    
    @Column(name = "cooperative_id")
    private Long cooperativeId;
    
    // ========== Thông tin cơ bản ==========
    @Column(name = "channel_slug", unique = true, nullable = false, length = 100)
    private String channelSlug; // URL slug: gocgac.com/shop/:slug
    
    @Column(name = "channel_name", nullable = false, length = 255)
    private String channelName;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "short_description", length = 500)
    private String shortDescription;
    
    // ========== Media ==========
    @Column(name = "banner_url", length = 500)
    private String bannerUrl;
    
    @Column(name = "logo_url", length = 500)
    private String logoUrl;
    
    @Column(name = "video_url", length = 500)
    private String videoUrl;
    
    // ========== Story/Nguồn gốc sản phẩm ==========
    @Column(name = "story_content", columnDefinition = "TEXT")
    private String storyContent;
    
    @Column(name = "origin_story", columnDefinition = "TEXT")
    private String originStory;
    
    // ========== Theme customization ==========
    @Column(name = "theme_settings", columnDefinition = "TEXT")
    private String themeSettings; // JSON: {"primaryColor": "#ff0000", "secondaryColor": "#00ff00", "logo": "url"}
    
    // ========== Social features ==========
    @Column(name = "follower_count", nullable = false)
    private Integer followerCount = 0;
    
    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;
    
    // ========== Status ==========
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "is_featured", nullable = false)
    private Boolean isFeatured = false;
    
    // ========== SEO ==========
    @Column(name = "meta_title", length = 255)
    private String metaTitle;
    
    @Column(name = "meta_description", columnDefinition = "TEXT")
    private String metaDescription;
    
    @Column(name = "meta_keywords", length = 500)
    private String metaKeywords;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

