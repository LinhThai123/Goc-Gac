package com.ecommerce.gocgac.repository;

import com.ecommerce.gocgac.entity.UserVoucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserVoucherRepository extends JpaRepository<UserVoucher, Long> {

    Optional<UserVoucher> findByUserIdAndVoucherId(Long userId, Long voucherId);

    boolean existsByUserIdAndVoucherId(Long userId, Long voucherId);

    List<UserVoucher> findByUserIdOrderBySavedAtDesc(Long userId);
}
