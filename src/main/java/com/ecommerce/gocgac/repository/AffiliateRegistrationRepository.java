package com.ecommerce.gocgac.repository;

import com.ecommerce.gocgac.entity.AffiliateRegistration;
import com.ecommerce.gocgac.entity.enums.ApprovalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AffiliateRegistrationRepository extends JpaRepository<AffiliateRegistration, Long> {

    Optional<AffiliateRegistration> findByUserId(Long userId);

    boolean existsByUserIdAndStatus(Long userId, ApprovalStatus status);

    Page<AffiliateRegistration> findByStatusOrderBySubmittedAtDesc(ApprovalStatus status, Pageable pageable);
}
