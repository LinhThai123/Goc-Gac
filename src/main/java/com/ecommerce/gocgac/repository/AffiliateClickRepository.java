package com.ecommerce.gocgac.repository;

import com.ecommerce.gocgac.entity.AffiliateClick;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface AffiliateClickRepository extends JpaRepository<AffiliateClick, Long> {

    /** Click gần nhất của người dùng còn trong cửa sổ quy gán (last-click attribution). */
    Optional<AffiliateClick> findFirstByUserIdAndClickedAtAfterOrderByClickedAtDesc(Long userId, LocalDateTime after);
}
