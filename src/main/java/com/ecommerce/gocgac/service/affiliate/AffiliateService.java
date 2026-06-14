package com.ecommerce.gocgac.service.affiliate;

import com.ecommerce.gocgac.common.response.PageResponse;
import com.ecommerce.gocgac.dto.affiliate.*;
import com.ecommerce.gocgac.entity.*;
import com.ecommerce.gocgac.entity.enums.ApprovalStatus;
import com.ecommerce.gocgac.entity.enums.CommissionStatus;
import com.ecommerce.gocgac.entity.enums.OrderStatus;
import com.ecommerce.gocgac.exception.BusinessException;
import com.ecommerce.gocgac.exception.ResourceNotFoundException;
import com.ecommerce.gocgac.repository.*;
import com.ecommerce.gocgac.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Dịch vụ Tiếp thị liên kết (Sprint 10 - M17).
 *
 * <p>Mô hình theo Shopee/TikTok/Lazada: đăng ký → duyệt → tạo link → ghi click
 * (quy gán <b>server-side last-click</b> trong N ngày) → đơn được quy gán tạo hoa hồng
 * PENDING (tính <b>theo từng sản phẩm</b>) → duyệt sau khi đơn DELIVERED qua hạn cooling-off
 * → chi trả. Đơn hủy/hoàn ⇒ hoa hồng CANCELLED. Cấm tự mua.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AffiliateService {

    private final AffiliateRegistrationRepository registrationRepository;
    private final AffiliatePartnerRepository partnerRepository;
    private final AffiliateLinkRepository linkRepository;
    private final AffiliateClickRepository clickRepository;
    private final AffiliateCommissionRepository commissionRepository;
    private final ProductCommissionRepository productCommissionRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;
    private final NotificationService notificationService;

    @Value("${gocgac.affiliate.attribution-window-days:7}")
    private int attributionWindowDays;

    @Value("${gocgac.affiliate.cooling-off-days:7}")
    private int coolingOffDays;

    @Value("${gocgac.affiliate.default-commission-rate:5.00}")
    private BigDecimal defaultCommissionRate;

    @Value("${gocgac.frontend.base-url:http://localhost:4202}")
    private String frontendBaseUrl;

    private static final String TYPE_AFFILIATE = "AFFILIATE";
    private static final String REF_COMMISSION = "AFFILIATE_COMMISSION";
    private static final String ACTIVE = "active";

    // ========== Registration (user) ==========

    @Transactional
    public void register(Long userId, RegisterAffiliateRequest req) {
        if (partnerRepository.existsByUserId(userId)) {
            throw new BusinessException("Bạn đã là đối tác tiếp thị liên kết");
        }
        if (registrationRepository.existsByUserIdAndStatus(userId, ApprovalStatus.PENDING)) {
            throw new BusinessException("Bạn đã có đơn đăng ký đang chờ duyệt");
        }
        AffiliateRegistration reg = new AffiliateRegistration();
        reg.setUserId(userId);
        reg.setApplicationReason(req.getApplicationReason());
        reg.setStatus(ApprovalStatus.PENDING);
        registrationRepository.save(reg);
    }

    public AffiliateRegistration getMyRegistration(Long userId) {
        return registrationRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Bạn chưa đăng ký tiếp thị liên kết"));
    }

    // ========== Admin approval ==========

    public PageResponse<AffiliateRegistration> getPendingRegistrations(Pageable pageable) {
        return PageResponse.from(
            registrationRepository.findByStatusOrderBySubmittedAtDesc(ApprovalStatus.PENDING, pageable));
    }

    @Transactional
    public void review(Long registrationId, Long reviewerId, ReviewAffiliateRequest req) {
        AffiliateRegistration reg = registrationRepository.findById(registrationId)
            .orElseThrow(() -> new ResourceNotFoundException("Đơn đăng ký không tồn tại"));
        reg.setReviewedAt(LocalDateTime.now());
        reg.setReviewedBy(reviewerId);

        if (req.isApproved()) {
            reg.setStatus(ApprovalStatus.APPROVED);
            registrationRepository.save(reg);
            if (!partnerRepository.existsByUserId(reg.getUserId())) {
                AffiliatePartner partner = new AffiliatePartner();
                partner.setUserId(reg.getUserId());
                partner.setAffiliateCode(generateCode());
                partner.setCommissionRate(req.getCommissionRate() != null ? req.getCommissionRate() : defaultCommissionRate);
                partner.setStatus(ACTIVE);
                partnerRepository.save(partner);
            }
            notificationService.notify(reg.getUserId(), TYPE_AFFILIATE, "Đăng ký tiếp thị liên kết được duyệt",
                "Bạn đã trở thành đối tác tiếp thị liên kết.", "AFFILIATE_REGISTRATION", reg.getId());
        } else {
            reg.setStatus(ApprovalStatus.REJECTED);
            reg.setRejectionReason(req.getRejectionReason());
            registrationRepository.save(reg);
            notificationService.notify(reg.getUserId(), TYPE_AFFILIATE, "Đăng ký tiếp thị liên kết bị từ chối",
                req.getRejectionReason() != null ? req.getRejectionReason() : "Đơn của bạn chưa được duyệt.",
                "AFFILIATE_REGISTRATION", reg.getId());
        }
    }

    // ========== Links (partner) ==========

    @Transactional
    public AffiliateLinkResponse createLink(Long userId, CreateAffiliateLinkRequest req) {
        AffiliatePartner partner = getPartner(userId);
        Product product = productRepository.findById(req.getProductId())
            .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tồn tại"));
        AffiliateLink link = linkRepository.findFirstByPartnerIdAndProductId(partner.getId(), product.getId())
            .orElseGet(() -> createLinkEntity(partner, product));
        return AffiliateLinkResponse.from(link);
    }

    public PageResponse<AffiliateLinkResponse> getMyLinks(Long userId, Pageable pageable) {
        AffiliatePartner partner = getPartner(userId);
        return PageResponse.from(
            linkRepository.findByPartnerIdOrderByCreatedAtDesc(partner.getId(), pageable)
                .map(AffiliateLinkResponse::from));
    }

    // ========== Click tracking (public) ==========

    /** Ghi nhận click và trả về URL trang sản phẩm để redirect. */
    @Transactional
    public String trackClick(String code, Long productId, Long userId, String ipAddress) {
        AffiliatePartner partner = partnerRepository.findByAffiliateCode(code)
            .orElseThrow(() -> new ResourceNotFoundException("Mã affiliate không tồn tại"));
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tồn tại"));

        AffiliateLink link = linkRepository.findFirstByPartnerIdAndProductId(partner.getId(), productId)
            .orElseGet(() -> createLinkEntity(partner, product));
        link.setClickCount(nz(link.getClickCount()) + 1);
        linkRepository.save(link);

        partner.setTotalClicks(nz(partner.getTotalClicks()) + 1);
        partnerRepository.save(partner);

        AffiliateClick click = new AffiliateClick();
        click.setLinkId(link.getId());
        click.setUserId(userId);
        click.setIpAddress(ipAddress);
        clickRepository.save(click);

        return buildProductUrl(product, code);
    }

    // ========== Attribution + commission (gọi từ OrderService) ==========

    /** Quy gán đơn cho đối tác qua last-click & tạo hoa hồng PENDING (tính theo từng sản phẩm). */
    @Transactional
    public void attributeOrder(Order order, Long buyerId) {
        if (commissionRepository.existsByOrderId(order.getId())) {
            return;
        }
        LocalDateTime after = LocalDateTime.now().minusDays(attributionWindowDays);
        AffiliateClick click = clickRepository
            .findFirstByUserIdAndClickedAtAfterOrderByClickedAtDesc(buyerId, after).orElse(null);
        if (click == null) return;

        AffiliateLink link = linkRepository.findById(click.getLinkId()).orElse(null);
        if (link == null) return;
        AffiliatePartner partner = partnerRepository.findById(link.getPartnerId()).orElse(null);
        if (partner == null || !ACTIVE.equals(partner.getStatus())) return;
        if (partner.getUserId().equals(buyerId)) return; // cấm tự mua

        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        BigDecimal totalCommission = BigDecimal.ZERO;
        for (OrderItem item : items) {
            BigDecimal ratePercent = effectiveRatePercent(item.getProductId(), partner);
            BigDecimal itemCommission = item.getSubtotal()
                .multiply(ratePercent)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            totalCommission = totalCommission.add(itemCommission);
        }
        if (totalCommission.compareTo(BigDecimal.ZERO) <= 0) return;

        BigDecimal orderAmount = order.getSubtotal();
        BigDecimal blendedRate = orderAmount.compareTo(BigDecimal.ZERO) > 0
            ? totalCommission.multiply(BigDecimal.valueOf(100)).divide(orderAmount, 2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;

        AffiliateCommission commission = new AffiliateCommission();
        commission.setPartnerId(partner.getId());
        commission.setOrderId(order.getId());
        commission.setOrderAmount(orderAmount);
        commission.setCommissionRate(blendedRate);
        commission.setCommissionAmount(totalCommission);
        commission.setStatus(CommissionStatus.PENDING);
        commissionRepository.save(commission);

        order.setAffiliateCode(partner.getAffiliateCode());

        link.setConversionCount(nz(link.getConversionCount()) + 1);
        linkRepository.save(link);
        partner.setTotalOrders(nz(partner.getTotalOrders()) + 1);
        partner.setTotalRevenue(partner.getTotalRevenue().add(orderAmount));
        partnerRepository.save(partner);

        notificationService.notify(partner.getUserId(), TYPE_AFFILIATE, "Có đơn từ link của bạn",
            "Một đơn hàng đã được quy gán cho bạn, hoa hồng đang chờ xác nhận.", REF_COMMISSION, commission.getId());
        log.info("Order {} attributed to affiliate partner {} (commission {})", order.getId(), partner.getId(), totalCommission);
    }

    /** Hủy hoa hồng khi đơn bị hủy/hoàn (trừ phần đã PAID). */
    @Transactional
    public void voidCommissionForOrder(Long orderId) {
        for (AffiliateCommission c : commissionRepository.findByOrderId(orderId)) {
            if (c.getStatus() == CommissionStatus.PAID || c.getStatus() == CommissionStatus.CANCELLED) {
                continue;
            }
            boolean wasApproved = c.getStatus() == CommissionStatus.APPROVED;
            c.setStatus(CommissionStatus.CANCELLED);
            commissionRepository.save(c);
            partnerRepository.findById(c.getPartnerId()).ifPresent(p -> {
                p.setTotalOrders(Math.max(0, nz(p.getTotalOrders()) - 1));
                p.setTotalRevenue(p.getTotalRevenue().subtract(c.getOrderAmount()).max(BigDecimal.ZERO));
                if (wasApproved) {
                    p.setTotalCommission(p.getTotalCommission().subtract(c.getCommissionAmount()).max(BigDecimal.ZERO));
                }
                partnerRepository.save(p);
            });
        }
    }

    // ========== Cooling-off approval (scheduler) ==========

    @Transactional
    public int approveDueCommissions() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(coolingOffDays);
        List<AffiliateCommission> due = commissionRepository.findApprovable(
            CommissionStatus.PENDING, OrderStatus.DELIVERED, threshold);
        for (AffiliateCommission c : due) {
            c.setStatus(CommissionStatus.APPROVED);
            commissionRepository.save(c);
            partnerRepository.findById(c.getPartnerId()).ifPresent(p -> {
                p.setTotalCommission(p.getTotalCommission().add(c.getCommissionAmount()));
                partnerRepository.save(p);
            });
            notificationService.notify(
                partnerRepository.findById(c.getPartnerId()).map(AffiliatePartner::getUserId).orElse(null),
                TYPE_AFFILIATE, "Hoa hồng được xác nhận",
                "Hoa hồng đơn #" + c.getOrderId() + " đã được xác nhận.", REF_COMMISSION, c.getId());
        }
        if (!due.isEmpty()) log.info("Approved {} affiliate commission(s)", due.size());
        return due.size();
    }

    // ========== Dashboard / admin ==========

    public AffiliatePartnerResponse getDashboard(Long userId) {
        return AffiliatePartnerResponse.from(getPartner(userId));
    }

    public PageResponse<AffiliateCommissionResponse> getMyCommissions(Long userId, Pageable pageable) {
        AffiliatePartner partner = getPartner(userId);
        return PageResponse.from(commissionRepository.findByPartnerIdOrderByCreatedAtDesc(partner.getId(), pageable)
            .map(AffiliateCommissionResponse::from));
    }

    public PageResponse<AffiliateCommissionResponse> listCommissions(CommissionStatus status, Pageable pageable) {
        return PageResponse.from(commissionRepository.findByStatusOrderByCreatedAtDesc(status, pageable)
            .map(AffiliateCommissionResponse::from));
    }

    @Transactional
    public AffiliateCommissionResponse payCommission(Long commissionId) {
        AffiliateCommission c = commissionRepository.findById(commissionId)
            .orElseThrow(() -> new ResourceNotFoundException("Hoa hồng không tồn tại"));
        if (c.getStatus() != CommissionStatus.APPROVED) {
            throw new BusinessException("Chỉ chi trả hoa hồng đã được duyệt (APPROVED)");
        }
        c.setStatus(CommissionStatus.PAID);
        c.setPaidAt(LocalDateTime.now());
        return AffiliateCommissionResponse.from(commissionRepository.save(c));
    }

    // ========== Helpers ==========

    private BigDecimal effectiveRatePercent(Long productId, AffiliatePartner partner) {
        return productCommissionRepository.findByProductId(productId)
            .filter(pc -> Boolean.TRUE.equals(pc.getIsActive()))
            .map(ProductCommission::getCommissionRate)
            .orElse(partner.getCommissionRate());
    }

    private AffiliateLink createLinkEntity(AffiliatePartner partner, Product product) {
        AffiliateLink link = new AffiliateLink();
        link.setPartnerId(partner.getId());
        link.setProductId(product.getId());
        link.setStoreId(product.getStoreId());
        link.setAffiliateCode(partner.getAffiliateCode());
        link.setLinkUrl(buildProductUrl(product, partner.getAffiliateCode()));
        return linkRepository.save(link);
    }

    private String buildProductUrl(Product product, String code) {
        return frontendBaseUrl + "/san-pham/" + product.getSlug() + "?aff=" + code;
    }

    private AffiliatePartner getPartner(Long userId) {
        return partnerRepository.findByUserId(userId)
            .orElseThrow(() -> new BusinessException("Bạn chưa phải đối tác tiếp thị liên kết"));
    }

    private String generateCode() {
        String code;
        do {
            code = "AFF" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (partnerRepository.existsByAffiliateCode(code));
        return code;
    }

    private int nz(Integer value) {
        return value != null ? value : 0;
    }
}
