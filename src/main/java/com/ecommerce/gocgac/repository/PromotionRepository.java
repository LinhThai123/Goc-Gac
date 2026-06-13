package com.ecommerce.gocgac.repository;

import com.ecommerce.gocgac.entity.Promotion;
import com.ecommerce.gocgac.entity.enums.PromotionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    Page<Promotion> findByStoreIdOrderByCreatedAtDesc(Long storeId, Pageable pageable);

    /** Kích hoạt khuyến mãi đã đến hạn (SCHEDULED → ACTIVE). */
    @Modifying
    @Query("UPDATE Promotion p SET p.status = :active " +
           "WHERE p.status = :scheduled AND p.startDate <= :now AND p.endDate >= :now")
    int activateDue(@Param("scheduled") PromotionStatus scheduled,
                    @Param("active") PromotionStatus active,
                    @Param("now") LocalDateTime now);

    /** Hết hạn khuyến mãi (SCHEDULED/ACTIVE → EXPIRED khi quá end_date). */
    @Modifying
    @Query("UPDATE Promotion p SET p.status = :expired " +
           "WHERE p.status IN (:scheduled, :active) AND p.endDate < :now")
    int expireOverdue(@Param("scheduled") PromotionStatus scheduled,
                      @Param("active") PromotionStatus active,
                      @Param("expired") PromotionStatus expired,
                      @Param("now") LocalDateTime now);
}
