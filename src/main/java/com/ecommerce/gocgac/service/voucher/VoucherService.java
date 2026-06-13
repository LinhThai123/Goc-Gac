package com.ecommerce.gocgac.service.voucher;

import com.ecommerce.gocgac.common.response.PageResponse;
import com.ecommerce.gocgac.dto.voucher.CreateVoucherRequest;
import com.ecommerce.gocgac.dto.voucher.UpdateVoucherRequest;
import com.ecommerce.gocgac.dto.voucher.VoucherResponse;
import com.ecommerce.gocgac.entity.OrderVoucher;
import com.ecommerce.gocgac.entity.User;
import com.ecommerce.gocgac.entity.UserVoucher;
import com.ecommerce.gocgac.entity.Voucher;
import com.ecommerce.gocgac.entity.VoucherUsage;
import com.ecommerce.gocgac.entity.enums.DiscountType;
import com.ecommerce.gocgac.entity.enums.VoucherStatus;
import com.ecommerce.gocgac.entity.enums.VoucherType;
import com.ecommerce.gocgac.exception.BadRequestException;
import com.ecommerce.gocgac.exception.BusinessException;
import com.ecommerce.gocgac.exception.ResourceNotFoundException;
import com.ecommerce.gocgac.repository.*;
import com.ecommerce.gocgac.service.store.StoreResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Dịch vụ Voucher (Sprint 5 - M12).
 *
 * <p>Bao gồm: CRUD voucher phía người bán, lưu voucher của khách, kiểm tra điều kiện
 * và tính giảm giá khi đặt hàng ({@link #previewDiscount}), ghi nhận sử dụng
 * ({@link #commitUsage}), và cron hết hạn ({@link #expireOverdueVouchers}).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VoucherService {

    private final VoucherRepository voucherRepository;
    private final UserVoucherRepository userVoucherRepository;
    private final OrderVoucherRepository orderVoucherRepository;
    private final VoucherUsageRepository voucherUsageRepository;
    private final UserRepository userRepository;
    private final StoreResolver storeResolver;

    /** Kết quả tính giảm giá để OrderService dùng khi checkout. */
    public record VoucherDiscount(Long voucherId, String code, BigDecimal amount) {}

    // ========== Seller CRUD ==========

    @Transactional
    public VoucherResponse createVoucher(Long sellerUserId, CreateVoucherRequest req) {
        Long storeId = storeResolver.resolveStoreId(sellerUserId);
        if (voucherRepository.existsByVoucherCode(req.getVoucherCode())) {
            throw new BadRequestException("Mã voucher đã tồn tại: " + req.getVoucherCode());
        }
        validateDateRange(req.getStartDate(), req.getEndDate());

        Voucher v = new Voucher();
        v.setStoreId(storeId);
        v.setVoucherCode(req.getVoucherCode());
        v.setVoucherName(req.getVoucherName());
        v.setVoucherType(req.getVoucherType());
        v.setDiscountType(req.getDiscountType());
        v.setDiscountValue(req.getDiscountValue());
        v.setMaxDiscountAmount(req.getMaxDiscountAmount());
        v.setMinOrderValue(req.getMinOrderValue());
        v.setUsageLimit(req.getUsageLimit());
        v.setUserUsageLimit(req.getUserUsageLimit() != null ? req.getUserUsageLimit() : 1);
        v.setMemberRankId(req.getMemberRankId());
        v.setStartDate(req.getStartDate());
        v.setEndDate(req.getEndDate());
        v.setStatus(VoucherStatus.ACTIVE);
        v = voucherRepository.save(v);
        log.info("Voucher {} created for store {}", v.getVoucherCode(), storeId);
        return VoucherResponse.from(v);
    }

    @Transactional
    public VoucherResponse updateVoucher(Long sellerUserId, Long voucherId, UpdateVoucherRequest req) {
        Voucher v = getOwnedVoucher(sellerUserId, voucherId);
        if (req.getVoucherName() != null) v.setVoucherName(req.getVoucherName());
        if (req.getDiscountValue() != null) v.setDiscountValue(req.getDiscountValue());
        if (req.getMaxDiscountAmount() != null) v.setMaxDiscountAmount(req.getMaxDiscountAmount());
        if (req.getMinOrderValue() != null) v.setMinOrderValue(req.getMinOrderValue());
        if (req.getUsageLimit() != null) v.setUsageLimit(req.getUsageLimit());
        if (req.getUserUsageLimit() != null) v.setUserUsageLimit(req.getUserUsageLimit());
        if (req.getMemberRankId() != null) v.setMemberRankId(req.getMemberRankId());
        if (req.getStartDate() != null) v.setStartDate(req.getStartDate());
        if (req.getEndDate() != null) v.setEndDate(req.getEndDate());
        if (req.getStatus() != null) v.setStatus(req.getStatus());
        validateDateRange(v.getStartDate(), v.getEndDate());
        v = voucherRepository.save(v);
        return VoucherResponse.from(v);
    }

    @Transactional
    public void deactivateVoucher(Long sellerUserId, Long voucherId) {
        Voucher v = getOwnedVoucher(sellerUserId, voucherId);
        v.setStatus(VoucherStatus.INACTIVE);
        voucherRepository.save(v);
    }

    public PageResponse<VoucherResponse> getStoreVouchers(Long sellerUserId, Pageable pageable) {
        Long storeId = storeResolver.resolveStoreId(sellerUserId);
        return PageResponse.from(
            voucherRepository.findByStoreIdOrderByCreatedAtDesc(storeId, pageable).map(VoucherResponse::from));
    }

    // ========== Customer ==========

    public List<VoucherResponse> getAvailableForStore(Long storeId) {
        return voucherRepository.findAvailableForStore(storeId, VoucherStatus.ACTIVE, LocalDateTime.now())
            .stream().map(VoucherResponse::from).toList();
    }

    @Transactional
    public void saveVoucher(Long userId, Long voucherId) {
        Voucher v = voucherRepository.findById(voucherId)
            .orElseThrow(() -> new ResourceNotFoundException("Voucher không tồn tại"));
        if (v.getStatus() != VoucherStatus.ACTIVE) {
            throw new BusinessException("Voucher không còn hiệu lực");
        }
        if (userVoucherRepository.existsByUserIdAndVoucherId(userId, voucherId)) {
            return; // đã lưu rồi
        }
        UserVoucher uv = new UserVoucher();
        uv.setUserId(userId);
        uv.setVoucherId(voucherId);
        userVoucherRepository.save(uv);
    }

    public List<VoucherResponse> getMyVouchers(Long userId) {
        return userVoucherRepository.findByUserIdOrderBySavedAtDesc(userId).stream()
            .map(uv -> voucherRepository.findById(uv.getVoucherId()).orElse(null))
            .filter(java.util.Objects::nonNull)
            .map(VoucherResponse::from)
            .toList();
    }

    // ========== Checkout integration ==========

    /**
     * Kiểm tra & tính giảm giá cho một đơn của gian hàng cụ thể.
     *
     * @return {@link VoucherDiscount} nếu voucher áp dụng được cho store này;
     *         {@code null} nếu voucher thuộc gian hàng khác (bỏ qua, không lỗi).
     * @throws BusinessException nếu voucher không hợp lệ (hết hạn, chưa đạt đơn tối thiểu, hết lượt...).
     */
    public VoucherDiscount previewDiscount(String code, Long userId, Long storeId,
                                           BigDecimal subtotal, BigDecimal shippingFee) {
        Voucher v = voucherRepository.findByVoucherCode(code)
            .orElseThrow(() -> new BusinessException("Voucher không tồn tại: " + code));

        // Voucher của gian hàng khác → không áp dụng cho đơn này (bỏ qua)
        if (v.getStoreId() != null && !v.getStoreId().equals(storeId)) {
            return null;
        }

        LocalDateTime now = LocalDateTime.now();
        if (v.getStatus() != VoucherStatus.ACTIVE) {
            throw new BusinessException("Voucher không còn hiệu lực");
        }
        if (now.isBefore(v.getStartDate()) || now.isAfter(v.getEndDate())) {
            throw new BusinessException("Voucher không trong thời gian áp dụng");
        }
        if (v.getMinOrderValue() != null && subtotal.compareTo(v.getMinOrderValue()) < 0) {
            throw new BusinessException("Đơn hàng chưa đạt giá trị tối thiểu để dùng voucher: " + v.getMinOrderValue());
        }
        if (v.getUsageLimit() != null && nz(v.getUsageCount()) >= v.getUsageLimit()) {
            throw new BusinessException("Voucher đã hết lượt sử dụng");
        }
        long userUsed = voucherUsageRepository.countByVoucherIdAndUserId(v.getId(), userId);
        if (userUsed >= nz(v.getUserUsageLimit())) {
            throw new BusinessException("Bạn đã dùng hết lượt voucher này");
        }
        if (v.getMemberRankId() != null) {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));
            if (user.getMemberRankId() == null || !user.getMemberRankId().equals(v.getMemberRankId())) {
                throw new BusinessException("Voucher chỉ dành cho hạng thành viên nhất định");
            }
        }

        BigDecimal discount = calcDiscount(v, subtotal, shippingFee != null ? shippingFee : BigDecimal.ZERO);
        return new VoucherDiscount(v.getId(), v.getVoucherCode(), discount);
    }

    /** Ghi nhận sử dụng voucher sau khi đơn đã được tạo. */
    @Transactional
    public void commitUsage(Long voucherId, String code, Long userId, Long orderId, BigDecimal amount) {
        Voucher v = voucherRepository.findById(voucherId)
            .orElseThrow(() -> new ResourceNotFoundException("Voucher không tồn tại"));
        v.setUsageCount(nz(v.getUsageCount()) + 1);
        voucherRepository.save(v);

        VoucherUsage usage = new VoucherUsage();
        usage.setVoucherId(voucherId);
        usage.setUserId(userId);
        usage.setOrderId(orderId);
        usage.setDiscountAmount(amount);
        voucherUsageRepository.save(usage);

        OrderVoucher ov = new OrderVoucher();
        ov.setOrderId(orderId);
        ov.setVoucherId(voucherId);
        ov.setVoucherCode(code);
        ov.setDiscountAmount(amount);
        orderVoucherRepository.save(ov);

        userVoucherRepository.findByUserIdAndVoucherId(userId, voucherId).ifPresent(uv -> {
            uv.setUsageCount(nz(uv.getUsageCount()) + 1);
            userVoucherRepository.save(uv);
        });
        log.info("Voucher {} used by user {} for order {} (-{})", code, userId, orderId, amount);
    }

    // ========== Scheduled ==========

    @Transactional
    public int expireOverdueVouchers() {
        int n = voucherRepository.expireOverdue(VoucherStatus.ACTIVE, VoucherStatus.EXPIRED, LocalDateTime.now());
        if (n > 0) log.info("Expired {} voucher(s)", n);
        return n;
    }

    // ========== Helpers ==========

    private BigDecimal calcDiscount(Voucher v, BigDecimal subtotal, BigDecimal shippingFee) {
        BigDecimal base = v.getVoucherType() == VoucherType.SHIPPING_DISCOUNT ? shippingFee : subtotal;
        BigDecimal discount;
        if (v.getDiscountType() == DiscountType.PERCENTAGE) {
            discount = base.multiply(v.getDiscountValue())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            if (v.getMaxDiscountAmount() != null) {
                discount = discount.min(v.getMaxDiscountAmount());
            }
        } else { // FIXED_AMOUNT
            discount = v.getDiscountValue();
        }
        return discount.min(base).max(BigDecimal.ZERO);
    }

    private Voucher getOwnedVoucher(Long sellerUserId, Long voucherId) {
        Long storeId = storeResolver.resolveStoreId(sellerUserId);
        Voucher v = voucherRepository.findById(voucherId)
            .orElseThrow(() -> new ResourceNotFoundException("Voucher không tồn tại"));
        if (v.getStoreId() == null || !v.getStoreId().equals(storeId)) {
            throw new BusinessException("Bạn không có quyền quản lý voucher này");
        }
        return v;
    }

    private void validateDateRange(LocalDateTime start, LocalDateTime end) {
        if (start != null && end != null && end.isBefore(start)) {
            throw new BadRequestException("Ngày kết thúc phải sau ngày bắt đầu");
        }
    }

    private int nz(Integer value) {
        return value != null ? value : 0;
    }
}
