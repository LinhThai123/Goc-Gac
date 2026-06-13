package com.ecommerce.gocgac.repository;

import com.ecommerce.gocgac.entity.Order;
import com.ecommerce.gocgac.entity.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Optional<Order> findByIdAndUserId(Long id, Long userId);

    boolean existsByOrderCode(String orderCode);

    Page<Order> findByIdInOrderByCreatedAtDesc(List<Long> ids, Pageable pageable);

    /** Tổng chi tiêu (đơn đã giao) của người dùng kể từ mốc thời gian — dùng tính hạng thành viên. */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o " +
           "WHERE o.userId = :userId AND o.orderStatus = :status AND o.deliveredAt >= :since")
    BigDecimal sumSpendingSince(@Param("userId") Long userId,
                                @Param("status") OrderStatus status,
                                @Param("since") LocalDateTime since);
}
