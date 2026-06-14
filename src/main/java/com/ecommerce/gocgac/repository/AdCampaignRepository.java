package com.ecommerce.gocgac.repository;

import com.ecommerce.gocgac.entity.AdCampaign;
import com.ecommerce.gocgac.entity.enums.ApprovalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AdCampaignRepository extends JpaRepository<AdCampaign, Long> {

    Page<AdCampaign> findByStoreIdOrderByCreatedAtDesc(Long storeId, Pageable pageable);

    Page<AdCampaign> findByApprovalStatusOrderByCreatedAtDesc(ApprovalStatus approvalStatus, Pageable pageable);

    /** Quảng cáo đủ điều kiện hiển thị tại một vị trí: đã duyệt, đang active, trong hạn, còn ngân sách. */
    @Query("SELECT a FROM AdCampaign a WHERE a.adPosition = :position " +
           "AND a.approvalStatus = :approved AND a.status = 'active' " +
           "AND a.startDate <= :now AND a.endDate >= :now " +
           "AND (a.budget IS NULL OR a.totalSpent < a.budget) " +
           "ORDER BY a.totalSpent ASC")
    List<AdCampaign> findServable(@Param("position") String position,
                                  @Param("approved") ApprovalStatus approved,
                                  @Param("now") LocalDateTime now);
}
