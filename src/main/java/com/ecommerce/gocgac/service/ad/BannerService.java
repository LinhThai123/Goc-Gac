package com.ecommerce.gocgac.service.ad;

import com.ecommerce.gocgac.dto.ad.BannerResponse;
import com.ecommerce.gocgac.dto.ad.CreateBannerRequest;
import com.ecommerce.gocgac.dto.ad.UpdateBannerRequest;
import com.ecommerce.gocgac.entity.Banner;
import com.ecommerce.gocgac.exception.ResourceNotFoundException;
import com.ecommerce.gocgac.repository.BannerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Quản lý banner trang chủ (Sprint 11 - M18). Admin CRUD; công khai theo vị trí + lịch hiển thị.
 */
@Service
@RequiredArgsConstructor
public class BannerService {

    private final BannerRepository bannerRepository;

    @Transactional
    public BannerResponse create(CreateBannerRequest req) {
        Banner b = new Banner();
        b.setBannerName(req.getBannerName());
        b.setBannerPosition(req.getBannerPosition());
        b.setImageUrl(req.getImageUrl());
        b.setMobileImageUrl(req.getMobileImageUrl());
        b.setLinkUrl(req.getLinkUrl());
        b.setTargetType(req.getTargetType() != null ? req.getTargetType() : "_self");
        b.setStartDate(req.getStartDate());
        b.setEndDate(req.getEndDate());
        b.setDisplayOrder(req.getDisplayOrder() != null ? req.getDisplayOrder() : 0);
        b.setIsActive(true);
        return BannerResponse.from(bannerRepository.save(b));
    }

    @Transactional
    public BannerResponse update(Long id, UpdateBannerRequest req) {
        Banner b = getOrThrow(id);
        if (req.getBannerName() != null) b.setBannerName(req.getBannerName());
        if (req.getBannerPosition() != null) b.setBannerPosition(req.getBannerPosition());
        if (req.getImageUrl() != null) b.setImageUrl(req.getImageUrl());
        if (req.getMobileImageUrl() != null) b.setMobileImageUrl(req.getMobileImageUrl());
        if (req.getLinkUrl() != null) b.setLinkUrl(req.getLinkUrl());
        if (req.getTargetType() != null) b.setTargetType(req.getTargetType());
        if (req.getStartDate() != null) b.setStartDate(req.getStartDate());
        if (req.getEndDate() != null) b.setEndDate(req.getEndDate());
        if (req.getDisplayOrder() != null) b.setDisplayOrder(req.getDisplayOrder());
        if (req.getIsActive() != null) b.setIsActive(req.getIsActive());
        return BannerResponse.from(bannerRepository.save(b));
    }

    @Transactional
    public void delete(Long id) {
        if (!bannerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Banner không tồn tại");
        }
        bannerRepository.deleteById(id);
    }

    public List<BannerResponse> listActive(String position) {
        return bannerRepository.findActive(position, LocalDateTime.now())
            .stream().map(BannerResponse::from).toList();
    }

    public List<BannerResponse> listAll() {
        return bannerRepository.findAllByOrderByDisplayOrderAsc()
            .stream().map(BannerResponse::from).toList();
    }

    private Banner getOrThrow(Long id) {
        return bannerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Banner không tồn tại"));
    }
}
