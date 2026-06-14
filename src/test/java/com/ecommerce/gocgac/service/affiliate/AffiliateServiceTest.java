package com.ecommerce.gocgac.service.affiliate;

import com.ecommerce.gocgac.entity.*;
import com.ecommerce.gocgac.entity.enums.CommissionStatus;
import com.ecommerce.gocgac.repository.*;
import com.ecommerce.gocgac.service.notification.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AffiliateServiceTest {

    @Mock private AffiliateRegistrationRepository registrationRepository;
    @Mock private AffiliatePartnerRepository partnerRepository;
    @Mock private AffiliateLinkRepository linkRepository;
    @Mock private AffiliateClickRepository clickRepository;
    @Mock private AffiliateCommissionRepository commissionRepository;
    @Mock private ProductCommissionRepository productCommissionRepository;
    @Mock private ProductRepository productRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private NotificationService notificationService;

    @InjectMocks private AffiliateService affiliateService;

    private static final Long BUYER_ID = 7L;
    private static final Long PARTNER_USER_ID = 99L;
    private static final Long ORDER_ID = 500L;

    private Order order;
    private AffiliatePartner partner;

    @BeforeEach
    void setUp() {
        order = new Order();
        order.setId(ORDER_ID);
        order.setSubtotal(new BigDecimal("200000"));

        partner = new AffiliatePartner();
        partner.setId(11L);
        partner.setUserId(PARTNER_USER_ID);
        partner.setAffiliateCode("AFF1");
        partner.setCommissionRate(new BigDecimal("5.00")); // fallback 5%
        partner.setStatus("active");
        partner.setTotalClicks(0);
        partner.setTotalOrders(0);
        partner.setTotalRevenue(BigDecimal.ZERO);
        partner.setTotalCommission(BigDecimal.ZERO);
    }

    private AffiliateClick click(Long linkId) {
        AffiliateClick c = new AffiliateClick();
        c.setLinkId(linkId);
        c.setUserId(BUYER_ID);
        return c;
    }

    private AffiliateLink link() {
        AffiliateLink l = new AffiliateLink();
        l.setId(22L);
        l.setPartnerId(11L);
        l.setConversionCount(0);
        return l;
    }

    private OrderItem item(Long productId, String subtotal) {
        OrderItem i = new OrderItem();
        i.setProductId(productId);
        i.setSubtotal(new BigDecimal(subtotal));
        return i;
    }

    private ProductCommission pc(BigDecimal rate) {
        ProductCommission p = new ProductCommission();
        p.setCommissionRate(rate);
        p.setIsActive(true);
        return p;
    }

    @Test
    @DisplayName("Quy gán đơn: hoa hồng tính theo từng sản phẩm (10% + fallback 5%)")
    void attributeOrder_perProductCommission() {
        when(commissionRepository.existsByOrderId(ORDER_ID)).thenReturn(false);
        when(clickRepository.findFirstByUserIdAndClickedAtAfterOrderByClickedAtDesc(eq(BUYER_ID), any(LocalDateTime.class)))
            .thenReturn(Optional.of(click(22L)));
        when(linkRepository.findById(22L)).thenReturn(Optional.of(link()));
        when(partnerRepository.findById(11L)).thenReturn(Optional.of(partner));
        when(orderItemRepository.findByOrderId(ORDER_ID)).thenReturn(List.of(
            item(100L, "100000"), item(101L, "100000")));
        when(productCommissionRepository.findByProductId(100L)).thenReturn(Optional.of(pc(new BigDecimal("10.00"))));
        when(productCommissionRepository.findByProductId(101L)).thenReturn(Optional.empty()); // fallback 5%
        when(commissionRepository.save(any(AffiliateCommission.class))).thenAnswer(inv -> inv.getArgument(0));

        affiliateService.attributeOrder(order, BUYER_ID);

        ArgumentCaptor<AffiliateCommission> captor = ArgumentCaptor.forClass(AffiliateCommission.class);
        verify(commissionRepository).save(captor.capture());
        AffiliateCommission saved = captor.getValue();
        // 100000*10% + 100000*5% = 15000
        assertThat(saved.getCommissionAmount()).isEqualByComparingTo("15000");
        assertThat(saved.getCommissionRate()).isEqualByComparingTo("7.50"); // 15000/200000*100
        assertThat(saved.getStatus()).isEqualTo(CommissionStatus.PENDING);
        assertThat(order.getAffiliateCode()).isEqualTo("AFF1");
        assertThat(partner.getTotalOrders()).isEqualTo(1);
        assertThat(partner.getTotalRevenue()).isEqualByComparingTo("200000");
    }

    @Test
    @DisplayName("Tự mua (đối tác == người mua) → không tạo hoa hồng")
    void attributeOrder_selfPurchase_skips() {
        when(commissionRepository.existsByOrderId(ORDER_ID)).thenReturn(false);
        when(clickRepository.findFirstByUserIdAndClickedAtAfterOrderByClickedAtDesc(eq(PARTNER_USER_ID), any(LocalDateTime.class)))
            .thenReturn(Optional.of(click(22L)));
        when(linkRepository.findById(22L)).thenReturn(Optional.of(link()));
        when(partnerRepository.findById(11L)).thenReturn(Optional.of(partner));

        affiliateService.attributeOrder(order, PARTNER_USER_ID); // người mua chính là đối tác

        verify(commissionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Không có click trong cửa sổ → không quy gán")
    void attributeOrder_noClick_skips() {
        when(commissionRepository.existsByOrderId(ORDER_ID)).thenReturn(false);
        when(clickRepository.findFirstByUserIdAndClickedAtAfterOrderByClickedAtDesc(eq(BUYER_ID), any(LocalDateTime.class)))
            .thenReturn(Optional.empty());

        affiliateService.attributeOrder(order, BUYER_ID);

        verify(commissionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Hủy/hoàn đơn: hoa hồng PENDING → CANCELLED, trừ thống kê đối tác")
    void voidCommission_cancels() {
        AffiliateCommission c = new AffiliateCommission();
        c.setId(1L);
        c.setPartnerId(11L);
        c.setOrderId(ORDER_ID);
        c.setOrderAmount(new BigDecimal("200000"));
        c.setCommissionAmount(new BigDecimal("15000"));
        c.setStatus(CommissionStatus.PENDING);
        partner.setTotalOrders(1);
        partner.setTotalRevenue(new BigDecimal("200000"));
        when(commissionRepository.findByOrderId(ORDER_ID)).thenReturn(List.of(c));
        when(partnerRepository.findById(11L)).thenReturn(Optional.of(partner));

        affiliateService.voidCommissionForOrder(ORDER_ID);

        assertThat(c.getStatus()).isEqualTo(CommissionStatus.CANCELLED);
        assertThat(partner.getTotalOrders()).isZero();
        assertThat(partner.getTotalRevenue()).isEqualByComparingTo("0");
    }
}
