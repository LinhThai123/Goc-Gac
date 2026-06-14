package com.ecommerce.gocgac.dto.news;

import com.ecommerce.gocgac.entity.NewsArticle;
import com.ecommerce.gocgac.entity.enums.ArticleStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NewsArticleResponse {

    private Long id;
    private Long categoryId;
    private String title;
    private String slug;
    private String summary;
    private String content;
    private String featuredImage;
    private Long authorId;
    private Integer viewCount;
    private ArticleStatus status;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;

    public static NewsArticleResponse from(NewsArticle a) {
        return NewsArticleResponse.builder()
            .id(a.getId())
            .categoryId(a.getCategoryId())
            .title(a.getTitle())
            .slug(a.getSlug())
            .summary(a.getSummary())
            .content(a.getContent())
            .featuredImage(a.getFeaturedImage())
            .authorId(a.getAuthorId())
            .viewCount(a.getViewCount())
            .status(a.getStatus())
            .publishedAt(a.getPublishedAt())
            .createdAt(a.getCreatedAt())
            .build();
    }
}
