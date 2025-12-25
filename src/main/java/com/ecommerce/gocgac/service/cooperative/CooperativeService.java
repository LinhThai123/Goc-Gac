package com.ecommerce.gocgac.service.cooperative;

import com.ecommerce.gocgac.common.response.MessageResponse;
import com.ecommerce.gocgac.common.util.XssSanitizer;
import com.ecommerce.gocgac.entity.Cooperative;
import com.ecommerce.gocgac.entity.CooperativeRegistration;
import com.ecommerce.gocgac.entity.Store;
import com.ecommerce.gocgac.entity.User;
import com.ecommerce.gocgac.entity.enums.ApprovalStatus;
import com.ecommerce.gocgac.entity.enums.StoreStatus;
import com.ecommerce.gocgac.entity.enums.UserType;
import com.ecommerce.gocgac.exception.CooperativeException;
import com.ecommerce.gocgac.external.KeycloakClient;
import com.ecommerce.gocgac.repository.CooperativeRegistrationRepository;
import com.ecommerce.gocgac.repository.CooperativeRepository;
import com.ecommerce.gocgac.repository.StoreRepository;
import com.ecommerce.gocgac.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CooperativeService {
    
    private final CooperativeRepository cooperativeRepository;
    private final CooperativeRegistrationRepository registrationRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final KeycloakClient keycloakClient;
    
    /**
     * Approve registration và tạo Cooperative entity
     */
    @Transactional
    public MessageResponse approveRegistration(Long registrationId, Long adminId) {
        CooperativeRegistration registration = registrationRepository.findById(registrationId)
            .orElseThrow(() -> new CooperativeException("Đơn đăng ký không tồn tại"));
        
        // Validate status
        if (registration.getStatus() != ApprovalStatus.PENDING) {
            throw new CooperativeException("Chỉ có thể approve đơn đăng ký ở trạng thái PENDING");
        }
        
        // Validate user tồn tại
        User user = userRepository.findById(registration.getUserId())
            .orElseThrow(() -> new CooperativeException("User không tồn tại"));
        
        // Validate user chưa có cooperative
        if (cooperativeRepository.existsByUserId(user.getId())) {
            throw new CooperativeException("User đã có cooperative");
        }
        
        // Validate tất cả thông tin đã đầy đủ (đã được validate khi submit)
        
        // Tạo Cooperative entity từ registration
        Cooperative cooperative = new Cooperative();
        cooperative.setUserId(user.getId());
        cooperative.setRegistrationId(registration.getId());
        
        // Copy thông tin từ registration (đã được sanitize khi lưu vào registration)
        // Nhưng vẫn sanitize lại để đảm bảo an toàn
        cooperative.setCooperativeName(XssSanitizer.sanitize(registration.getCooperativeName()));
        cooperative.setSlug(XssSanitizer.sanitize(registration.getSlug()));
        cooperative.setCooperativeCode(XssSanitizer.sanitize(registration.getCooperativeCode()));
        cooperative.setEstablishmentDate(registration.getEstablishmentDate());
        cooperative.setCooperativeType(registration.getCooperativeType());
        cooperative.setScale(registration.getScale());
        cooperative.setShortDescription(XssSanitizer.sanitizeHtml(registration.getShortDescription()));
        
        // Thông tin liên hệ
        // Email, phone, URL không cần sanitize - đã được validate và không chứa HTML
        cooperative.setContactEmail(registration.getContactEmail());
        cooperative.setContactPhone(registration.getContactPhone());
        cooperative.setContactPhoneAlt(registration.getContactPhoneAlt());
        cooperative.setWebsite(registration.getWebsite());
        cooperative.setFacebookPage(registration.getFacebookPage());
        
        // Địa chỉ
        cooperative.setFullAddress(XssSanitizer.sanitize(registration.getFullAddress()));
        cooperative.setProvince(XssSanitizer.sanitize(registration.getProvince()));
        cooperative.setDistrict(XssSanitizer.sanitize(registration.getDistrict()));
        cooperative.setWard(XssSanitizer.sanitize(registration.getWard()));
        cooperative.setPostalCode(StringUtils.hasText(registration.getPostalCode()) ? 
            XssSanitizer.sanitize(registration.getPostalCode()) : null);
        cooperative.setShowMap(registration.getShowMap());
        cooperative.setLatitude(registration.getLatitude());
        cooperative.setLongitude(registration.getLongitude());
        
        // Người đại diện
        cooperative.setRepresentativeName(XssSanitizer.sanitize(registration.getRepresentativeName()));
        cooperative.setRepresentativePosition(XssSanitizer.sanitize(registration.getRepresentativePosition()));
        cooperative.setRepresentativeIdNumber(XssSanitizer.sanitize(registration.getRepresentativeIdNumber()));
        cooperative.setRepresentativeIdIssueDate(registration.getRepresentativeIdIssueDate());
        cooperative.setRepresentativeIdIssuePlace(XssSanitizer.sanitize(registration.getRepresentativeIdIssuePlace()));
        // Email, phone không cần sanitize - đã được validate
        cooperative.setRepresentativeEmail(registration.getRepresentativeEmail());
        cooperative.setRepresentativePhone(registration.getRepresentativePhone());
        
        // Pháp lý
        cooperative.setTaxCode(StringUtils.hasText(registration.getTaxCode()) ? 
            XssSanitizer.sanitize(registration.getTaxCode()) : null);
        cooperative.setRegistrationCertificateNumber(XssSanitizer.sanitize(registration.getRegistrationCertificateNumber()));
        cooperative.setRegistrationCertificateIssueDate(registration.getRegistrationCertificateIssueDate());
        cooperative.setRegistrationCertificateIssuePlace(XssSanitizer.sanitize(registration.getRegistrationCertificateIssuePlace()));
        
        // Kinh doanh
        if (registration.getProductTypes() != null) {
            List<String> sanitizedProductTypes = registration.getProductTypes().stream()
                .map(XssSanitizer::sanitize)
                .collect(Collectors.toList());
            cooperative.setProductTypes(sanitizedProductTypes);
        }
        cooperative.setMainProductDescription(XssSanitizer.sanitizeHtml(registration.getMainProductDescription()));
        
        cooperative = cooperativeRepository.save(cooperative);
        
        // Tạo Store cho Cooperative (1 HTX = 1 Store)
        // Validate cooperative chưa có Store
        if (storeRepository.findByCooperativeId(cooperative.getId()).isPresent()) {
            throw new CooperativeException("Cooperative đã có Store");
        }
        
        Store store = new Store();
        store.setCooperativeId(cooperative.getId());
        store.setSellerId(null); // Store của HTX không gắn với Seller cụ thể
        store.setStoreName(cooperative.getCooperativeName());
        store.setStoreCode(cooperative.getCooperativeCode());
        store.setDescription(cooperative.getShortDescription());
        store.setContactPhone(cooperative.getContactPhone());
        store.setContactEmail(cooperative.getContactEmail());
        store.setAddress(cooperative.getFullAddress());
        store.setStatus(StoreStatus.ACTIVE);
        store.setIsVerified(false);
        store.setIsBranded(false);
        store = storeRepository.save(store);
        
        log.info("Store {} created for Cooperative {}", store.getId(), cooperative.getId());
        
        // Update registration status
        registration.setStatus(ApprovalStatus.APPROVED);
        registration.setReviewedAt(LocalDateTime.now());
        registration.setReviewedBy(adminId);
        registrationRepository.save(registration);
        
        // Update User
        user.setUserType(UserType.COOPERATIVE_MANAGER);
        userRepository.save(user);
        
        // Assign role trong Keycloak
        try {
            if (user.getKeycloakId() != null) {
                keycloakClient.assignRoleToUser(user.getKeycloakId(), "COOPERATIVE_MANAGER");
                log.info("Đã gán role COOPERATIVE_MANAGER cho user {} trong Keycloak", user.getKeycloakId());
            }
        } catch (Exception e) {
            log.error("Không thể gán role COOPERATIVE_MANAGER trong Keycloak: {}", e.getMessage(), e);
            // Không throw exception để không rollback transaction
            // Có thể retry sau hoặc admin assign manually
        }
        
        log.info("Cooperative registration {} approved by admin {}. Cooperative ID: {}", 
            registrationId, adminId, cooperative.getId());
        
        MessageResponse response = new MessageResponse();
        response.setMessage("Đơn đăng ký HTX đã được phê duyệt thành công");
        response.setStatus(HttpStatus.OK.value());
        response.setData(cooperative);
        
        return response;
    }
    
    /**
     * Reject registration
     */
    @Transactional
    public MessageResponse rejectRegistration(Long registrationId, Long adminId, String reason) {
        CooperativeRegistration registration = registrationRepository.findById(registrationId)
            .orElseThrow(() -> new CooperativeException("Đơn đăng ký không tồn tại"));
        
        // Validate status
        if (registration.getStatus() != ApprovalStatus.PENDING) {
            throw new CooperativeException("Chỉ có thể reject đơn đăng ký ở trạng thái PENDING");
        }
        
        if (reason == null || reason.trim().isEmpty()) {
            throw new CooperativeException("Lý do từ chối không được để trống");
        }
        
        // Sanitize rejection reason
        String sanitizedReason = XssSanitizer.sanitize(reason);
        
        // Update registration
        registration.setStatus(ApprovalStatus.REJECTED);
        registration.setRejectionReason(sanitizedReason);
        registration.setReviewedAt(LocalDateTime.now());
        registration.setReviewedBy(adminId);
        
        registrationRepository.save(registration);
        
        log.info("Cooperative registration {} rejected by admin {}. Reason: {}", 
            registrationId, adminId, sanitizedReason);
        
        MessageResponse response = new MessageResponse();
        response.setMessage("Đơn đăng ký HTX đã bị từ chối");
        response.setStatus(HttpStatus.OK.value());
        response.setData(registration);
        
        return response;
    }
    
    /**
     * Lấy danh sách registrations với pagination và filter
     */
    public Page<CooperativeRegistration> getRegistrations(ApprovalStatus status, Pageable pageable) {
        if (status != null) {
            return registrationRepository.findByStatus(status, pageable);
        }
        return registrationRepository.findAll(pageable);
    }
    
    /**
     * Lấy thông tin registration theo ID (cho admin)
     */
    public CooperativeRegistration getRegistrationById(Long registrationId) {
        return registrationRepository.findById(registrationId)
            .orElseThrow(() -> new CooperativeException("Đơn đăng ký không tồn tại"));
    }
    
    /**
     * Lấy thông tin cooperative của user
     */
    public Optional<Cooperative> getCooperativeByUserId(Long userId) {
        return cooperativeRepository.findByUserId(userId);
    }
    
    /**
     * Lấy thông tin cooperative theo slug (public)
     */
    public Optional<Cooperative> getCooperativeBySlug(String slug) {
        return cooperativeRepository.findBySlug(slug);
    }
}
