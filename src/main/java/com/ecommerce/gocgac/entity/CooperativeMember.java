package com.ecommerce.gocgac.entity;

import com.ecommerce.gocgac.entity.enums.ApprovalStatus;
import com.ecommerce.gocgac.entity.enums.CooperativeMemberRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity lưu thông tin thành viên HTX (Seller đăng ký vào HTX)
 */
@Entity
@Table(name = "cooperative_members", indexes = {
    @Index(name = "idx_cooperative_members_cooperative", columnList = "cooperative_id"),
    @Index(name = "idx_cooperative_members_user", columnList = "user_id"),
    @Index(name = "idx_cooperative_members_status", columnList = "status"),
    @Index(name = "idx_cooperative_members_unique", columnList = "cooperative_id,user_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CooperativeMember {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "cooperative_id", nullable = false)
    private Long cooperativeId; // ID của HTX
    
    @Column(name = "user_id", nullable = false)
    private Long userId; // ID của User (CUSTOMER hoặc SELLER) - Thành viên HTX
    
    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 50)
    private CooperativeMemberRole role = CooperativeMemberRole.MEMBER; // Vai trò trong HTX
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApprovalStatus status = ApprovalStatus.PENDING; // PENDING, APPROVED, REJECTED
    
    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason; // Lý do từ chối (nếu bị reject)
    
    @Column(name = "joined_at")
    private LocalDateTime joinedAt; // Thời gian được approve và tham gia HTX
    
    @Column(name = "applied_at", nullable = false, updatable = false)
    private LocalDateTime appliedAt = LocalDateTime.now(); // Thời gian đăng ký
    
    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt; // Thời gian được review
    
    @Column(name = "reviewed_by")
    private Long reviewedBy; // ID của người review (COOPERATIVE_MANAGER)
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

