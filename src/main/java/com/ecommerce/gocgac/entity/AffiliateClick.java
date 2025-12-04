package com.ecommerce.gocgac.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "affiliate_clicks", indexes = {
    @Index(name = "idx_link", columnList = "link_id"),
    @Index(name = "idx_affiliate_clicks_clicked", columnList = "clicked_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AffiliateClick {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "link_id", nullable = false)
    private Long linkId;
    
    @Column(name = "user_id")
    private Long userId;
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    @Column(name = "clicked_at", nullable = false, updatable = false)
    private LocalDateTime clickedAt = LocalDateTime.now();
}

