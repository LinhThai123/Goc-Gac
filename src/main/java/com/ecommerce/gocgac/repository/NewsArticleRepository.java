package com.ecommerce.gocgac.repository;

import com.ecommerce.gocgac.entity.NewsArticle;
import com.ecommerce.gocgac.entity.enums.ArticleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NewsArticleRepository extends JpaRepository<NewsArticle, Long> {

    Page<NewsArticle> findByStatusOrderByPublishedAtDesc(ArticleStatus status, Pageable pageable);

    Page<NewsArticle> findByCategoryIdAndStatusOrderByPublishedAtDesc(Long categoryId, ArticleStatus status, Pageable pageable);

    Page<NewsArticle> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Optional<NewsArticle> findBySlugAndStatus(String slug, ArticleStatus status);

    boolean existsBySlug(String slug);

    @Modifying
    @Query("UPDATE NewsArticle a SET a.viewCount = a.viewCount + 1 WHERE a.id = :id")
    void incrementViewCount(@Param("id") Long id);
}
