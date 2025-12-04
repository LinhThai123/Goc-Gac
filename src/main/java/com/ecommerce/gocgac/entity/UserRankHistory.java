package com.ecommerce.gocgac.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Entity
@Table(name = "user_rank_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRankHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "rank_id", nullable = false)
    private Long rankId;
    
    @Column(name = "total_spending", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalSpending;
    
    @Column(name = "achieved_at", nullable = false, updatable = false)
    private LocalDateTime achievedAt = LocalDateTime.now();
    
    @Column(name = "expires_at")
    private LocalDate expiresAt;
}

