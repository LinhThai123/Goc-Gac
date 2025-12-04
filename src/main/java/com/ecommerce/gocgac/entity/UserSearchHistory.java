package com.ecommerce.gocgac.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_search_history", indexes = {
    @Index(name = "idx_user_search_history_user", columnList = "user_id"),
    @Index(name = "idx_keyword", columnList = "search_keyword"),
    @Index(name = "idx_searched", columnList = "searched_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserSearchHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id")
    private Long userId;
    
    @Column(name = "search_keyword", nullable = false, length = 500)
    private String searchKeyword;
    
    @Column(name = "result_count", nullable = false)
    private Integer resultCount = 0;
    
    @Column(name = "searched_at", nullable = false, updatable = false)
    private LocalDateTime searchedAt = LocalDateTime.now();
}

