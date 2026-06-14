package com.ecommerce.gocgac.repository;

import com.ecommerce.gocgac.entity.AffiliatePartner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AffiliatePartnerRepository extends JpaRepository<AffiliatePartner, Long> {

    Optional<AffiliatePartner> findByUserId(Long userId);

    Optional<AffiliatePartner> findByAffiliateCode(String affiliateCode);

    boolean existsByUserId(Long userId);

    boolean existsByAffiliateCode(String affiliateCode);
}
