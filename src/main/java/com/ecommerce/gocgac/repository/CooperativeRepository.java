package com.ecommerce.gocgac.repository;

import com.ecommerce.gocgac.entity.Cooperative;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CooperativeRepository extends JpaRepository<Cooperative, Long> {
    
    // Tìm cooperative của user
    Optional<Cooperative> findByUserId(Long userId);
    
    // Kiểm tra user đã có cooperative chưa
    boolean existsByUserId(Long userId);
    
    // Tìm cooperative theo slug
    Optional<Cooperative> findBySlug(String slug);
    
    // Tìm cooperative theo cooperativeCode
    Optional<Cooperative> findByCooperativeCode(String cooperativeCode);
    
    // Kiểm tra slug đã tồn tại chưa
    boolean existsBySlug(String slug);
    
    // Kiểm tra cooperativeCode đã tồn tại chưa
    boolean existsByCooperativeCode(String cooperativeCode);
    
    // Kiểm tra taxCode đã tồn tại chưa (nếu có)
    boolean existsByTaxCode(String taxCode);
}

