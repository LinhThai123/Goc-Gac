package com.ecommerce.gocgac.service.ad;

import com.ecommerce.gocgac.common.response.PageResponse;
import com.ecommerce.gocgac.dto.ad.AdCampaignResponse;
import com.ecommerce.gocgac.dto.ad.CreateAdCampaignRequest;
import com.ecommerce.gocgac.dto.ad.ReviewAdRequest;
import com.ecommerce.gocgac.dto.ad.UpdateAdCampaignRequest;
import com.ecommerce.gocgac.entity.AdCampaign;
import com.ecommerce.gocgac.entity.AdClick;
import com.ecommerce.gocgac.entity.Product;
import com.ecommerce.gocgac.entity.enums.ApprovalStatus;
import com.ecommerce.gocgac.exception.BadRequestException;
import com.ecommerce.gocgac.exception.BusinessException;
import com.ecommerce.gocgac.exception.ResourceNotFoundException;
import com.ecommerce.gocgac.repository.AdCampaignRepository;
import com.ecommerce.gocgac.repository.AdClickRepository;
import com.ecommerce.gocgac.repository.ProductRepository;
import com.ecommerce.gocgac.service.notification.NotificationService;
import com.ecommerce.gocgac.service.store.StoreResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Dịch vụ Quảng cáo (Sprint 11 - M18).
 *
 * <p>Người bán tạo chiến dịch (DRAFT) → gửi duyệt (PENDING) → admin duyệt (APPROVED, active).
 * Khi có click: trừ ngân sách theo CPC, tự dừng (completed) khi hết ngân sách.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdCampaignService {

    private final AdCampaignRepository campaignRepository;
    private final AdClickRepository adClickRepository;
    private final ProductRepository productRepository;
    private final StoreResolver storeResolver;
    private final NotificationService notificationService;

    @Value("${gocgac.frontend.base-url:http://localhost:4202}")
    private String frontendBaseUrl;

    private static final String STATUS_ACTIVE = "active";
    private static final String STATUS_COMPLETED = "completed";
    private static final String TYPE_AD = "AD";

    // ===== Seller =====

    @Transactional
    public AdCampaignResponse create(Long sellerUserId, CreateAdCampaignRequest req) {
        Long storeId = storeResolver.resolveStoreId(sellerUserId);
        Product product = productRepository.findById(req.getProductId())
            .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tồn tại"));
        if (!product.getStoreId().equals(storeId)) {
            throw new BusinessException("Sản phẩm không thuộc gian hàng của bạn");
        }
        validateDates(req.getStartDate(), req.getEndDate());

        AdCampaign c = new AdCampaign();
        c.setStoreId(storeId);
        c.setCampaignName(req.getCampaignName());
        c.setProductId(req.getProductId());
        c.setAdImageUrl(req.getAdImageUrl());
        c.setAdPosition(req.getAdPosition());
        c.setStartDate(req.getStartDate());
        c.setEndDate(req.getEndDate());
        c.setBudget(req.getBudget());
        c.setCostPerClick(req.getCostPerClick());
        c.setApprovalStatus(ApprovalStatus.DRAFT);
        c.setStatus("scheduled");
        return AdCampaignResponse.from(campaignRepository.save(c));
    }

    @Transactional
    public AdCampaignResponse update(Long sellerUserId, Long id, UpdateAdCampaignRequest req) {
        AdCampaign c = getOwned(sellerUserId, id);
        if (c.getApprovalStatus() != ApprovalStatus.DRAFT && c.getApprovalStatus() != ApprovalStatus.REJECTED) {
            throw new BusinessException("Chỉ sửa được chiến dịch khi đang nháp hoặc bị từ chối");
        }
        if (req.getCampaignName() != null) c.setCampaignName(req.getCampaignName());
        if (req.getAdImageUrl() != null) c.setAdImageUrl(req.getAdImageUrl());
        if (req.getAdPosition() != null) c.setAdPosition(req.getAdPosition());
        if (req.getStartDate() != null) c.setStartDate(req.getStartDate());
        if (req.getEndDate() != null) c.setEndDate(req.getEndDate());
        if (req.getBudget() != null) c.setBudget(req.getBudget());
        if (req.getCostPerClick() != null) c.setCostPerClick(req.getCostPerClick());
        validateDates(c.getStartDate(), c.getEndDate());
        return AdCampaignResponse.from(campaignRepository.save(c));
    }

    @Transactional
    public AdCampaignResponse submit(Long sellerUserId, Long id) {
        AdCampaign c = getOwned(sellerUserId, id);
        if (c.getApprovalStatus() != ApprovalStatus.DRAFT && c.getApprovalStatus() != ApprovalStatus.REJECTED) {
            throw new BusinessException("Chiến dịch đã được gửi duyệt");
        }
        c.setApprovalStatus(ApprovalStatus.PENDING);
        return AdCampaignResponse.from(campaignRepository.save(c));
    }

    public PageResponse<AdCampaignResponse> getMyCampaigns(Long sellerUserId, Pageable pageable) {
        Long storeId = storeResolver.resolveStoreId(sellerUserId);
        return PageResponse.from(campaignRepository.findByStoreIdOrderByCreatedAtDesc(storeId, pageable)
            .map(AdCampaignResponse::from));
    }

    // ===== Public serving =====

    public java.util.List<AdCampaignResponse> getServableAds(String position) {
        return campaignRepository.findServable(position, ApprovalStatus.APPROVED, LocalDateTime.now())
            .stream().map(AdCampaignResponse::from).toList();
    }

    /** Ghi nhận click, trừ ngân sách (CPC), trả về URL đích để redirect. */
    @Transactional
    public String trackClick(Long campaignId, Long userId, String ip, String userAgent) {
        AdCampaign c = campaignRepository.findById(campaignId)
            .orElseThrow(() -> new ResourceNotFoundException("Chiến dịch không tồn tại"));
        String target = buildTarget(c);

        if (isServable(c)) {
            AdClick click = new AdClick();
            click.setCampaignId(c.getId());
            click.setUserId(userId);
            click.setIpAddress(ip);
            click.setUserAgent(userAgent);
            adClickRepository.save(click);

            c.setTotalClicks(nz(c.getTotalClicks()) + 1);
            if (c.getCostPerClick() != null) {
                c.setTotalSpent(c.getTotalSpent().add(c.getCostPerClick()));
                if (c.getBudget() != null && c.getTotalSpent().compareTo(c.getBudget()) >= 0) {
                    c.setStatus(STATUS_COMPLETED); // hết ngân sách → dừng
                }
            }
            campaignRepository.save(c);
        }
        return target;
    }

    // ===== Admin =====

    public PageResponse<AdCampaignResponse> getPending(Pageable pageable) {
        return PageResponse.from(
            campaignRepository.findByApprovalStatusOrderByCreatedAtDesc(ApprovalStatus.PENDING, pageable)
                .map(AdCampaignResponse::from));
    }

    @Transactional
    public AdCampaignResponse review(Long id, Long reviewerId, ReviewAdRequest req) {
        AdCampaign c = campaignRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Chiến dịch không tồn tại"));
        c.setApprovedBy(reviewerId);
        c.setApprovedAt(LocalDateTime.now());
        Long ownerUserId = safeOwner(c.getStoreId());

        if (req.isApproved()) {
            c.setApprovalStatus(ApprovalStatus.APPROVED);
            c.setStatus(STATUS_ACTIVE);
            notify(ownerUserId, "Quảng cáo được duyệt", "Chiến dịch \"" + c.getCampaignName() + "\" đã được duyệt.", c.getId());
        } else {
            c.setApprovalStatus(ApprovalStatus.REJECTED);
            c.setRejectionReason(req.getRejectionReason());
            notify(ownerUserId, "Quảng cáo bị từ chối",
                req.getRejectionReason() != null ? req.getRejectionReason() : "Chiến dịch chưa được duyệt.", c.getId());
        }
        return AdCampaignResponse.from(campaignRepository.save(c));
    }

    // ===== Helpers =====

    private boolean isServable(AdCampaign c) {
        LocalDateTime now = LocalDateTime.now();
        boolean budgetLeft = c.getBudget() == null || c.getTotalSpent().compareTo(c.getBudget()) < 0;
        return c.getApprovalStatus() == ApprovalStatus.APPROVED
            && STATUS_ACTIVE.equals(c.getStatus())
            && !now.isBefore(c.getStartDate()) && !now.isAfter(c.getEndDate())
            && budgetLeft;
    }

    private String buildTarget(AdCampaign c) {
        return productRepository.findById(c.getProductId())
            .map(p -> frontendBaseUrl + "/san-pham/" + p.getSlug())
            .orElse(frontendBaseUrl);
    }

    private AdCampaign getOwned(Long sellerUserId, Long id) {
        Long storeId = storeResolver.resolveStoreId(sellerUserId);
        AdCampaign c = campaignRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Chiến dịch không tồn tại"));
        if (!c.getStoreId().equals(storeId)) {
            throw new BusinessException("Chiến dịch không thuộc gian hàng của bạn");
        }
        return c;
    }

    private Long safeOwner(Long storeId) {
        try {
            return storeResolver.resolveStoreOwnerUserId(storeId);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private void notify(Long userId, String title, String message, Long campaignId) {
        if (userId != null) {
            notificationService.notify(userId, TYPE_AD, title, message, "AD_CAMPAIGN", campaignId);
        }
    }

    private void validateDates(LocalDateTime start, LocalDateTime end) {
        if (start != null && end != null && end.isBefore(start)) {
            throw new BadRequestException("Ngày kết thúc phải sau ngày bắt đầu");
        }
    }

    private int nz(Integer value) {
        return value != null ? value : 0;
    }
}
