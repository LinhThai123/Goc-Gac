package com.ecommerce.gocgac.repository;

import com.ecommerce.gocgac.entity.AffiliateCommission;
import com.ecommerce.gocgac.entity.enums.CommissionStatus;
import com.ecommerce.gocgac.entity.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AffiliateCommissionRepository extends JpaRepository<AffiliateCommission, Long> {

    Page<AffiliateCommission> findByPartnerIdOrderByCreatedAtDesc(Long partnerId, Pageable pageable);

    Page<AffiliateCommission> findByStatusOrderByCreatedAtDesc(CommissionStatus status, Pageable pageable);

    boolean existsByOrderId(Long orderId);

    List<AffiliateCommission> findByOrderId(Long orderId);

    /** Hoa hồng PENDING của các đơn đã giao quá hạn cooling-off → đủ điều kiện duyệt. */
    @Query("SELECT c FROM AffiliateCommission c, Order o " +
           "WHERE c.orderId = o.id AND c.status = :pending " +
           "AND o.orderStatus = :delivered AND o.deliveredAt <= :threshold")
    List<AffiliateCommission> findApprovable(@Param("pending") CommissionStatus pending,
                                             @Param("delivered") OrderStatus delivered,
                                             @Param("threshold") LocalDateTime threshold);
}
