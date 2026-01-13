package com.ecommerce.gocgac.dto.channel;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateChannelRequest {
    
    @Size(min = 3, max = 100, message = "Channel slug phải từ 3-100 ký tự")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Channel slug chỉ chứa chữ thường, số và dấu gạch ngang")
    private String channelSlug;
    
    @Size(max = 255, message = "Channel name không được vượt quá 255 ký tự")
    private String channelName;
    
    @Size(max = 500, message = "Short description không được vượt quá 500 ký tự")
    private String shortDescription;
    
    private String description;
    
    private String bannerUrl;
    
    private String logoUrl;
    
    private String videoUrl;
    
    private String storyContent;
    
    private String originStory;
    
    private String themeSettings; // JSON string
    
    private Boolean isActive;
    
    private Boolean isFeatured;
    
    private String metaTitle;
    
    private String metaDescription;
    
    private String metaKeywords;
}

