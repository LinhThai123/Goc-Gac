package com.ecommerce.gocgac.entity;

import com.ecommerce.gocgac.entity.enums.ArticleStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "news_articles", indexes = {
    @Index(name = "idx_news_articles_category", columnList = "category_id"),
    @Index(name = "idx_news_articles_slug", columnList = "slug"),
    @Index(name = "idx_news_articles_status", columnList = "status"),
    @Index(name = "idx_news_articles_published", columnList = "published_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NewsArticle {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "category_id")
    private Long categoryId;
    
    @Column(nullable = false, length = 500)
    private String title;
    
    @Column(nullable = false, length = 500)
    private String slug;
    
    @Column(columnDefinition = "TEXT")
    private String summary;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "featured_image", length = 500)
    private String featuredImage;
    
    @Column(name = "author_id")
    private Long authorId;
    
    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ArticleStatus status = ArticleStatus.DRAFT;
    
    @Column(name = "published_at")
    private LocalDateTime publishedAt;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

