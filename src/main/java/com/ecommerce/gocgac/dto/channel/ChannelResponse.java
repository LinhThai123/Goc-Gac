package com.ecommerce.gocgac.dto.channel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChannelResponse {
    
    private Long id;
    
    private Long storeId;
    
    private Long cooperativeId;
    
    private String channelSlug;
    
    private String channelName;
    
    private String shortDescription;
    
    private String description;
    
    private String bannerUrl;
    
    private String logoUrl;
    
    private String videoUrl;
    
    private String storyContent;
    
    private String originStory;
    
    private String themeSettings;
    
    private Integer followerCount;
    
    private Integer viewCount;
    
    private Boolean isActive;
    
    private Boolean isFeatured;
    
    private String metaTitle;
    
    private String metaDescription;
    
    private String metaKeywords;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}

