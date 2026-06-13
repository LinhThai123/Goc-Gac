package com.ecommerce.gocgac.repository;

import com.ecommerce.gocgac.entity.OrderItem;
import com.ecommerce.gocgac.entity.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderId(Long orderId);

    @Query("SELECT DISTINCT oi.orderId FROM OrderItem oi WHERE oi.storeId = :storeId")
    List<Long> findDistinctOrderIdsByStoreId(@Param("storeId") Long storeId);

    boolean existsByOrderIdAndStoreId(Long orderId, Long storeId);

    /** Kiểm tra người dùng đã mua (và nhận) sản phẩm chưa — để xác thực đánh giá. */
    @Query("SELECT CASE WHEN COUNT(oi) > 0 THEN true ELSE false END " +
           "FROM OrderItem oi, Order o " +
           "WHERE oi.orderId = o.id AND o.userId = :userId " +
           "AND oi.productId = :productId AND o.orderStatus = :status")
    boolean hasUserPurchasedProduct(@Param("userId") Long userId,
                                    @Param("productId") Long productId,
                                    @Param("status") OrderStatus status);
}
