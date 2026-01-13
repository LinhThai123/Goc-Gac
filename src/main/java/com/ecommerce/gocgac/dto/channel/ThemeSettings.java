package com.ecommerce.gocgac.dto.channel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Theme settings cho Channel customization
 * Lưu dạng JSON string trong database
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ThemeSettings {
    
    private String primaryColor; // Hex color: #ff0000
    
    private String secondaryColor; // Hex color: #00ff00
    
    private String logo; // Logo URL (nếu muốn override logo mặc định)
    
    private String backgroundColor; // Background color
    
    private String textColor; // Text color
    
    private String fontFamily; // Font family
    
    private Integer fontSize; // Base font size
}

