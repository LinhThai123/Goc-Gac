package com.ecommerce.gocgac.service.news;

import com.ecommerce.gocgac.common.util.SlugUtils;
import com.ecommerce.gocgac.common.util.XssSanitizer;
import com.ecommerce.gocgac.dto.news.CreateNewsCategoryRequest;
import com.ecommerce.gocgac.dto.news.NewsCategoryResponse;
import com.ecommerce.gocgac.dto.news.UpdateNewsCategoryRequest;
import com.ecommerce.gocgac.entity.NewsCategory;
import com.ecommerce.gocgac.exception.BadRequestException;
import com.ecommerce.gocgac.exception.ResourceNotFoundException;
import com.ecommerce.gocgac.repository.NewsCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Quản lý danh mục tin tức (Sprint 12 - M19).
 */
@Service
@RequiredArgsConstructor
public class NewsCategoryService {

    private final NewsCategoryRepository categoryRepository;

    @Transactional
    public NewsCategoryResponse create(CreateNewsCategoryRequest req) {
        String slug = StringUtils.hasText(req.getCategorySlug())
            ? SlugUtils.toSlug(req.getCategorySlug())
            : SlugUtils.toSlug(req.getCategoryName());
        slug = ensureUniqueSlug(slug);

        NewsCategory c = new NewsCategory();
        c.setCategoryName(XssSanitizer.sanitize(req.getCategoryName()));
        c.setCategorySlug(slug);
        c.setDescription(XssSanitizer.sanitizeHtml(req.getDescription()));
        c.setDisplayOrder(req.getDisplayOrder() != null ? req.getDisplayOrder() : 0);
        c.setIsActive(true);
        return NewsCategoryResponse.from(categoryRepository.save(c));
    }

    @Transactional
    public NewsCategoryResponse update(Long id, UpdateNewsCategoryRequest req) {
        NewsCategory c = getOrThrow(id);
        if (req.getCategoryName() != null) c.setCategoryName(XssSanitizer.sanitize(req.getCategoryName()));
        if (req.getDescription() != null) c.setDescription(XssSanitizer.sanitizeHtml(req.getDescription()));
        if (req.getDisplayOrder() != null) c.setDisplayOrder(req.getDisplayOrder());
        if (req.getIsActive() != null) c.setIsActive(req.getIsActive());
        return NewsCategoryResponse.from(categoryRepository.save(c));
    }

    @Transactional
    public void delete(Long id) {
        NewsCategory c = getOrThrow(id);
        c.setIsActive(false);
        categoryRepository.save(c);
    }

    public List<NewsCategoryResponse> listActive() {
        return categoryRepository.findByIsActiveTrueOrderByDisplayOrderAsc()
            .stream().map(NewsCategoryResponse::from).toList();
    }

    private NewsCategory getOrThrow(Long id) {
        return categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Danh mục tin không tồn tại"));
    }

    private String ensureUniqueSlug(String base) {
        if (!StringUtils.hasText(base)) {
            throw new BadRequestException("Không tạo được slug từ tên danh mục");
        }
        String slug = base;
        int i = 1;
        while (categoryRepository.existsByCategorySlug(slug)) {
            slug = base + "-" + (++i);
        }
        return slug;
    }
}
