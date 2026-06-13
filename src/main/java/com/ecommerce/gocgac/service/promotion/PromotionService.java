package com.ecommerce.gocgac.service.promotion;

import com.ecommerce.gocgac.common.response.PageResponse;
import com.ecommerce.gocgac.dto.promotion.CreatePromotionRequest;
import com.ecommerce.gocgac.dto.promotion.PromotionResponse;
import com.ecommerce.gocgac.dto.promotion.UpdatePromotionRequest;
import com.ecommerce.gocgac.entity.Product;
import com.ecommerce.gocgac.entity.Promotion;
import com.ecommerce.gocgac.entity.PromotionProduct;
import com.ecommerce.gocgac.entity.enums.PromotionStatus;
import com.ecommerce.gocgac.exception.BadRequestException;
import com.ecommerce.gocgac.exception.BusinessException;
import com.ecommerce.gocgac.exception.ResourceNotFoundException;
import com.ecommerce.gocgac.repository.ProductRepository;
import com.ecommerce.gocgac.repository.PromotionProductRepository;
import com.ecommerce.gocgac.repository.PromotionRepository;
import com.ecommerce.gocgac.service.store.StoreResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Dịch vụ Khuyến mãi (Sprint 5 - M12).
 * CRUD chương trình khuyến mãi, gán/bỏ sản phẩm, và refresh trạng thái theo lịch.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PromotionService {

    private final PromotionRepository promotionRepository;
    private final PromotionProductRepository promotionProductRepository;
    private final ProductRepository productRepository;
    private final StoreResolver storeResolver;

    @Transactional
    public PromotionResponse createPromotion(Long sellerUserId, CreatePromotionRequest req) {
        Long storeId = storeResolver.resolveStoreId(sellerUserId);
        validateDateRange(req.getStartDate(), req.getEndDate());

        Promotion p = new Promotion();
        p.setStoreId(storeId);
        p.setPromotionName(req.getPromotionName());
        p.setDescription(req.getDescription());
        p.setPromotionType(req.getPromotionType());
        p.setDiscountValue(req.getDiscountValue());
        p.setStartDate(req.getStartDate());
        p.setEndDate(req.getEndDate());
        p.setStatus(computeStatus(req.getStartDate(), req.getEndDate()));
        p = promotionRepository.save(p);

        if (req.getProductIds() != null) {
            attachProducts(p.getId(), storeId, req.getProductIds());
        }
        log.info("Promotion {} created for store {}", p.getId(), storeId);
        return toResponse(p);
    }

    @Transactional
    public PromotionResponse updatePromotion(Long sellerUserId, Long promotionId, UpdatePromotionRequest req) {
        Promotion p = getOwned(sellerUserId, promotionId);
        if (req.getPromotionName() != null) p.setPromotionName(req.getPromotionName());
        if (req.getDescription() != null) p.setDescription(req.getDescription());
        if (req.getDiscountValue() != null) p.setDiscountValue(req.getDiscountValue());
        if (req.getStartDate() != null) p.setStartDate(req.getStartDate());
        if (req.getEndDate() != null) p.setEndDate(req.getEndDate());
        if (req.getStatus() != null) p.setStatus(req.getStatus());
        validateDateRange(p.getStartDate(), p.getEndDate());
        p = promotionRepository.save(p);
        return toResponse(p);
    }

    @Transactional
    public void cancelPromotion(Long sellerUserId, Long promotionId) {
        Promotion p = getOwned(sellerUserId, promotionId);
        p.setStatus(PromotionStatus.CANCELLED);
        promotionRepository.save(p);
    }

    public PageResponse<PromotionResponse> getStorePromotions(Long sellerUserId, Pageable pageable) {
        Long storeId = storeResolver.resolveStoreId(sellerUserId);
        return PageResponse.from(
            promotionRepository.findByStoreIdOrderByCreatedAtDesc(storeId, pageable).map(this::toResponse));
    }

    public PromotionResponse getPromotion(Long sellerUserId, Long promotionId) {
        return toResponse(getOwned(sellerUserId, promotionId));
    }

    @Transactional
    public PromotionResponse addProducts(Long sellerUserId, Long promotionId, List<Long> productIds) {
        Promotion p = getOwned(sellerUserId, promotionId);
        attachProducts(p.getId(), p.getStoreId(), productIds);
        return toResponse(p);
    }

    @Transactional
    public PromotionResponse removeProduct(Long sellerUserId, Long promotionId, Long productId) {
        Promotion p = getOwned(sellerUserId, promotionId);
        promotionProductRepository.deleteByPromotionIdAndProductId(p.getId(), productId);
        return toResponse(p);
    }

    // ========== Scheduled ==========

    @Transactional
    public void refreshStatuses() {
        LocalDateTime now = LocalDateTime.now();
        int activated = promotionRepository.activateDue(PromotionStatus.SCHEDULED, PromotionStatus.ACTIVE, now);
        int expired = promotionRepository.expireOverdue(
            PromotionStatus.SCHEDULED, PromotionStatus.ACTIVE, PromotionStatus.EXPIRED, now);
        if (activated > 0 || expired > 0) {
            log.info("Promotion status refresh: activated={}, expired={}", activated, expired);
        }
    }

    // ========== Helpers ==========

    private void attachProducts(Long promotionId, Long storeId, List<Long> productIds) {
        for (Long productId : productIds) {
            Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tồn tại: " + productId));
            if (!product.getStoreId().equals(storeId)) {
                throw new BusinessException("Sản phẩm " + productId + " không thuộc gian hàng của bạn");
            }
            if (!promotionProductRepository.existsByPromotionIdAndProductId(promotionId, productId)) {
                PromotionProduct pp = new PromotionProduct();
                pp.setPromotionId(promotionId);
                pp.setProductId(productId);
                promotionProductRepository.save(pp);
            }
        }
    }

    private Promotion getOwned(Long sellerUserId, Long promotionId) {
        Long storeId = storeResolver.resolveStoreId(sellerUserId);
        Promotion p = promotionRepository.findById(promotionId)
            .orElseThrow(() -> new ResourceNotFoundException("Khuyến mãi không tồn tại"));
        if (p.getStoreId() == null || !p.getStoreId().equals(storeId)) {
            throw new BusinessException("Bạn không có quyền quản lý khuyến mãi này");
        }
        return p;
    }

    private PromotionStatus computeStatus(LocalDateTime start, LocalDateTime end) {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(start)) return PromotionStatus.SCHEDULED;
        if (now.isAfter(end)) return PromotionStatus.EXPIRED;
        return PromotionStatus.ACTIVE;
    }

    private PromotionResponse toResponse(Promotion p) {
        List<Long> productIds = promotionProductRepository.findByPromotionId(p.getId())
            .stream().map(PromotionProduct::getProductId).toList();
        return PromotionResponse.from(p, productIds);
    }

    private void validateDateRange(LocalDateTime start, LocalDateTime end) {
        if (start != null && end != null && end.isBefore(start)) {
            throw new BadRequestException("Ngày kết thúc phải sau ngày bắt đầu");
        }
    }
}
