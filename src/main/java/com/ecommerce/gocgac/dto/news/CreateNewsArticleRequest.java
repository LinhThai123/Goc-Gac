package com.ecommerce.gocgac.dto.news;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateNewsArticleRequest {

    private Long categoryId;

    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;

    /** Slug tùy chọn — tự sinh từ tiêu đề nếu để trống. */
    private String slug;

    private String summary;

    @NotBlank(message = "Nội dung không được để trống")
    private String content;

    private String featuredImage;

    /** true = đăng ngay (PUBLISHED), false/null = lưu nháp (DRAFT). */
    private Boolean publish;
}
