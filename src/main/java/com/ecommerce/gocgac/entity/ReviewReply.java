package com.ecommerce.gocgac.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "review_replies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewReply {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "review_id", nullable = false)
    private Long reviewId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "reply_content", nullable = false, columnDefinition = "TEXT")
    private String replyContent;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}

