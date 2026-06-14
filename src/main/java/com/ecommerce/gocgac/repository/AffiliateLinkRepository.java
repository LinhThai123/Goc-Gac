package com.ecommerce.gocgac.repository;

import com.ecommerce.gocgac.entity.AffiliateLink;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AffiliateLinkRepository extends JpaRepository<AffiliateLink, Long> {

    Page<AffiliateLink> findByPartnerIdOrderByCreatedAtDesc(Long partnerId, Pageable pageable);

    Optional<AffiliateLink> findFirstByPartnerIdAndProductId(Long partnerId, Long productId);
}
