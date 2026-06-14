package com.ecommerce.gocgac.repository;

import com.ecommerce.gocgac.entity.AdClick;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdClickRepository extends JpaRepository<AdClick, Long> {
}
