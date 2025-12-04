package com.ecommerce.gocgac.entity;

import com.ecommerce.gocgac.entity.enums.ApprovalStatus;
import com.ecommerce.gocgac.entity.enums.BusinessType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "seller_registrations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SellerRegistration {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "business_type", nullable = false)
    private BusinessType businessType;
    
    @Column(name = "business_name", nullable = false)
    private String businessName;
    
    @Column(name = "tax_code", length = 50)
    private String taxCode;
    
    @Column(name = "identity_document", columnDefinition = "TEXT")
    private String identityDocument;
    
    @Column(name = "business_license", columnDefinition = "TEXT")
    private String businessLicense;
    
    @Column(name = "bank_account", length = 50)
    private String bankAccount;
    
    @Column(name = "bank_name", length = 100)
    private String bankName;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApprovalStatus status = ApprovalStatus.PENDING;
    
    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;
    
    @Column(name = "submitted_at", nullable = false, updatable = false)
    private LocalDateTime submittedAt = LocalDateTime.now();
    
    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;
    
    @Column(name = "reviewed_by")
    private Long reviewedBy;
}

