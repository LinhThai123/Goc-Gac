package com.ecommerce.gocgac.service.cooperative;

import com.ecommerce.gocgac.common.response.MessageResponse;
import com.ecommerce.gocgac.common.util.XssSanitizer;
import com.ecommerce.gocgac.dto.cooperative.CooperativeRegistrationRequest;
import com.ecommerce.gocgac.entity.CooperativeRegistration;
import com.ecommerce.gocgac.entity.enums.ApprovalStatus;
import com.ecommerce.gocgac.exception.CooperativeException;
import com.ecommerce.gocgac.repository.CooperativeRegistrationRepository;
import com.ecommerce.gocgac.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CooperativeRegistrationService {
    
    private final CooperativeRegistrationRepository registrationRepository;
    private final UserRepository userRepository;
    
    /**
     * Đăng ký HTX với tất cả thông tin trong 1 request (đơn giản hóa)
     * Giống SellerRegistration - 1 request duy nhất
     */
    @Transactional
    public MessageResponse registerCooperative(Long userId, CooperativeRegistrationRequest request) {
        // Kiểm tra user tồn tại
        if (!userRepository.existsById(userId)) {
            throw new CooperativeException("User không tồn tại");
        }
        
        // Kiểm tra user đã có registration PENDING chưa
        Optional<CooperativeRegistration> existingRegistration = registrationRepository
            .findByUserIdAndStatusIn(userId, Arrays.asList(ApprovalStatus.PENDING));
        
        if (existingRegistration.isPresent()) {
            throw new CooperativeException("Bạn đã có đơn đăng ký đang chờ xử lý. Vui lòng hoàn thành hoặc hủy đơn hiện tại trước khi tạo mới.");
        }
        
        // Validate unique fields
        if (registrationRepository.existsBySlug(request.getSlug())) {
            throw new CooperativeException("Slug đã tồn tại");
        }
        
        if (registrationRepository.existsByCooperativeCode(request.getCooperativeCode())) {
            throw new CooperativeException("Mã số HTX đã tồn tại");
        }
        
        // Tạo mới registration với tất cả thông tin
        CooperativeRegistration registration = new CooperativeRegistration();
        registration.setUserId(userId);
        registration.setStatus(ApprovalStatus.PENDING); // Trực tiếp PENDING, không cần DRAFT
        registration.setCurrentStep(6); // Đã hoàn thành tất cả 6 bước
        
        // Step 1: Thông tin HTX cơ bản
        registration.setCooperativeName(XssSanitizer.sanitize(request.getCooperativeName()));
        registration.setSlug(XssSanitizer.sanitize(request.getSlug()));
        registration.setCooperativeCode(XssSanitizer.sanitize(request.getCooperativeCode()));
        registration.setEstablishmentDate(request.getEstablishmentDate());
        registration.setCooperativeType(request.getCooperativeType());
        registration.setScale(request.getScale());
        registration.setShortDescription(XssSanitizer.sanitizeHtml(request.getShortDescription()));
        
        // Step 2: Thông tin liên hệ
        // Email, phone, URL không cần sanitize - đã có validation riêng (@Email, @Pattern, @URL)
        registration.setContactEmail(request.getContactEmail());
        registration.setContactPhone(request.getContactPhone());
        if (StringUtils.hasText(request.getContactPhoneAlt())) {
            registration.setContactPhoneAlt(request.getContactPhoneAlt());
        }
        if (StringUtils.hasText(request.getWebsite())) {
            registration.setWebsite(request.getWebsite());
        }
        if (StringUtils.hasText(request.getFacebookPage())) {
            registration.setFacebookPage(request.getFacebookPage());
        }
        
        // Step 3: Địa chỉ kinh doanh
        registration.setFullAddress(XssSanitizer.sanitize(request.getFullAddress()));
        registration.setProvince(XssSanitizer.sanitize(request.getProvince()));
        registration.setDistrict(XssSanitizer.sanitize(request.getDistrict()));
        registration.setWard(XssSanitizer.sanitize(request.getWard()));
        if (StringUtils.hasText(request.getPostalCode())) {
            registration.setPostalCode(XssSanitizer.sanitize(request.getPostalCode()));
        }
        registration.setShowMap(request.getShowMap() != null ? request.getShowMap() : false);
        registration.setLatitude(request.getLatitude());
        registration.setLongitude(request.getLongitude());
        
        // Step 4: Thông tin người đại diện
        registration.setRepresentativeName(XssSanitizer.sanitize(request.getRepresentativeName()));
        registration.setRepresentativePosition(XssSanitizer.sanitize(request.getRepresentativePosition()));
        registration.setRepresentativeIdNumber(XssSanitizer.sanitize(request.getRepresentativeIdNumber()));
        registration.setRepresentativeIdIssueDate(request.getRepresentativeIdIssueDate());
        registration.setRepresentativeIdIssuePlace(XssSanitizer.sanitize(request.getRepresentativeIdIssuePlace()));
        // Email, phone, URL không cần sanitize - đã có validation riêng
        registration.setRepresentativeEmail(request.getRepresentativeEmail());
        registration.setRepresentativePhone(request.getRepresentativePhone());
        if (StringUtils.hasText(request.getRepresentativeIdFrontImage())) {
            registration.setRepresentativeIdFrontImage(request.getRepresentativeIdFrontImage());
        }
        if (StringUtils.hasText(request.getRepresentativeIdBackImage())) {
            registration.setRepresentativeIdBackImage(request.getRepresentativeIdBackImage());
        }
        
        // Step 5: Thông tin pháp lý
        if (StringUtils.hasText(request.getTaxCode())) {
            registration.setTaxCode(request.getTaxCode()); // Tax code không cần sanitize
        }
        registration.setRegistrationCertificateNumber(XssSanitizer.sanitize(request.getRegistrationCertificateNumber()));
        registration.setRegistrationCertificateIssueDate(request.getRegistrationCertificateIssueDate());
        registration.setRegistrationCertificateIssuePlace(XssSanitizer.sanitize(request.getRegistrationCertificateIssuePlace()));
        // URL images không cần sanitize - đã có @URL validation
        if (StringUtils.hasText(request.getRegistrationCertificateImage())) {
            registration.setRegistrationCertificateImage(request.getRegistrationCertificateImage());
        }
        if (StringUtils.hasText(request.getTaxCodeCertificateImage())) {
            registration.setTaxCodeCertificateImage(request.getTaxCodeCertificateImage());
        }
        
        // Step 6: Thông tin kinh doanh
        registration.setProductTypes(request.getProductTypes().stream()
            .map(XssSanitizer::sanitize)
            .collect(Collectors.toList()));
        registration.setMainProductDescription(XssSanitizer.sanitizeHtml(request.getMainProductDescription()));
        registration.setBusinessScale(request.getBusinessScale());
        registration.setHasSpecialCertification(request.getHasSpecialCertification() != null ? request.getHasSpecialCertification() : false);
        if (StringUtils.hasText(request.getSpecialCertificationDetails())) {
            registration.setSpecialCertificationDetails(XssSanitizer.sanitizeHtml(request.getSpecialCertificationDetails()));
        }
        
        registration.setSubmittedAt(LocalDateTime.now());
        
        registration = registrationRepository.save(registration);
        
        log.info("Cooperative registration {} created and submitted by user {}", registration.getId(), userId);
        
        MessageResponse response = new MessageResponse();
        response.setMessage("Đơn đăng ký HTX đã được gửi thành công. Vui lòng chờ admin phê duyệt.");
        response.setStatus(HttpStatus.OK.value());
        response.setData(registration);
        
        return response;
    }
    
    /**
     * Lấy thông tin chi tiết registration
     */
    public CooperativeRegistration getRegistrationDetail(Long userId, Long registrationId) {
        return findRegistrationByUserAndId(userId, registrationId);
    }
    
    /**
     * Lấy registration hiện tại của user (PENDING)
     */
    public Optional<CooperativeRegistration> getMyRegistration(Long userId) {
        return registrationRepository.findByUserIdAndStatusIn(
            userId, 
            Arrays.asList(ApprovalStatus.PENDING)
        );
    }
    
    /**
     * Kiểm tra user có thể tạo registration mới không
     */
    public boolean canCreateNewRegistration(Long userId) {
        Optional<CooperativeRegistration> existing = registrationRepository.findByUserIdAndStatusIn(
            userId,
            Arrays.asList(ApprovalStatus.PENDING)
        );
        return existing.isEmpty();
    }
    
    // ========== Private Helper Methods ==========
    
    private CooperativeRegistration findRegistrationByUserAndId(Long userId, Long registrationId) {
        CooperativeRegistration registration = registrationRepository.findById(registrationId)
            .orElseThrow(() -> new CooperativeException("Đơn đăng ký không tồn tại"));
        
        if (!registration.getUserId().equals(userId)) {
            throw new CooperativeException("Bạn không có quyền truy cập đơn đăng ký này");
        }
        
        return registration;
    }
}
