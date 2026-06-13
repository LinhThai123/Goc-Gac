package com.ecommerce.gocgac.repository;

import com.ecommerce.gocgac.entity.LoyaltyConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoyaltyConfigRepository extends JpaRepository<LoyaltyConfig, Long> {

    Optional<LoyaltyConfig> findByEventType(String eventType);

    boolean existsByEventType(String eventType);
}
