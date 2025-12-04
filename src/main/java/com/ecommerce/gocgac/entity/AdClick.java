package com.ecommerce.gocgac.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "ad_clicks", indexes = {
    @Index(name = "idx_campaign", columnList = "campaign_id"),
    @Index(name = "idx_ad_clicks_clicked", columnList = "clicked_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdClick {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "campaign_id", nullable = false)
    private Long campaignId;
    
    @Column(name = "user_id")
    private Long userId;
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;
    
    @Column(name = "clicked_at", nullable = false, updatable = false)
    private LocalDateTime clickedAt = LocalDateTime.now();
    
    @Column(nullable = false)
    private Boolean converted = false;
}

