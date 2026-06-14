package com.ecommerce.gocgac.service.news;

import com.ecommerce.gocgac.dto.news.CreateNewsArticleRequest;
import com.ecommerce.gocgac.dto.news.NewsArticleResponse;
import com.ecommerce.gocgac.entity.NewsArticle;
import com.ecommerce.gocgac.entity.enums.ArticleStatus;
import com.ecommerce.gocgac.exception.ResourceNotFoundException;
import com.ecommerce.gocgac.repository.NewsArticleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NewsArticleServiceTest {

    @Mock private NewsArticleRepository articleRepository;
    @InjectMocks private NewsArticleService articleService;

    private static final Long AUTHOR_ID = 3L;

    private CreateNewsArticleRequest request(String title, boolean publish) {
        CreateNewsArticleRequest r = new CreateNewsArticleRequest();
        r.setTitle(title);
        r.setContent("<p>Nội dung bài viết</p>");
        r.setPublish(publish);
        return r;
    }

    @Test
    @DisplayName("Tạo bài đăng ngay: PUBLISHED + có publishedAt, slug không dấu")
    void create_published() {
        when(articleRepository.existsBySlug(any())).thenReturn(false);
        when(articleRepository.save(any(NewsArticle.class))).thenAnswer(inv -> {
            NewsArticle a = inv.getArgument(0);
            a.setId(1L);
            return a;
        });

        NewsArticleResponse res = articleService.create(AUTHOR_ID, request("Tin mới hôm nay", true));

        assertThat(res.getStatus()).isEqualTo(ArticleStatus.PUBLISHED);
        assertThat(res.getPublishedAt()).isNotNull();
        assertThat(res.getSlug()).isEqualTo("tin-moi-hom-nay");
        assertThat(res.getAuthorId()).isEqualTo(AUTHOR_ID);
    }

    @Test
    @DisplayName("Tạo nháp: DRAFT, không có publishedAt")
    void create_draft() {
        when(articleRepository.existsBySlug(any())).thenReturn(false);
        when(articleRepository.save(any(NewsArticle.class))).thenAnswer(inv -> inv.getArgument(0));

        NewsArticleResponse res = articleService.create(AUTHOR_ID, request("Bài nháp", false));

        assertThat(res.getStatus()).isEqualTo(ArticleStatus.DRAFT);
        assertThat(res.getPublishedAt()).isNull();
    }

    @Test
    @DisplayName("Slug trùng → tự thêm hậu tố số")
    void create_duplicateSlug_appendsSuffix() {
        when(articleRepository.existsBySlug("tin-moi")).thenReturn(true);
        when(articleRepository.existsBySlug("tin-moi-2")).thenReturn(false);
        when(articleRepository.save(any(NewsArticle.class))).thenAnswer(inv -> inv.getArgument(0));

        articleService.create(AUTHOR_ID, request("Tin mới", false));

        ArgumentCaptor<NewsArticle> captor = ArgumentCaptor.forClass(NewsArticle.class);
        verify(articleRepository).save(captor.capture());
        assertThat(captor.getValue().getSlug()).isEqualTo("tin-moi-2");
    }

    @Test
    @DisplayName("Xem theo slug: tăng lượt xem")
    void getBySlug_incrementsView() {
        NewsArticle a = new NewsArticle();
        a.setId(5L);
        a.setSlug("tin-moi");
        a.setStatus(ArticleStatus.PUBLISHED);
        a.setViewCount(5);
        when(articleRepository.findBySlugAndStatus("tin-moi", ArticleStatus.PUBLISHED)).thenReturn(Optional.of(a));

        NewsArticleResponse res = articleService.getPublishedBySlug("tin-moi");

        assertThat(res.getViewCount()).isEqualTo(6);
        verify(articleRepository).incrementViewCount(5L);
    }

    @Test
    @DisplayName("Slug không tồn tại → ResourceNotFoundException")
    void getBySlug_notFound_throws() {
        when(articleRepository.findBySlugAndStatus(any(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> articleService.getPublishedBySlug("khong-ton-tai"))
            .isInstanceOf(ResourceNotFoundException.class);

        verify(articleRepository, never()).incrementViewCount(anyLong());
    }

    @Test
    @DisplayName("Đăng bài đang nháp: PUBLISHED + set publishedAt")
    void publish_setsPublishedAt() {
        NewsArticle a = new NewsArticle();
        a.setId(2L);
        a.setStatus(ArticleStatus.DRAFT);
        when(articleRepository.findById(2L)).thenReturn(Optional.of(a));
        when(articleRepository.save(any(NewsArticle.class))).thenAnswer(inv -> inv.getArgument(0));

        NewsArticleResponse res = articleService.publish(2L);

        assertThat(res.getStatus()).isEqualTo(ArticleStatus.PUBLISHED);
        assertThat(res.getPublishedAt()).isNotNull();
    }
}
