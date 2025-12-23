package com.ecommerce.gocgac.repository;

import com.ecommerce.gocgac.entity.CooperativeRegistration;
import com.ecommerce.gocgac.entity.enums.ApprovalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CooperativeRegistrationRepository extends JpaRepository<CooperativeRegistration, Long> {
    
    // Tìm registration của user
    Optional<CooperativeRegistration> findByUserId(Long userId);
    
    // Tìm registration theo status
    List<CooperativeRegistration> findByStatus(ApprovalStatus status);
    
    // Tìm registration đang pending hoặc draft của user
    Optional<CooperativeRegistration> findByUserIdAndStatusIn(
        Long userId, 
        List<ApprovalStatus> statuses
    );
    
    // Kiểm tra slug đã tồn tại chưa
    boolean existsBySlug(String slug);
    
    // Kiểm tra cooperativeCode đã tồn tại chưa
    boolean existsByCooperativeCode(String cooperativeCode);
    
    // Kiểm tra taxCode đã tồn tại chưa (nếu có)
    boolean existsByTaxCode(String taxCode);
    
    // Tìm registration theo slug
    Optional<CooperativeRegistration> findBySlug(String slug);
    
    // Tìm registration theo cooperativeCode
    Optional<CooperativeRegistration> findByCooperativeCode(String cooperativeCode);
    
    // Tìm registration theo userId và status
    Optional<CooperativeRegistration> findByUserIdAndStatus(Long userId, ApprovalStatus status);
    
    // Tìm registration theo status với pagination
    Page<CooperativeRegistration> findByStatus(ApprovalStatus status, Pageable pageable);
}

