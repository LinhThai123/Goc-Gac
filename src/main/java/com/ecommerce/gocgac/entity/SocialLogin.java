package com.ecommerce.gocgac.entity;

import com.ecommerce.gocgac.entity.enums.SocialProvider;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "social_logins", 
    uniqueConstraints = @UniqueConstraint(name = "unique_social_login", columnNames = {"provider", "provider_user_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SocialLogin {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SocialProvider provider;
    
    @Column(name = "provider_user_id", nullable = false)
    private String providerUserId;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}

