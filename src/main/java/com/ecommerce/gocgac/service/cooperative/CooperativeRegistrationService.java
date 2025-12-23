package com.ecommerce.gocgac.service.cooperative;

import com.ecommerce.gocgac.common.response.MessageResponse;
import com.ecommerce.gocgac.common.util.XssSanitizer;
import com.ecommerce.gocgac.dto.cooperative.*;
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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CooperativeRegistrationService {
    
    private final CooperativeRegistrationRepository registrationRepository;
    private final UserRepository userRepository;
    
    /**
     * Tạo hoặc cập nhật Step 1: Thông tin HTX cơ bản
     */
    @Transactional
    public MessageResponse createOrUpdateStep1(Long userId, Step1Request request) {
        // Kiểm tra user tồn tại
        if (!userRepository.existsById(userId)) {
            throw new CooperativeException("User không tồn tại");
        }
        
        // Kiểm tra user đã có registration DRAFT/PENDING chưa
        Optional<CooperativeRegistration> existingRegistration = registrationRepository
            .findByUserIdAndStatusIn(userId, Arrays.asList(ApprovalStatus.DRAFT, ApprovalStatus.PENDING));
        
        CooperativeRegistration registration;
        
        if (existingRegistration.isPresent()) {
            registration = existingRegistration.get();
            // Kiểm tra status
            if (registration.getStatus() == ApprovalStatus.PENDING) {
                throw new CooperativeException("Không thể chỉnh sửa đơn đăng ký đã được submit");
            }
        } else {
            // Tạo mới
            registration = new CooperativeRegistration();
            registration.setUserId(userId);
            registration.setStatus(ApprovalStatus.DRAFT);
            registration.setCurrentStep(0);
        }
        
        // Validate unique fields
        if (registrationRepository.existsBySlug(request.getSlug()) && 
            (registration.getId() == null || !registration.getSlug().equals(request.getSlug()))) {
            throw new CooperativeException("Slug đã tồn tại");
        }
        
        if (registrationRepository.existsByCooperativeCode(request.getCooperativeCode()) && 
            (registration.getId() == null || !registration.getCooperativeCode().equals(request.getCooperativeCode()))) {
            throw new CooperativeException("Mã số HTX đã tồn tại");
        }
        
        // Update Step 1 fields (sanitize để chống XSS)
        registration.setCooperativeName(XssSanitizer.sanitize(request.getCooperativeName()));
        registration.setSlug(XssSanitizer.sanitize(request.getSlug())); // Slug đã có pattern validation nhưng vẫn sanitize để an toàn
        registration.setCooperativeCode(XssSanitizer.sanitize(request.getCooperativeCode())); // Code đã có pattern validation nhưng vẫn sanitize
        registration.setEstablishmentDate(request.getEstablishmentDate());
        registration.setCooperativeType(request.getCooperativeType());
        registration.setScale(request.getScale());
        registration.setShortDescription(XssSanitizer.sanitizeHtml(request.getShortDescription())); // Cho phép một số HTML formatting
        registration.setCurrentStep(1);
        
        registration = registrationRepository.save(registration);
        
        RegistrationStepData data = new RegistrationStepData(
            registration.getId(),
            1,
            registration.getStatus()
        );
        
        MessageResponse response = new MessageResponse();
        response.setMessage("Lưu thông tin HTX thành công");
        response.setStatus(HttpStatus.OK.value());
        response.setData(data);
        
        return response;
    }
    
    /**
     * Cập nhật Step 2: Thông tin liên hệ
     */
    @Transactional
    public MessageResponse updateStep2(Long userId, Step2Request request) {
        CooperativeRegistration registration = findRegistrationByUserAndId(userId, request.getRegistrationId());
        
        // Update Step 2 fields (sanitize để chống XSS)
        registration.setContactEmail(XssSanitizer.sanitize(request.getContactEmail())); // Email đã có validation nhưng vẫn sanitize
        registration.setContactPhone(XssSanitizer.sanitize(request.getContactPhone())); // Phone đã có pattern validation
        registration.setContactPhoneAlt(StringUtils.hasText(request.getContactPhoneAlt()) ? 
            XssSanitizer.sanitize(request.getContactPhoneAlt()) : null);
        registration.setWebsite(StringUtils.hasText(request.getWebsite()) ? 
            XssSanitizer.sanitize(request.getWebsite()) : null);
        registration.setFacebookPage(StringUtils.hasText(request.getFacebookPage()) ? 
            XssSanitizer.sanitize(request.getFacebookPage()) : null);
        registration.setCurrentStep(Math.max(registration.getCurrentStep(), 2));
        
        registration = registrationRepository.save(registration);
        
        RegistrationStepData data = new RegistrationStepData(
            registration.getId(),
            2,
            registration.getStatus()
        );
        
        MessageResponse response = new MessageResponse();
        response.setMessage("Lưu thông tin liên hệ thành công");
        response.setStatus(HttpStatus.OK.value());
        response.setData(data);
        
        return response;
    }
    
    /**
     * Cập nhật Step 3: Địa chỉ kinh doanh
     */
    @Transactional
    public MessageResponse updateStep3(Long userId, Step3Request request) {
        CooperativeRegistration registration = findRegistrationByUserAndId(userId, request.getRegistrationId());
        
        // Update Step 3 fields (sanitize để chống XSS)
        registration.setFullAddress(XssSanitizer.sanitize(request.getFullAddress()));
        registration.setProvince(XssSanitizer.sanitize(request.getProvince()));
        registration.setDistrict(XssSanitizer.sanitize(request.getDistrict()));
        registration.setWard(XssSanitizer.sanitize(request.getWard()));
        registration.setPostalCode(StringUtils.hasText(request.getPostalCode()) ? 
            XssSanitizer.sanitize(request.getPostalCode()) : null);
        registration.setShowMap(request.getShowMap());
        registration.setLatitude(request.getLatitude());
        registration.setLongitude(request.getLongitude());
        registration.setCurrentStep(Math.max(registration.getCurrentStep(), 3));
        
        registration = registrationRepository.save(registration);
        
        RegistrationStepData data = new RegistrationStepData(
            registration.getId(),
            3,
            registration.getStatus()
        );
        
        MessageResponse response = new MessageResponse();
        response.setMessage("Lưu địa chỉ kinh doanh thành công");
        response.setStatus(HttpStatus.OK.value());
        response.setData(data);
        
        return response;
    }
    
    /**
     * Cập nhật Step 4: Thông tin người đại diện
     */
    @Transactional
    public MessageResponse updateStep4(Long userId, Step4Request request) {
        CooperativeRegistration registration = findRegistrationByUserAndId(userId, request.getRegistrationId());
        
        // Update Step 4 fields (sanitize để chống XSS)
        registration.setRepresentativeName(XssSanitizer.sanitize(request.getRepresentativeName()));
        registration.setRepresentativePosition(XssSanitizer.sanitize(request.getRepresentativePosition()));
        registration.setRepresentativeIdNumber(XssSanitizer.sanitize(request.getRepresentativeIdNumber()));
        registration.setRepresentativeIdIssueDate(request.getRepresentativeIdIssueDate());
        registration.setRepresentativeIdIssuePlace(XssSanitizer.sanitize(request.getRepresentativeIdIssuePlace()));
        registration.setRepresentativeEmail(XssSanitizer.sanitize(request.getRepresentativeEmail()));
        registration.setRepresentativePhone(XssSanitizer.sanitize(request.getRepresentativePhone()));
        registration.setRepresentativeIdFrontImage(StringUtils.hasText(request.getRepresentativeIdFrontImage()) ? 
            XssSanitizer.sanitize(request.getRepresentativeIdFrontImage()) : null);
        registration.setRepresentativeIdBackImage(StringUtils.hasText(request.getRepresentativeIdBackImage()) ? 
            XssSanitizer.sanitize(request.getRepresentativeIdBackImage()) : null);
        registration.setCurrentStep(Math.max(registration.getCurrentStep(), 4));
        
        registration = registrationRepository.save(registration);
        
        RegistrationStepData data = new RegistrationStepData(
            registration.getId(),
            4,
            registration.getStatus()
        );
        
        MessageResponse response = new MessageResponse();
        response.setMessage("Lưu thông tin người đại diện thành công");
        response.setStatus(HttpStatus.OK.value());
        response.setData(data);
        
        return response;
    }
    
    /**
     * Cập nhật Step 5: Thông tin pháp lý
     */
    @Transactional
    public MessageResponse updateStep5(Long userId, Step5Request request) {
        CooperativeRegistration registration = findRegistrationByUserAndId(userId, request.getRegistrationId());
        
        // Validate taxCode unique (nếu có)
        if (request.getTaxCode() != null && !request.getTaxCode().isEmpty()) {
            if (registrationRepository.existsByTaxCode(request.getTaxCode()) && 
                (registration.getTaxCode() == null || !registration.getTaxCode().equals(request.getTaxCode()))) {
                throw new CooperativeException("Mã số thuế đã tồn tại");
            }
        }
        
        // Update Step 5 fields (sanitize để chống XSS)
        registration.setTaxCode(StringUtils.hasText(request.getTaxCode()) ? 
            XssSanitizer.sanitize(request.getTaxCode()) : null);
        registration.setRegistrationCertificateNumber(XssSanitizer.sanitize(request.getRegistrationCertificateNumber()));
        registration.setRegistrationCertificateIssueDate(request.getRegistrationCertificateIssueDate());
        registration.setRegistrationCertificateIssuePlace(XssSanitizer.sanitize(request.getRegistrationCertificateIssuePlace()));
        registration.setRegistrationCertificateImage(StringUtils.hasText(request.getRegistrationCertificateImage()) ? 
            XssSanitizer.sanitize(request.getRegistrationCertificateImage()) : null);
        registration.setTaxCodeCertificateImage(StringUtils.hasText(request.getTaxCodeCertificateImage()) ? 
            XssSanitizer.sanitize(request.getTaxCodeCertificateImage()) : null);
        registration.setCurrentStep(Math.max(registration.getCurrentStep(), 5));
        
        registration = registrationRepository.save(registration);
        
        RegistrationStepData data = new RegistrationStepData(
            registration.getId(),
            5,
            registration.getStatus()
        );
        
        MessageResponse response = new MessageResponse();
        response.setMessage("Lưu thông tin pháp lý thành công");
        response.setStatus(HttpStatus.OK.value());
        response.setData(data);
        
        return response;
    }
    
    /**
     * Cập nhật Step 6: Thông tin kinh doanh
     */
    @Transactional
    public MessageResponse updateStep6(Long userId, Step6Request request) {
        CooperativeRegistration registration = findRegistrationByUserAndId(userId, request.getRegistrationId());
        
        // Update Step 6 fields (sanitize để chống XSS)
        // Sanitize productTypes list
        if (request.getProductTypes() != null) {
            List<String> sanitizedProductTypes = request.getProductTypes().stream()
                .map(XssSanitizer::sanitize)
                .collect(Collectors.toList());
            registration.setProductTypes(sanitizedProductTypes);
        }
        registration.setMainProductDescription(XssSanitizer.sanitizeHtml(request.getMainProductDescription())); // Cho phép HTML formatting
        registration.setBusinessScale(request.getBusinessScale());
        registration.setHasSpecialCertification(request.getHasSpecialCertification());
        registration.setSpecialCertificationDetails(StringUtils.hasText(request.getSpecialCertificationDetails()) ? 
            XssSanitizer.sanitizeHtml(request.getSpecialCertificationDetails()) : null); // Cho phép HTML formatting
        registration.setCurrentStep(6);
        
        registration = registrationRepository.save(registration);
        
        RegistrationStepData data = new RegistrationStepData(
            registration.getId(),
            6,
            registration.getStatus()
        );
        
        MessageResponse response = new MessageResponse();
        response.setMessage("Lưu thông tin kinh doanh thành công");
        response.setStatus(HttpStatus.OK.value());
        response.setData(data);
        
        return response;
    }
    
    /**
     * Submit registration (chuyển từ DRAFT sang PENDING)
     */
    @Transactional
    public MessageResponse submitRegistration(Long userId, Long registrationId) {
        CooperativeRegistration registration = findRegistrationByUserAndId(userId, registrationId);
        
        // Validate status
        if (registration.getStatus() != ApprovalStatus.DRAFT) {
            throw new CooperativeException("Chỉ có thể submit đơn đăng ký ở trạng thái DRAFT");
        }
        
        // Validate tất cả 6 bước đã điền đầy đủ
        validateAllStepsCompleted(registration);
        
        // Update status
        registration.setStatus(ApprovalStatus.PENDING);
        registration.setSubmittedAt(LocalDateTime.now());
        
        registration = registrationRepository.save(registration);
        
        log.info("Cooperative registration {} submitted by user {}", registrationId, userId);
        
        RegistrationStepData data = new RegistrationStepData(
            registration.getId(),
            6,
            registration.getStatus()
        );
        
        MessageResponse response = new MessageResponse();
        response.setMessage("Đơn đăng ký đã được gửi thành công. Vui lòng chờ admin phê duyệt.");
        response.setStatus(HttpStatus.OK.value());
        response.setData(data);
        
        return response;
    }
    
    /**
     * Lấy thông tin chi tiết registration
     */
    public CooperativeRegistration getRegistrationDetail(Long userId, Long registrationId) {
        return findRegistrationByUserAndId(userId, registrationId);
    }
    
    /**
     * Lấy registration hiện tại của user (DRAFT hoặc PENDING)
     */
    public Optional<CooperativeRegistration> getMyRegistration(Long userId) {
        return registrationRepository.findByUserIdAndStatusIn(
            userId, 
            Arrays.asList(ApprovalStatus.DRAFT, ApprovalStatus.PENDING)
        );
    }
    
    /**
     * Kiểm tra user có thể tạo registration mới không
     */
    public boolean canCreateNewRegistration(Long userId) {
        Optional<CooperativeRegistration> existing = registrationRepository.findByUserIdAndStatusIn(
            userId,
            Arrays.asList(ApprovalStatus.DRAFT, ApprovalStatus.PENDING)
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
    
    private void validateAllStepsCompleted(CooperativeRegistration registration) {
        List<String> missingFields = new java.util.ArrayList<>();
        
        // Step 1
        if (registration.getCooperativeName() == null || registration.getCooperativeName().isEmpty()) {
            missingFields.add("Tên HTX");
        }
        if (registration.getSlug() == null || registration.getSlug().isEmpty()) {
            missingFields.add("Slug");
        }
        if (registration.getCooperativeCode() == null || registration.getCooperativeCode().isEmpty()) {
            missingFields.add("Mã số HTX");
        }
        if (registration.getEstablishmentDate() == null) {
            missingFields.add("Ngày thành lập");
        }
        if (registration.getCooperativeType() == null) {
            missingFields.add("Loại hình HTX");
        }
        if (registration.getScale() == null) {
            missingFields.add("Quy mô HTX");
        }
        if (registration.getShortDescription() == null || registration.getShortDescription().isEmpty()) {
            missingFields.add("Mô tả ngắn");
        }
        
        // Step 2
        if (registration.getContactEmail() == null || registration.getContactEmail().isEmpty()) {
            missingFields.add("Email liên hệ");
        }
        if (registration.getContactPhone() == null || registration.getContactPhone().isEmpty()) {
            missingFields.add("Số điện thoại");
        }
        
        // Step 3
        if (registration.getFullAddress() == null || registration.getFullAddress().isEmpty()) {
            missingFields.add("Địa chỉ đầy đủ");
        }
        if (registration.getProvince() == null || registration.getProvince().isEmpty()) {
            missingFields.add("Tỉnh/Thành phố");
        }
        if (registration.getDistrict() == null || registration.getDistrict().isEmpty()) {
            missingFields.add("Quận/Huyện");
        }
        if (registration.getWard() == null || registration.getWard().isEmpty()) {
            missingFields.add("Phường/Xã");
        }
        
        // Step 4
        if (registration.getRepresentativeName() == null || registration.getRepresentativeName().isEmpty()) {
            missingFields.add("Họ và tên người đại diện");
        }
        if (registration.getRepresentativePosition() == null || registration.getRepresentativePosition().isEmpty()) {
            missingFields.add("Chức vụ");
        }
        if (registration.getRepresentativeIdNumber() == null || registration.getRepresentativeIdNumber().isEmpty()) {
            missingFields.add("Số CCCD/CMND");
        }
        if (registration.getRepresentativeIdIssueDate() == null) {
            missingFields.add("Ngày cấp CCCD");
        }
        if (registration.getRepresentativeIdIssuePlace() == null || registration.getRepresentativeIdIssuePlace().isEmpty()) {
            missingFields.add("Nơi cấp CCCD");
        }
        if (registration.getRepresentativeEmail() == null || registration.getRepresentativeEmail().isEmpty()) {
            missingFields.add("Email người đại diện");
        }
        if (registration.getRepresentativePhone() == null || registration.getRepresentativePhone().isEmpty()) {
            missingFields.add("Số điện thoại người đại diện");
        }
        
        // Step 5
        if (registration.getRegistrationCertificateNumber() == null || registration.getRegistrationCertificateNumber().isEmpty()) {
            missingFields.add("Số Giấy CNĐK HTX");
        }
        if (registration.getRegistrationCertificateIssueDate() == null) {
            missingFields.add("Ngày cấp Giấy CNĐK HTX");
        }
        if (registration.getRegistrationCertificateIssuePlace() == null || registration.getRegistrationCertificateIssuePlace().isEmpty()) {
            missingFields.add("Nơi cấp Giấy CNĐK HTX");
        }
        
        // Step 6
        if (registration.getProductTypes() == null || registration.getProductTypes().isEmpty()) {
            missingFields.add("Loại sản phẩm kinh doanh");
        }
        if (registration.getMainProductDescription() == null || registration.getMainProductDescription().isEmpty()) {
            missingFields.add("Mô tả sản phẩm chính");
        }
        if (registration.getBusinessScale() == null) {
            missingFields.add("Quy mô sản xuất/kinh doanh");
        }
        if (registration.getHasSpecialCertification() == null) {
            missingFields.add("Thông tin chứng nhận/giấy phép đặc biệt");
        }
        
        if (!missingFields.isEmpty()) {
            throw new CooperativeException(
                "Vui lòng điền đầy đủ thông tin. Các trường còn thiếu: " + String.join(", ", missingFields)
            );
        }
    }
}

