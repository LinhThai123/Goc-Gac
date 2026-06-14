package com.ecommerce.gocgac.service.news;

import com.ecommerce.gocgac.common.response.PageResponse;
import com.ecommerce.gocgac.common.util.SlugUtils;
import com.ecommerce.gocgac.common.util.XssSanitizer;
import com.ecommerce.gocgac.dto.news.CreateNewsArticleRequest;
import com.ecommerce.gocgac.dto.news.NewsArticleResponse;
import com.ecommerce.gocgac.dto.news.UpdateNewsArticleRequest;
import com.ecommerce.gocgac.entity.NewsArticle;
import com.ecommerce.gocgac.entity.enums.ArticleStatus;
import com.ecommerce.gocgac.exception.BadRequestException;
import com.ecommerce.gocgac.exception.ResourceNotFoundException;
import com.ecommerce.gocgac.repository.NewsArticleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * Quản lý bài viết tin tức (Sprint 12 - M19).
 *
 * <p>Vòng đời: DRAFT → PUBLISHED (đăng) → HIDDEN (gỡ). Nội dung được làm sạch HTML.
 * Chỉ bài PUBLISHED mới hiển thị công khai và được đếm lượt xem.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NewsArticleService {

    private final NewsArticleRepository articleRepository;

    // ========== Admin ==========

    @Transactional
    public NewsArticleResponse create(Long authorId, CreateNewsArticleRequest req) {
        NewsArticle a = new NewsArticle();
        a.setCategoryId(req.getCategoryId());
        a.setTitle(XssSanitizer.sanitize(req.getTitle()));
        a.setSlug(ensureUniqueSlug(StringUtils.hasText(req.getSlug())
            ? SlugUtils.toSlug(req.getSlug()) : SlugUtils.toSlug(req.getTitle())));
        a.setSummary(XssSanitizer.sanitizeHtml(req.getSummary()));
        a.setContent(XssSanitizer.sanitizeHtml(req.getContent()));
        a.setFeaturedImage(req.getFeaturedImage());
        a.setAuthorId(authorId);
        a.setViewCount(0);
        if (Boolean.TRUE.equals(req.getPublish())) {
            a.setStatus(ArticleStatus.PUBLISHED);
            a.setPublishedAt(LocalDateTime.now());
        } else {
            a.setStatus(ArticleStatus.DRAFT);
        }
        a = articleRepository.save(a);
        log.info("News article {} created by user {} (status {})", a.getId(), authorId, a.getStatus());
        return NewsArticleResponse.from(a);
    }

    @Transactional
    public NewsArticleResponse update(Long id, UpdateNewsArticleRequest req) {
        NewsArticle a = getOrThrow(id);
        if (req.getCategoryId() != null) a.setCategoryId(req.getCategoryId());
        if (req.getTitle() != null) a.setTitle(XssSanitizer.sanitize(req.getTitle()));
        if (req.getSummary() != null) a.setSummary(XssSanitizer.sanitizeHtml(req.getSummary()));
        if (req.getContent() != null) a.setContent(XssSanitizer.sanitizeHtml(req.getContent()));
        if (req.getFeaturedImage() != null) a.setFeaturedImage(req.getFeaturedImage());
        return NewsArticleResponse.from(articleRepository.save(a));
    }

    @Transactional
    public NewsArticleResponse publish(Long id) {
        NewsArticle a = getOrThrow(id);
        a.setStatus(ArticleStatus.PUBLISHED);
        if (a.getPublishedAt() == null) {
            a.setPublishedAt(LocalDateTime.now());
        }
        return NewsArticleResponse.from(articleRepository.save(a));
    }

    @Transactional
    public NewsArticleResponse unpublish(Long id) {
        NewsArticle a = getOrThrow(id);
        a.setStatus(ArticleStatus.HIDDEN);
        return NewsArticleResponse.from(articleRepository.save(a));
    }

    @Transactional
    public void delete(Long id) {
        if (!articleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Bài viết không tồn tại");
        }
        articleRepository.deleteById(id);
    }

    public PageResponse<NewsArticleResponse> listAll(Pageable pageable) {
        return PageResponse.from(
            articleRepository.findAllByOrderByCreatedAtDesc(pageable).map(NewsArticleResponse::from));
    }

    // ========== Public ==========

    public PageResponse<NewsArticleResponse> listPublished(Pageable pageable) {
        return PageResponse.from(
            articleRepository.findByStatusOrderByPublishedAtDesc(ArticleStatus.PUBLISHED, pageable)
                .map(NewsArticleResponse::from));
    }

    public PageResponse<NewsArticleResponse> listPublishedByCategory(Long categoryId, Pageable pageable) {
        return PageResponse.from(
            articleRepository.findByCategoryIdAndStatusOrderByPublishedAtDesc(categoryId, ArticleStatus.PUBLISHED, pageable)
                .map(NewsArticleResponse::from));
    }

    /** Xem chi tiết bài viết công khai theo slug + tăng lượt xem. */
    @Transactional
    public NewsArticleResponse getPublishedBySlug(String slug) {
        NewsArticle a = articleRepository.findBySlugAndStatus(slug, ArticleStatus.PUBLISHED)
            .orElseThrow(() -> new ResourceNotFoundException("Bài viết không tồn tại"));
        articleRepository.incrementViewCount(a.getId());
        a.setViewCount((a.getViewCount() != null ? a.getViewCount() : 0) + 1);
        return NewsArticleResponse.from(a);
    }

    // ========== Helpers ==========

    private NewsArticle getOrThrow(Long id) {
        return articleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Bài viết không tồn tại"));
    }

    private String ensureUniqueSlug(String base) {
        if (!StringUtils.hasText(base)) {
            throw new BadRequestException("Không tạo được slug từ tiêu đề");
        }
        String slug = base;
        int i = 1;
        while (articleRepository.existsBySlug(slug)) {
            slug = base + "-" + (++i);
        }
        return slug;
    }
}
