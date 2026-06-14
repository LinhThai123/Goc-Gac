package com.ecommerce.gocgac.dto.news;

import lombok.Data;

/** Cập nhật bài viết — chỉ trường khác null mới được áp dụng. */
@Data
public class UpdateNewsArticleRequest {

    private Long categoryId;
    private String title;
    private String summary;
    private String content;
    private String featuredImage;
}
