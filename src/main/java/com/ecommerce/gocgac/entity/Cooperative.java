package com.ecommerce.gocgac.entity;

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
@Table(name = "cooperatives", indexes = {
    @Index(name = "idx_cooperatives_user_id", columnList = "user_id"),
    @Index(name = "idx_cooperatives_slug", columnList = "slug"),
    @Index(name = "idx_cooperatives_code", columnList = "cooperative_code")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cooperative {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId; // User với COOPERATIVE_MANAGER role
    
    @Column(name = "registration_id", nullable = false)
    private Long registrationId; // Reference đến CooperativeRegistration đã approve
    
    // ========== Thông tin cơ bản (từ registration) ==========
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
    
    // ========== Thông tin liên hệ ==========
    @Column(name = "contact_email", nullable = false)
    private String contactEmail;
    
    @Column(name = "contact_phone", nullable = false, length = 20)
    private String contactPhone;
    
    @Column(name = "contact_phone_alt", length = 20)
    private String contactPhoneAlt;
    
    @Column(name = "website", length = 500)
    private String website;
    
    @Column(name = "facebook_page", length = 500)
    private String facebookPage;
    
    // ========== Địa chỉ ==========
    @Column(name = "full_address", nullable = false, columnDefinition = "TEXT")
    private String fullAddress;
    
    @Column(name = "province", nullable = false, length = 100)
    private String province;
    
    @Column(name = "district", nullable = false, length = 100)
    private String district;
    
    @Column(name = "ward", nullable = false, length = 100)
    private String ward;
    
    @Column(name = "postal_code", length = 20)
    private String postalCode;
    
    @Column(name = "show_map", nullable = false)
    private Boolean showMap = false;
    
    @Column(name = "latitude")
    private Double latitude;
    
    @Column(name = "longitude")
    private Double longitude;
    
    // ========== Thông tin người đại diện ==========
    @Column(name = "representative_name", nullable = false, length = 200)
    private String representativeName;
    
    @Column(name = "representative_position", nullable = false, length = 100)
    private String representativePosition;
    
    @Column(name = "representative_id_number", nullable = false, length = 50)
    private String representativeIdNumber;
    
    @Column(name = "representative_id_issue_date", nullable = false)
    private LocalDate representativeIdIssueDate;
    
    @Column(name = "representative_id_issue_place", nullable = false, length = 200)
    private String representativeIdIssuePlace;
    
    @Column(name = "representative_email", nullable = false)
    private String representativeEmail;
    
    @Column(name = "representative_phone", nullable = false, length = 20)
    private String representativePhone;
    
    // ========== Thông tin pháp lý ==========
    @Column(name = "tax_code", unique = true, length = 50)
    private String taxCode;
    
    @Column(name = "registration_certificate_number", nullable = false, length = 100)
    private String registrationCertificateNumber;
    
    @Column(name = "registration_certificate_issue_date", nullable = false)
    private LocalDate registrationCertificateIssueDate;
    
    @Column(name = "registration_certificate_issue_place", nullable = false, length = 200)
    private String registrationCertificateIssuePlace;
    
    // ========== Thông tin kinh doanh ==========
    @ElementCollection
    @CollectionTable(name = "cooperative_product_types", 
                     joinColumns = @JoinColumn(name = "cooperative_id"))
    @Column(name = "product_type")
    private List<String> productTypes = new ArrayList<>();
    
    @Column(name = "main_product_description", nullable = false, columnDefinition = "TEXT")
    private String mainProductDescription;
    
    // ========== Thông tin bổ sung (có thể update sau) ==========
    @Column(name = "logo_url", length = 500)
    private String logoUrl;
    
    @Column(name = "banner_url", length = 500)
    private String bannerUrl;
    
    @Column(name = "video_url", length = 500)
    private String videoUrl;
    
    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

