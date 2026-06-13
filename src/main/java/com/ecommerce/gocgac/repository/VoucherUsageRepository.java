package com.ecommerce.gocgac.repository;

import com.ecommerce.gocgac.entity.VoucherUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VoucherUsageRepository extends JpaRepository<VoucherUsage, Long> {

    long countByVoucherIdAndUserId(Long voucherId, Long userId);
}
