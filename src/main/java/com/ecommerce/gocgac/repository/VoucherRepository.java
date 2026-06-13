package com.ecommerce.gocgac.repository;

import com.ecommerce.gocgac.entity.Voucher;
import com.ecommerce.gocgac.entity.enums.VoucherStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {

    Optional<Voucher> findByVoucherCode(String voucherCode);

    boolean existsByVoucherCode(String voucherCode);

    Page<Voucher> findByStoreIdOrderByCreatedAtDesc(Long storeId, Pageable pageable);

    @Query("SELECT v FROM Voucher v WHERE (v.storeId = :storeId OR v.storeId IS NULL) " +
           "AND v.status = :active AND v.startDate <= :now AND v.endDate >= :now")
    List<Voucher> findAvailableForStore(@Param("storeId") Long storeId,
                                        @Param("active") VoucherStatus active,
                                        @Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE Voucher v SET v.status = :expired " +
           "WHERE v.status = :active AND v.endDate < :now")
    int expireOverdue(@Param("active") VoucherStatus active,
                      @Param("expired") VoucherStatus expired,
                      @Param("now") LocalDateTime now);
}
