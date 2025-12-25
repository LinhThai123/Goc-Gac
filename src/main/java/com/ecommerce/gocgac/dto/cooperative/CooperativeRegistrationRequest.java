package com.ecommerce.gocgac.dto.cooperative;

import com.ecommerce.gocgac.entity.enums.BusinessScale;
import com.ecommerce.gocgac.entity.enums.CooperativeScale;
import com.ecommerce.gocgac.entity.enums.CooperativeType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO cho đăng ký HTX - 1 request duy nhất chứa tất cả thông tin
 * Đơn giản hóa so với multi-step approach
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CooperativeRegistrationRequest {
    
    // ========== STEP 1: Thông tin HTX cơ bản ==========
    @NotBlank(message = "Tên HTX không được để trống")
    @Size(min = 5, max = 200, message = "Tên HTX phải từ 5 đến 200 ký tự")
    private String cooperativeName;
    
    @NotBlank(message = "Slug không được để trống")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug chỉ được chứa chữ thường, số và dấu gạch ngang")
    @Size(min = 3, max = 100, message = "Slug phải từ 3 đến 100 ký tự")
    private String slug;
    
    @NotBlank(message = "Mã số HTX không được để trống")
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "Mã số HTX chỉ được chứa chữ hoa, số và dấu gạch ngang")
    @Size(min = 5, max = 50, message = "Mã số HTX phải từ 5 đến 50 ký tự")
    private String cooperativeCode;
    
    @NotNull(message = "Ngày thành lập không được để trống")
    @PastOrPresent(message = "Ngày thành lập không được là tương lai")
    private LocalDate establishmentDate;
    
    @NotNull(message = "Loại hình HTX không được để trống")
    private CooperativeType cooperativeType;
    
    @NotNull(message = "Quy mô HTX không được để trống")
    private CooperativeScale scale;
    
    @NotBlank(message = "Mô tả ngắn không được để trống")
    @Size(min = 50, max = 500, message = "Mô tả ngắn phải từ 50 đến 500 ký tự")
    private String shortDescription;
    
    // ========== STEP 2: Thông tin liên hệ ==========
    @NotBlank(message = "Email liên hệ không được để trống")
    @Email(message = "Email không hợp lệ")
    private String contactEmail;
    
    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^[0-9]{10,11}$", message = "Số điện thoại không hợp lệ")
    private String contactPhone;
    
    @Pattern(regexp = "^[0-9]{10,11}$", message = "Số điện thoại phụ không hợp lệ")
    private String contactPhoneAlt;
    
    @URL(message = "Website không hợp lệ")
    private String website;
    
    private String facebookPage;
    
    // ========== STEP 3: Địa chỉ kinh doanh ==========
    @NotBlank(message = "Địa chỉ đầy đủ không được để trống")
    @Size(max = 500, message = "Địa chỉ không được vượt quá 500 ký tự")
    private String fullAddress;
    
    @NotBlank(message = "Tỉnh/Thành phố không được để trống")
    @Size(max = 100, message = "Tỉnh/Thành phố không được vượt quá 100 ký tự")
    private String province;
    
    @NotBlank(message = "Quận/Huyện không được để trống")
    @Size(max = 100, message = "Quận/Huyện không được vượt quá 100 ký tự")
    private String district;
    
    @NotBlank(message = "Phường/Xã không được để trống")
    @Size(max = 100, message = "Phường/Xã không được vượt quá 100 ký tự")
    private String ward;
    
    @Size(max = 20, message = "Mã bưu chính không được vượt quá 20 ký tự")
    private String postalCode;
    
    @NotNull(message = "Hiển thị bản đồ không được để trống")
    private Boolean showMap;
    
    @DecimalMin(value = "-90.0", message = "Vĩ độ không hợp lệ")
    @DecimalMax(value = "90.0", message = "Vĩ độ không hợp lệ")
    private Double latitude;
    
    @DecimalMin(value = "-180.0", message = "Kinh độ không hợp lệ")
    @DecimalMax(value = "180.0", message = "Kinh độ không hợp lệ")
    private Double longitude;
    
    // ========== STEP 4: Thông tin người đại diện ==========
    @NotBlank(message = "Họ và tên người đại diện không được để trống")
    @Size(max = 200, message = "Họ và tên không được vượt quá 200 ký tự")
    private String representativeName;
    
    @NotBlank(message = "Chức vụ không được để trống")
    @Size(max = 100, message = "Chức vụ không được vượt quá 100 ký tự")
    private String representativePosition;
    
    @NotBlank(message = "Số CCCD/CMND không được để trống")
    @Size(max = 50, message = "Số CCCD/CMND không được vượt quá 50 ký tự")
    private String representativeIdNumber;
    
    @NotNull(message = "Ngày cấp CCCD không được để trống")
    @PastOrPresent(message = "Ngày cấp không được là tương lai")
    private LocalDate representativeIdIssueDate;
    
    @NotBlank(message = "Nơi cấp CCCD không được để trống")
    @Size(max = 200, message = "Nơi cấp không được vượt quá 200 ký tự")
    private String representativeIdIssuePlace;
    
    @NotBlank(message = "Email người đại diện không được để trống")
    @Email(message = "Email không hợp lệ")
    private String representativeEmail;
    
    @NotBlank(message = "Số điện thoại người đại diện không được để trống")
    @Pattern(regexp = "^[0-9]{10,11}$", message = "Số điện thoại không hợp lệ")
    private String representativePhone;
    
    @URL(message = "URL ảnh mặt trước CCCD không hợp lệ")
    private String representativeIdFrontImage;
    
    @URL(message = "URL ảnh mặt sau CCCD không hợp lệ")
    private String representativeIdBackImage;
    
    // ========== STEP 5: Thông tin pháp lý ==========
    @Size(max = 50, message = "Mã số thuế không được vượt quá 50 ký tự")
    private String taxCode;
    
    @NotBlank(message = "Số Giấy CNĐK HTX không được để trống")
    @Size(max = 100, message = "Số Giấy CNĐK HTX không được vượt quá 100 ký tự")
    private String registrationCertificateNumber;
    
    @NotNull(message = "Ngày cấp Giấy CNĐK HTX không được để trống")
    @PastOrPresent(message = "Ngày cấp không được là tương lai")
    private LocalDate registrationCertificateIssueDate;
    
    @NotBlank(message = "Nơi cấp Giấy CNĐK HTX không được để trống")
    @Size(max = 200, message = "Nơi cấp không được vượt quá 200 ký tự")
    private String registrationCertificateIssuePlace;
    
    @URL(message = "URL ảnh Giấy CNĐK HTX không hợp lệ")
    private String registrationCertificateImage;
    
    @URL(message = "URL ảnh Giấy chứng nhận mã số thuế không hợp lệ")
    private String taxCodeCertificateImage;
    
    // ========== STEP 6: Thông tin kinh doanh ==========
    @NotEmpty(message = "Loại sản phẩm kinh doanh không được để trống")
    @Size(min = 1, max = 20, message = "Phải có ít nhất 1 loại sản phẩm và tối đa 20 loại")
    private List<@NotBlank(message = "Loại sản phẩm không được để trống") String> productTypes;
    
    @NotBlank(message = "Mô tả sản phẩm chính không được để trống")
    @Size(min = 50, max = 2000, message = "Mô tả sản phẩm chính phải từ 50 đến 2000 ký tự")
    private String mainProductDescription;
    
    @NotNull(message = "Quy mô sản xuất/kinh doanh không được để trống")
    private BusinessScale businessScale;
    
    @NotNull(message = "Thông tin chứng nhận/giấy phép đặc biệt không được để trống")
    private Boolean hasSpecialCertification;
    
    @Size(max = 1000, message = "Chi tiết chứng nhận không được vượt quá 1000 ký tự")
    private String specialCertificationDetails;
}

