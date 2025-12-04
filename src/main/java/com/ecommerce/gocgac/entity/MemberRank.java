package com.ecommerce.gocgac.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "member_ranks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberRank {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "rank_name", nullable = false, length = 100)
    private String rankName;
    
    @Column(name = "min_spending", nullable = false, precision = 15, scale = 2)
    private BigDecimal minSpending;
    
    @Column(name = "period_months", nullable = false)
    private Integer periodMonths = 12;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(columnDefinition = "TEXT")
    private String benefits;
    
    @Column(name = "rank_order", nullable = false)
    private Integer rankOrder;
    
    @Column(nullable = false)
    private String status = "active"; // active, inactive
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

