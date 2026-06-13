package com.ecommerce.gocgac.service.loyalty;

import com.ecommerce.gocgac.common.response.PageResponse;
import com.ecommerce.gocgac.dto.loyalty.LoyaltyConfigResponse;
import com.ecommerce.gocgac.dto.loyalty.LoyaltyInfoResponse;
import com.ecommerce.gocgac.dto.loyalty.LoyaltyTransactionResponse;
import com.ecommerce.gocgac.dto.loyalty.UpsertLoyaltyConfigRequest;
import com.ecommerce.gocgac.entity.LoyaltyConfig;
import com.ecommerce.gocgac.entity.LoyaltyTransaction;
import com.ecommerce.gocgac.entity.MemberRank;
import com.ecommerce.gocgac.entity.User;
import com.ecommerce.gocgac.entity.UserRankHistory;
import com.ecommerce.gocgac.entity.enums.OrderStatus;
import com.ecommerce.gocgac.entity.enums.TransactionType;
import com.ecommerce.gocgac.exception.BusinessException;
import com.ecommerce.gocgac.exception.ResourceNotFoundException;
import com.ecommerce.gocgac.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Dịch vụ Khách hàng thân thiết (Sprint 6 - M13).
 *
 * <p>Tích điểm theo tỉ lệ chi tiêu (config {@code EARN_RATE} — số VND cho 1 điểm),
 * đổi điểm khi đặt hàng (config {@code REDEEM_RATE} — số VND giảm cho 1 điểm),
 * và tự động nâng hạng thành viên theo tổng chi tiêu trong kỳ.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoyaltyService {

    private final LoyaltyConfigRepository loyaltyConfigRepository;
    private final LoyaltyTransactionRepository loyaltyTransactionRepository;
    private final MemberRankRepository memberRankRepository;
    private final UserRankHistoryRepository userRankHistoryRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    private static final String EARN_RATE = "EARN_RATE";       // VND chi tiêu cho 1 điểm
    private static final String REDEEM_RATE = "REDEEM_RATE";   // VND giảm cho 1 điểm khi đổi
    private static final int DEFAULT_EARN_RATE = 1000;
    private static final int DEFAULT_REDEEM_RATE = 1000;
    private static final String REF_ORDER = "ORDER";
    private static final String RANK_ACTIVE = "active";

    /** Kết quả đổi điểm để OrderService dùng khi checkout. */
    public record RedeemResult(int pointsUsed, BigDecimal discount) {}

    // ========== Earn / Redeem (gọi từ OrderService) ==========

    /** Tích điểm khi đơn hoàn tất (DELIVERED). Trả về số điểm đã cộng. */
    @Transactional
    public int earnForOrder(Long userId, Long orderId, BigDecimal orderTotal) {
        int rate = getConfigValue(EARN_RATE, DEFAULT_EARN_RATE);
        if (rate <= 0 || orderTotal == null) return 0;
        int points = orderTotal.divideToIntegralValue(BigDecimal.valueOf(rate)).intValue();
        if (points <= 0) return 0;

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));
        user.setLoyaltyPoints(nz(user.getLoyaltyPoints()) + points);
        userRepository.save(user);

        record(userId, TransactionType.EARN, points, "ORDER_COMPLETED", orderId,
            "Tích điểm cho đơn hàng hoàn tất");
        log.info("User {} earned {} points from order {}", userId, points, orderId);
        return points;
    }

    /**
     * Tính số điểm đổi được và số tiền giảm tương ứng (không trừ điểm).
     *
     * @param redeemableBase mức tiền tối đa được giảm (vd subtotal − voucher)
     * @throws BusinessException nếu không đủ điểm
     */
    public RedeemResult previewRedeem(Long userId, Integer pointsRequested, BigDecimal redeemableBase) {
        if (pointsRequested == null || pointsRequested <= 0) {
            return new RedeemResult(0, BigDecimal.ZERO);
        }
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));
        int available = nz(user.getLoyaltyPoints());
        if (pointsRequested > available) {
            throw new BusinessException("Không đủ điểm để đổi. Bạn đang có " + available + " điểm");
        }
        int rate = getConfigValue(REDEEM_RATE, DEFAULT_REDEEM_RATE);
        if (rate <= 0) return new RedeemResult(0, BigDecimal.ZERO);

        int maxByBase = redeemableBase.divideToIntegralValue(BigDecimal.valueOf(rate)).intValue();
        int pointsUsed = Math.min(pointsRequested, Math.max(0, maxByBase));
        BigDecimal discount = BigDecimal.valueOf((long) pointsUsed * rate);
        return new RedeemResult(pointsUsed, discount);
    }

    /** Trừ điểm đã đổi sau khi đơn được tạo. */
    @Transactional
    public void commitRedeem(Long userId, int pointsUsed, Long orderId) {
        if (pointsUsed <= 0) return;
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));
        if (nz(user.getLoyaltyPoints()) < pointsUsed) {
            throw new BusinessException("Không đủ điểm để đổi");
        }
        user.setLoyaltyPoints(nz(user.getLoyaltyPoints()) - pointsUsed);
        userRepository.save(user);
        record(userId, TransactionType.REDEEM, pointsUsed, "REDEEM", orderId,
            "Đổi điểm giảm giá đơn hàng");
        log.info("User {} redeemed {} points for order {}", userId, pointsUsed, orderId);
    }

    // ========== Member rank ==========

    /** Tính lại hạng thành viên theo tổng chi tiêu trong kỳ; nâng hạng nếu đạt. */
    @Transactional
    public void recalculateRank(Long userId) {
        List<MemberRank> ranks = memberRankRepository.findByStatusOrderByMinSpendingDesc(RANK_ACTIVE);
        LocalDateTime now = LocalDateTime.now();

        for (MemberRank rank : ranks) {
            LocalDateTime since = now.minusMonths(rank.getPeriodMonths() != null ? rank.getPeriodMonths() : 12);
            BigDecimal spending = orderRepository.sumSpendingSince(userId, OrderStatus.DELIVERED, since);
            if (spending.compareTo(rank.getMinSpending()) >= 0) {
                User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));
                if (user.getMemberRankId() == null || !user.getMemberRankId().equals(rank.getId())) {
                    user.setMemberRankId(rank.getId());
                    userRepository.save(user);

                    UserRankHistory history = new UserRankHistory();
                    history.setUserId(userId);
                    history.setRankId(rank.getId());
                    history.setTotalSpending(spending);
                    history.setExpiresAt(LocalDate.now().plusMonths(
                        rank.getPeriodMonths() != null ? rank.getPeriodMonths() : 12));
                    userRankHistoryRepository.save(history);
                    log.info("User {} reached rank {} (spending {})", userId, rank.getRankName(), spending);
                }
                return; // đã gán hạng cao nhất đạt được
            }
        }
    }

    // ========== Customer info ==========

    public LoyaltyInfoResponse getMyInfo(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));
        String rankName = null;
        if (user.getMemberRankId() != null) {
            rankName = memberRankRepository.findById(user.getMemberRankId())
                .map(MemberRank::getRankName).orElse(null);
        }
        return LoyaltyInfoResponse.builder()
            .points(nz(user.getLoyaltyPoints()))
            .memberRankId(user.getMemberRankId())
            .memberRankName(rankName)
            .build();
    }

    public PageResponse<LoyaltyTransactionResponse> getMyTransactions(Long userId, Pageable pageable) {
        return PageResponse.from(
            loyaltyTransactionRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(LoyaltyTransactionResponse::from));
    }

    // ========== Config (admin) ==========

    @Transactional
    public LoyaltyConfigResponse upsertConfig(UpsertLoyaltyConfigRequest req) {
        LoyaltyConfig config = loyaltyConfigRepository.findByEventType(req.getEventType())
            .orElseGet(LoyaltyConfig::new);
        config.setEventType(req.getEventType());
        config.setEventName(req.getEventName());
        config.setPointsAwarded(req.getPointsAwarded());
        config.setIsActive(req.getIsActive() != null ? req.getIsActive() : true);
        config.setDescription(req.getDescription());
        config = loyaltyConfigRepository.save(config);
        return LoyaltyConfigResponse.from(config);
    }

    public List<LoyaltyConfigResponse> listConfigs() {
        return loyaltyConfigRepository.findAll().stream().map(LoyaltyConfigResponse::from).toList();
    }

    // ========== Helpers ==========

    private int getConfigValue(String eventType, int defaultValue) {
        return loyaltyConfigRepository.findByEventType(eventType)
            .filter(c -> Boolean.TRUE.equals(c.getIsActive()))
            .map(LoyaltyConfig::getPointsAwarded)
            .orElse(defaultValue);
    }

    private void record(Long userId, TransactionType type, int points, String eventType,
                        Long referenceId, String description) {
        LoyaltyTransaction t = new LoyaltyTransaction();
        t.setUserId(userId);
        t.setTransactionType(type);
        t.setPoints(points);
        t.setEventType(eventType);
        t.setReferenceType(REF_ORDER);
        t.setReferenceId(referenceId);
        t.setDescription(description);
        loyaltyTransactionRepository.save(t);
    }

    private int nz(Integer value) {
        return value != null ? value : 0;
    }
}
