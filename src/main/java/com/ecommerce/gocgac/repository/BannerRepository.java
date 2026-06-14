package com.ecommerce.gocgac.repository;

import com.ecommerce.gocgac.entity.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BannerRepository extends JpaRepository<Banner, Long> {

    /** Banner đang hiển thị tại một vị trí: active và trong khoảng thời gian (nếu có). */
    @Query("SELECT b FROM Banner b WHERE b.bannerPosition = :position AND b.isActive = true " +
           "AND (b.startDate IS NULL OR b.startDate <= :now) " +
           "AND (b.endDate IS NULL OR b.endDate >= :now) " +
           "ORDER BY b.displayOrder ASC")
    List<Banner> findActive(@Param("position") String position, @Param("now") LocalDateTime now);

    List<Banner> findAllByOrderByDisplayOrderAsc();
}
