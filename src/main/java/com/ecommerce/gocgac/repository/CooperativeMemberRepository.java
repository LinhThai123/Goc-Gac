package com.ecommerce.gocgac.repository;

import com.ecommerce.gocgac.entity.CooperativeMember;
import com.ecommerce.gocgac.entity.enums.ApprovalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CooperativeMemberRepository extends JpaRepository<CooperativeMember, Long> {
    
    // Tìm member theo cooperative và user
    Optional<CooperativeMember> findByCooperativeIdAndUserId(Long cooperativeId, Long userId);
    
    // Kiểm tra user đã là member của cooperative chưa
    boolean existsByCooperativeIdAndUserId(Long cooperativeId, Long userId);
    
    // Tìm tất cả members của một cooperative
    List<CooperativeMember> findByCooperativeId(Long cooperativeId);
    
    // Tìm members của cooperative theo status
    List<CooperativeMember> findByCooperativeIdAndStatus(Long cooperativeId, ApprovalStatus status);
    
    // Tìm members của cooperative theo status với pagination
    Page<CooperativeMember> findByCooperativeIdAndStatus(Long cooperativeId, ApprovalStatus status, Pageable pageable);
    
    // Tìm tất cả cooperative mà user đã tham gia (theo status)
    List<CooperativeMember> findByUserIdAndStatus(Long userId, ApprovalStatus status);
    
    // Đếm số members đã approve của một cooperative
    long countByCooperativeIdAndStatus(Long cooperativeId, ApprovalStatus status);
}

