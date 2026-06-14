package com.ecommerce.gocgac.service.ad;

import com.ecommerce.gocgac.entity.AdCampaign;
import com.ecommerce.gocgac.entity.Product;
import com.ecommerce.gocgac.entity.enums.ApprovalStatus;
import com.ecommerce.gocgac.repository.AdCampaignRepository;
import com.ecommerce.gocgac.repository.AdClickRepository;
import com.ecommerce.gocgac.repository.ProductRepository;
import com.ecommerce.gocgac.service.notification.NotificationService;
import com.ecommerce.gocgac.service.store.StoreResolver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdCampaignServiceTest {

    @Mock private AdCampaignRepository campaignRepository;
    @Mock private AdClickRepository adClickRepository;
    @Mock private ProductRepository productRepository;
    @Mock private StoreResolver storeResolver;
    @Mock private NotificationService notificationService;

    @InjectMocks private AdCampaignService adCampaignService;

    private static final Long CAMPAIGN_ID = 50L;

    private AdCampaign campaign(String budget, String cpc, ApprovalStatus approval, String status) {
        AdCampaign c = new AdCampaign();
        c.setId(CAMPAIGN_ID);
        c.setStoreId(1L);
        c.setProductId(100L);
        c.setApprovalStatus(approval);
        c.setStatus(status);
        c.setStartDate(LocalDateTime.now().minusDays(1));
        c.setEndDate(LocalDateTime.now().plusDays(1));
        c.setBudget(new BigDecimal(budget));
        c.setCostPerClick(new BigDecimal(cpc));
        c.setTotalClicks(0);
        c.setTotalSpent(BigDecimal.ZERO);
        return c;
    }

    private void stubProduct() {
        Product p = new Product();
        p.setId(100L);
        p.setSlug("ao-thun");
        when(productRepository.findById(100L)).thenReturn(Optional.of(p));
    }

    @Test
    @DisplayName("Click hợp lệ: tăng click, trừ ngân sách theo CPC")
    void trackClick_deductsBudget() {
        AdCampaign c = campaign("100000", "5000", ApprovalStatus.APPROVED, "active");
        when(campaignRepository.findById(CAMPAIGN_ID)).thenReturn(Optional.of(c));
        stubProduct();

        String target = adCampaignService.trackClick(CAMPAIGN_ID, 7L, "1.2.3.4", "UA");

        assertThat(c.getTotalClicks()).isEqualTo(1);
        assertThat(c.getTotalSpent()).isEqualByComparingTo("5000");
        assertThat(c.getStatus()).isEqualTo("active");
        assertThat(target).contains("/san-pham/ao-thun");
        verify(adClickRepository).save(any());
    }

    @Test
    @DisplayName("Hết ngân sách sau click → status completed")
    void trackClick_budgetExhausted_completes() {
        AdCampaign c = campaign("5000", "5000", ApprovalStatus.APPROVED, "active");
        when(campaignRepository.findById(CAMPAIGN_ID)).thenReturn(Optional.of(c));
        stubProduct();

        adCampaignService.trackClick(CAMPAIGN_ID, 7L, "1.2.3.4", "UA");

        assertThat(c.getTotalSpent()).isEqualByComparingTo("5000");
        assertThat(c.getStatus()).isEqualTo("completed");
    }

    @Test
    @DisplayName("Chiến dịch chưa duyệt → không tính phí, không ghi click")
    void trackClick_notApproved_noCharge() {
        AdCampaign c = campaign("100000", "5000", ApprovalStatus.DRAFT, "scheduled");
        when(campaignRepository.findById(CAMPAIGN_ID)).thenReturn(Optional.of(c));
        stubProduct();

        adCampaignService.trackClick(CAMPAIGN_ID, 7L, "1.2.3.4", "UA");

        assertThat(c.getTotalClicks()).isZero();
        assertThat(c.getTotalSpent()).isEqualByComparingTo("0");
        verify(adClickRepository, never()).save(any());
        verify(campaignRepository, never()).save(any());
    }
}
