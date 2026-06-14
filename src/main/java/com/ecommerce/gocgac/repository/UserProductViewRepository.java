package com.ecommerce.gocgac.repository;

import com.ecommerce.gocgac.entity.UserProductView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserProductViewRepository extends JpaRepository<UserProductView, Long> {

    List<UserProductView> findTop50ByUserIdOrderByViewedAtDesc(Long userId);
}
