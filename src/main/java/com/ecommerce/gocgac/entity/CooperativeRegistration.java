package com.ecommerce.gocgac.entity;

import com.ecommerce.gocgac.entity.enums.ApprovalStatus;
import com.ecommerce.gocgac.entity.enums.BusinessScale;
import com.ecommerce.gocgac.entity.enums.CooperativeScale;
import com.ecommerce.gocgac.entity.enums.CooperativeType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cooperative_registrations", indexes = {
    @Index(name = "idx_cooperative_registrations_user_id", columnList = "user_id"),
    @Index(name = "idx_cooperative_registrations_status", columnList = "status"),
    @Index(name = "idx_cooperative_registrations_slug", columnList = "slug"),
    @Index(name = "idx_cooperative_registrations_code", columnList = "cooperative_code")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CooperativeRegistration {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    // ========== STEP 1: Thông tin HTX cơ bản ==========
    @Column(name = "cooperative_name", nullable = false, length = 200)
    private String cooperativeName;
    
    @Column(name = "slug", unique = true, nullable = false, length = 100)
    private String slug;
    
    @Column(name = "cooperative_code", unique = true, nullable = false, length = 50)
    private String cooperativeCode;
    
    @Column(name = "establishment_date", nullable = false)
    private LocalDate establishmentDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "cooperative_type", nullable = false)
    private CooperativeType cooperativeType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "scale", nullable = false)
    private CooperativeScale scale;
    
    @Column(name = "short_description", nullable = false, columnDefinition = "TEXT")
    private String shortDescription;
    
    // ========== STEP 2: Thông tin liên hệ ==========
    @Column(name = "contact_email")
    private String contactEmail;
    
    @Column(name = "contact_phone", length = 20)
    private String contactPhone;
    
    @Column(name = "contact_phone_alt", length = 20)
    private String contactPhoneAlt;
    
    @Column(name = "website", length = 500)
    private String website;
    
    @Column(name = "facebook_page", length = 500)
    private String facebookPage;
    
    // ========== STEP 3: Địa chỉ kinh doanh ==========
    @Column(name = "full_address", columnDefinition = "TEXT")
    private String fullAddress;
    
    @Column(name = "province", length = 100)
    private String province;
    
    @Column(name = "district", length = 100)
    private String district;
    
    @Column(name = "ward", length = 100)
    private String ward;
    
    @Column(name = "postal_code", length = 20)
    private String postalCode;
    
    @Column(name = "show_map")
    private Boolean showMap = false;
    
    @Column(name = "latitude")
    private Double latitude;
    
    @Column(name = "longitude")
    private Double longitude;
    
    // ========== STEP 4: Thông tin người đại diện ==========
    @Column(name = "representative_name", length = 200)
    private String representativeName;
    
    @Column(name = "representative_position", length = 100)
    private String representativePosition;
    
    @Column(name = "representative_id_number", length = 50)
    private String representativeIdNumber;
    
    @Column(name = "representative_id_issue_date")
    private LocalDate representativeIdIssueDate;
    
    @Column(name = "representative_id_issue_place", length = 200)
    private String representativeIdIssuePlace;
    
    @Column(name = "representative_email")
    private String representativeEmail;
    
    @Column(name = "representative_phone", length = 20)
    private String representativePhone;
    
    @Column(name = "representative_id_front_image", length = 500)
    private String representativeIdFrontImage;
    
    @Column(name = "representative_id_back_image", length = 500)
    private String representativeIdBackImage;
    
    // ========== STEP 5: Thông tin pháp lý ==========
    @Column(name = "tax_code", unique = true, length = 50)
    private String taxCode;
    
    @Column(name = "registration_certificate_number", length = 100)
    private String registrationCertificateNumber;
    
    @Column(name = "registration_certificate_issue_date")
    private LocalDate registrationCertificateIssueDate;
    
    @Column(name = "registration_certificate_issue_place", length = 200)
    private String registrationCertificateIssuePlace;
    
    @Column(name = "registration_certificate_image", length = 500)
    private String registrationCertificateImage;
    
    @Column(name = "tax_code_certificate_image", length = 500)
    private String taxCodeCertificateImage;
    
    // ========== STEP 6: Thông tin kinh doanh ==========
    @ElementCollection
    @CollectionTable(name = "cooperative_registration_product_types", 
                     joinColumns = @JoinColumn(name = "registration_id"))
    @Column(name = "product_type")
    private List<String> productTypes = new ArrayList<>();
    
    @Column(name = "main_product_description", columnDefinition = "TEXT")
    private String mainProductDescription;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "business_scale")
    private BusinessScale businessScale;
    
    @Column(name = "has_special_certification")
    private Boolean hasSpecialCertification;
    
    @Column(name = "special_certification_details", columnDefinition = "TEXT")
    private String specialCertificationDetails;
    
    // ========== Status và tracking ==========
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApprovalStatus status = ApprovalStatus.DRAFT;
    
    @Column(name = "current_step", nullable = false)
    private Integer currentStep = 0; // 0-6: 0 = chưa bắt đầu, 6 = hoàn thành
    
    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;
    
    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;
    
    @Column(name = "reviewed_by")
    private Long reviewedBy;
    
    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

