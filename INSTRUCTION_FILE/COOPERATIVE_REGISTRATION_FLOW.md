# Quy Trình Đăng Ký Hợp Tác Xã (HTX)

## 📋 Tổng Quan

Quy trình đăng ký Hợp tác xã (HTX) là một quy trình nhiều bước, yêu cầu người dùng cung cấp đầy đủ thông tin về HTX, thông tin liên hệ, địa chỉ kinh doanh, thông tin người đại diện, thông tin pháp lý và thông tin kinh doanh.

Sau khi đăng ký thành công và được phê duyệt, user sẽ được nâng cấp từ `CUSTOMER` hoặc `SELLER` lên `COOPERATIVE_MANAGER` trong hệ thống.

---

## 🔐 Điều Kiện Tiên Quyết

### 1. User Phải Đã Đăng Ký và Đăng Nhập
- User phải có tài khoản trong hệ thống (đã đăng ký qua `/api/auth/register`)
- User phải đã đăng nhập và có JWT token hợp lệ
- User phải có `emailVerified = true` (đã xác thực email)
- User có thể là `CUSTOMER` hoặc `SELLER` hiện tại

### 2. Kiểm Tra Trạng Thái
- User không được có đơn đăng ký HTX đang ở trạng thái `PENDING` hoặc `DRAFT`
- Nếu đã có đơn bị `REJECTED`, user có thể tạo đơn mới

---

## 📝 Quy Trình Đăng Ký (6 Bước)

### **BƯỚC 1: Thông Tin HTX Cơ Bản**

**Endpoint:** `POST /api/cooperative/registration/step1`

**Thông tin cần cung cấp:**

| Trường | Kiểu | Bắt buộc | Mô tả | Validation |
|--------|------|----------|-------|------------|
| `cooperativeName` | String | ✅ | Tên HTX | Min: 5, Max: 200 chars |
| `slug` | String | ✅ | Slug URL (unique) | Pattern: `^[a-z0-9-]+$`, Min: 3, Max: 100 |
| `cooperativeCode` | String | ✅ | Mã số HTX | Pattern: `^[A-Z0-9-]+$`, Min: 5, Max: 50, Unique |
| `establishmentDate` | LocalDate | ✅ | Ngày thành lập | Không được là tương lai |
| `cooperativeType` | Enum | ✅ | Loại hình HTX | Xem bảng Loại hình HTX |
| `scale` | Enum | ✅ | Quy mô HTX | Xem bảng Quy mô HTX |
| `shortDescription` | String | ✅ | Mô tả ngắn về HTX | Min: 50, Max: 500 chars |

**Loại hình HTX (CooperativeType):**
- `AGRICULTURAL` - HTX Nông nghiệp
- `INDUSTRIAL` - HTX Công nghiệp
- `SERVICE` - HTX Dịch vụ
- `CONSUMER` - HTX Tiêu dùng
- `CREDIT` - HTX Tín dụng
- `HOUSING` - HTX Nhà ở
- `OTHER` - Loại hình khác

**Quy mô HTX (CooperativeScale):**
- `SMALL` - Quy mô nhỏ (< 50 thành viên)
- `MEDIUM` - Quy mô trung bình (50-200 thành viên)
- `LARGE` - Quy mô lớn (> 200 thành viên)

**Response:**
```json
{
  "status": 200,
  "message": "Lưu thông tin HTX thành công",
  "data": {
    "registrationId": 1,
    "step": 1,
    "status": "DRAFT"
  }
}
```

**Lưu ý:**
- Thông tin được lưu ở trạng thái `DRAFT`
- `slug` và `cooperativeCode` phải unique trong hệ thống
- User có thể lưu nháp và quay lại chỉnh sửa

---

### **BƯỚC 2: Thông Tin Liên Hệ**

**Endpoint:** `POST /api/cooperative/registration/step2`

**Thông tin cần cung cấp:**

| Trường | Kiểu | Bắt buộc | Mô tả | Validation |
|--------|------|----------|-------|------------|
| `contactEmail` | String | ✅ | Email liên hệ | Email format, khác với email đăng nhập |
| `contactPhone` | String | ✅ | Số điện thoại chính | Pattern: `^[0-9]{10,11}$` |
| `contactPhoneAlt` | String | ❌ | Số điện thoại phụ | Pattern: `^[0-9]{10,11}$` |
| `website` | String | ❌ | Website | URL format (http/https) |
| `facebookPage` | String | ❌ | Facebook Page | URL format hoặc username |
| `registrationId` | Long | ✅ | ID đơn đăng ký từ bước 1 | Phải tồn tại và thuộc về user |

**Response:**
```json
{
  "status": 200,
  "message": "Lưu thông tin liên hệ thành công",
  "data": {
    "registrationId": 1,
    "step": 2,
    "status": "DRAFT"
  }
}
```

**Lưu ý:**
- `contactEmail` có thể khác với email đăng nhập của user
- Website và Facebook Page là optional nhưng nên có để tăng độ tin cậy

---

### **BƯỚC 3: Địa Chỉ Kinh Doanh**

**Endpoint:** `POST /api/cooperative/registration/step3`

**Thông tin cần cung cấp:**

| Trường | Kiểu | Bắt buộc | Mô tả | Validation |
|--------|------|----------|-------|------------|
| `fullAddress` | String | ✅ | Địa chỉ đầy đủ | Min: 10, Max: 500 chars |
| `province` | String | ✅ | Tỉnh/Thành phố | Phải có trong danh sách tỉnh/thành |
| `district` | String | ✅ | Quận/Huyện | Phải thuộc tỉnh/thành đã chọn |
| `ward` | String | ✅ | Phường/Xã | Phải thuộc quận/huyện đã chọn |
| `postalCode` | String | ❌ | Mã bưu chính | Pattern: `^[0-9]{5,6}$` |
| `showMap` | Boolean | ✅ | Hiển thị bản đồ | Default: true |
| `latitude` | Double | ❌ | Vĩ độ (nếu có) | Range: -90 to 90 |
| `longitude` | Double | ❌ | Kinh độ (nếu có) | Range: -180 to 180 |
| `registrationId` | Long | ✅ | ID đơn đăng ký | Phải tồn tại và thuộc về user |

**Response:**
```json
{
  "status": 200,
  "message": "Lưu địa chỉ kinh doanh thành công",
  "data": {
    "registrationId": 1,
    "step": 3,
    "status": "DRAFT"
  }
}
```

**Lưu ý:**
- Nếu `showMap = true`, nên cung cấp `latitude` và `longitude` để hiển thị bản đồ chính xác
- Địa chỉ phải đầy đủ và chính xác để phục vụ giao hàng và liên hệ

---

### **BƯỚC 4: Thông Tin Người Đại Diện**

**Endpoint:** `POST /api/cooperative/registration/step4`

**Thông tin cần cung cấp:**

| Trường | Kiểu | Bắt buộc | Mô tả | Validation |
|--------|------|----------|-------|------------|
| `representativeName` | String | ✅ | Họ và tên người đại diện | Min: 5, Max: 100 chars |
| `representativePosition` | String | ✅ | Chức vụ | Min: 3, Max: 100 chars (VD: Chủ nhiệm, Giám đốc) |
| `representativeIdNumber` | String | ✅ | Số CCCD/CMND | Pattern: `^[0-9]{9,12}$` |
| `representativeIdIssueDate` | LocalDate | ✅ | Ngày cấp CCCD | Không được là tương lai |
| `representativeIdIssuePlace` | String | ✅ | Nơi cấp CCCD | Min: 5, Max: 200 chars |
| `representativeEmail` | String | ✅ | Email người đại diện | Email format |
| `representativePhone` | String | ✅ | Số điện thoại người đại diện | Pattern: `^[0-9]{10,11}$` |
| `representativeIdFrontImage` | String | ❌ | Ảnh mặt trước CCCD | URL hoặc base64 |
| `representativeIdBackImage` | String | ❌ | Ảnh mặt sau CCCD | URL hoặc base64 |
| `registrationId` | Long | ✅ | ID đơn đăng ký | Phải tồn tại và thuộc về user |

**Response:**
```json
{
  "status": 200,
  "message": "Lưu thông tin người đại diện thành công",
  "data": {
    "registrationId": 1,
    "step": 4,
    "status": "DRAFT"
  }
}
```

**Lưu ý:**
- Người đại diện có thể là Chủ nhiệm HTX, Giám đốc, hoặc người được ủy quyền
- Nên upload ảnh CCCD để xác minh (optional nhưng khuyến khích)
- Email và số điện thoại người đại diện phải khác với thông tin liên hệ HTX

---

### **BƯỚC 5: Thông Tin Pháp Lý**

**Endpoint:** `POST /api/cooperative/registration/step5`

**Thông tin cần cung cấp:**

| Trường | Kiểu | Bắt buộc | Mô tả | Validation |
|--------|------|----------|-------|------------|
| `taxCode` | String | ✅ | Mã số thuế | Pattern: `^[0-9]{10,13}$`, Unique |
| `registrationCertificateNumber` | String | ✅ | Số Giấy CNĐK HTX | Min: 5, Max: 100 chars, Unique |
| `registrationCertificateIssueDate` | LocalDate | ✅ | Ngày cấp Giấy CNĐK HTX | Không được là tương lai |
| `registrationCertificateIssuePlace` | String | ✅ | Nơi cấp Giấy CNĐK HTX | Min: 5, Max: 200 chars |
| `registrationCertificateImage` | String | ❌ | Ảnh Giấy CNĐK HTX | URL hoặc base64 |
| `taxCodeCertificateImage` | String | ❌ | Ảnh Giấy chứng nhận MST | URL hoặc base64 |
| `registrationId` | Long | ✅ | ID đơn đăng ký | Phải tồn tại và thuộc về user |

**Response:**
```json
{
  "status": 200,
  "message": "Lưu thông tin pháp lý thành công",
  "data": {
    "registrationId": 1,
    "step": 5,
    "status": "DRAFT"
  }
}
```

**Lưu ý:**
- Mã số thuế và số Giấy CNĐK HTX phải unique trong hệ thống
- Nên upload ảnh giấy tờ để xác minh (optional nhưng khuyến khích)
- Thông tin pháp lý sẽ được admin kiểm tra kỹ trước khi phê duyệt

---

### **BƯỚC 6: Thông Tin Kinh Doanh**

**Endpoint:** `POST /api/cooperative/registration/step6`

**Thông tin cần cung cấp:**

| Trường | Kiểu | Bắt buộc | Mô tả | Validation |
|--------|------|----------|-------|------------|
| `productTypes` | List<String> | ✅ | Loại sản phẩm kinh doanh | Min: 1 item, Max: 20 items |
| `mainProductDescription` | String | ✅ | Mô tả sản phẩm chính | Min: 100, Max: 2000 chars |
| `businessScale` | Enum | ✅ | Quy mô sản xuất/kinh doanh | Xem bảng Quy mô Kinh doanh |
| `hasSpecialCertification` | Boolean | ✅ | Có chứng nhận/giấy phép đặc biệt | Default: false |
| `specialCertifications` | List<CertificationDTO> | ❌ | Danh sách chứng nhận đặc biệt | Chỉ cần nếu `hasSpecialCertification = true` |
| `registrationId` | Long | ✅ | ID đơn đăng ký | Phải tồn tại và thuộc về user |

**Quy mô Kinh doanh (BusinessScale):**
- `SMALL` - Quy mô nhỏ (< 1 tỷ VNĐ/năm)
- `MEDIUM` - Quy mô trung bình (1-10 tỷ VNĐ/năm)
- `LARGE` - Quy mô lớn (> 10 tỷ VNĐ/năm)

**CertificationDTO (nếu có chứng nhận đặc biệt):**
```json
{
  "certificateName": "Giấy chứng nhận VietGAP",
  "certificateNumber": "VG-2024-001",
  "issueDate": "2024-01-15",
  "issuePlace": "Bộ Nông nghiệp và Phát triển Nông thôn",
  "certificateImage": "url_or_base64"
}
```

**Response:**
```json
{
  "status": 200,
  "message": "Lưu thông tin kinh doanh thành công",
  "data": {
    "registrationId": 1,
    "step": 6,
    "status": "DRAFT",
    "canSubmit": true
  }
}
```

**Lưu ý:**
- Sau bước 6, user có thể xem lại toàn bộ thông tin và submit đơn
- `canSubmit = true` khi tất cả các bước đã hoàn thành

---

## 📤 Submit Đơn Đăng Ký

**Endpoint:** `POST /api/cooperative/registration/submit`

**Request:**
```json
{
  "registrationId": 1
}
```

**Validation:**
- Tất cả 6 bước phải đã hoàn thành
- Tất cả thông tin bắt buộc phải đã được điền
- Đơn phải ở trạng thái `DRAFT`

**Response:**
```json
{
  "status": 200,
  "message": "Đơn đăng ký HTX đã được gửi thành công. Vui lòng chờ phê duyệt.",
  "data": {
    "registrationId": 1,
    "status": "PENDING",
    "submittedAt": "2024-12-14T10:30:00"
  }
}
```

**Sau khi submit:**
- Đơn chuyển sang trạng thái `PENDING`
- Admin sẽ nhận được thông báo có đơn mới cần duyệt
- User không thể chỉnh sửa đơn nữa (chỉ có thể xem)

---

## 👀 Xem Lại Thông Tin Đơn Đăng Ký

**Endpoint:** `GET /api/cooperative/registration/{registrationId}`

**Response:**
```json
{
  "status": 200,
  "message": "Lấy thông tin đơn đăng ký thành công",
  "data": {
    "registrationId": 1,
    "status": "DRAFT",
    "step": 3,
    "cooperativeInfo": { ... },
    "contactInfo": { ... },
    "addressInfo": { ... },
    "representativeInfo": { ... },
    "legalInfo": { ... },
    "businessInfo": { ... },
    "submittedAt": null,
    "reviewedAt": null,
    "reviewedBy": null,
    "rejectionReason": null
  }
}
```

---

## ✏️ Chỉnh Sửa Đơn Đăng Ký (Chỉ khi DRAFT)

**Endpoint:** `PUT /api/cooperative/registration/{registrationId}/step/{stepNumber}`

**Lưu ý:**
- Chỉ có thể chỉnh sửa khi đơn ở trạng thái `DRAFT`
- Có thể chỉnh sửa từng bước riêng lẻ
- Request body giống như POST ở bước tương ứng

---

## 🗑️ Hủy Đơn Đăng Ký (Chỉ khi DRAFT)

**Endpoint:** `DELETE /api/cooperative/registration/{registrationId}`

**Response:**
```json
{
  "status": 200,
  "message": "Đã hủy đơn đăng ký thành công",
  "data": null
}
```

**Lưu ý:**
- Chỉ có thể hủy khi đơn ở trạng thái `DRAFT`
- Sau khi hủy, user có thể tạo đơn mới

---

## 📋 Danh Sách Đơn Đăng Ký Của User

**Endpoint:** `GET /api/cooperative/registration/my-registrations`

**Query Parameters:**
- `status` (optional): Filter theo status (DRAFT, PENDING, APPROVED, REJECTED)
- `page` (optional): Số trang (default: 0)
- `size` (optional): Số item mỗi trang (default: 10)

**Response:**
```json
{
  "status": 200,
  "message": "Lấy danh sách đơn đăng ký thành công",
  "data": {
    "content": [
      {
        "registrationId": 1,
        "cooperativeName": "HTX Nông nghiệp ABC",
        "status": "PENDING",
        "submittedAt": "2024-12-14T10:30:00",
        "currentStep": 6
      }
    ],
    "totalElements": 1,
    "totalPages": 1,
    "currentPage": 0
  }
}
```

---

## ✅ Phê Duyệt Đơn (Admin Only)

**Endpoint:** `POST /api/admin/cooperative/registration/{registrationId}/approve`

**Request:**
```json
{
  "notes": "Đơn đăng ký hợp lệ, đã kiểm tra đầy đủ giấy tờ"
}
```

**Response:**
```json
{
  "status": 200,
  "message": "Đã phê duyệt đơn đăng ký HTX thành công",
  "data": {
    "registrationId": 1,
    "status": "APPROVED",
    "reviewedAt": "2024-12-15T09:00:00",
    "reviewedBy": 1
  }
}
```

**Sau khi phê duyệt:**
1. User được nâng cấp `userType` từ `CUSTOMER`/`SELLER` lên `COOPERATIVE_MANAGER`
2. User được gán role `COOPERATIVE_MANAGER` trong Keycloak
3. Tạo Store entity cho HTX (nếu chưa có)
4. Gửi email thông báo phê duyệt cho user
5. User có thể truy cập các chức năng dành cho HTX

---

## ❌ Từ Chối Đơn (Admin Only)

**Endpoint:** `POST /api/admin/cooperative/registration/{registrationId}/reject`

**Request:**
```json
{
  "rejectionReason": "Thiếu giấy tờ pháp lý, vui lòng bổ sung và gửi lại"
}
```

**Response:**
```json
{
  "status": 200,
  "message": "Đã từ chối đơn đăng ký HTX",
  "data": {
    "registrationId": 1,
    "status": "REJECTED",
    "rejectionReason": "Thiếu giấy tờ pháp lý, vui lòng bổ sung và gửi lại",
    "reviewedAt": "2024-12-15T09:00:00",
    "reviewedBy": 1
  }
}
```

**Sau khi từ chối:**
- Đơn chuyển sang trạng thái `REJECTED`
- User nhận được email thông báo với lý do từ chối
- User có thể tạo đơn mới sau khi khắc phục vấn đề

---

## 🔄 Tạo Lại Đơn Sau Khi Bị Từ Chối

**Endpoint:** `POST /api/cooperative/registration/create-from-rejected/{rejectedRegistrationId}`

**Lưu ý:**
- Chỉ có thể tạo lại từ đơn bị `REJECTED`
- Thông tin từ đơn cũ sẽ được copy sang đơn mới (trạng thái `DRAFT`)
- User có thể chỉnh sửa và submit lại

---

## 📊 Entity Structure (Dự Kiến)

### CooperativeRegistration Entity

```java
@Entity
@Table(name = "cooperative_registrations")
public class CooperativeRegistration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    // Step 1: Thông tin HTX
    private String cooperativeName;
    private String slug;
    private String cooperativeCode;
    private LocalDate establishmentDate;
    private CooperativeType cooperativeType;
    private CooperativeScale scale;
    private String shortDescription;
    
    // Step 2: Thông tin liên hệ
    private String contactEmail;
    private String contactPhone;
    private String contactPhoneAlt;
    private String website;
    private String facebookPage;
    
    // Step 3: Địa chỉ kinh doanh
    private String fullAddress;
    private String province;
    private String district;
    private String ward;
    private String postalCode;
    private Boolean showMap;
    private Double latitude;
    private Double longitude;
    
    // Step 4: Thông tin người đại diện
    private String representativeName;
    private String representativePosition;
    private String representativeIdNumber;
    private LocalDate representativeIdIssueDate;
    private String representativeIdIssuePlace;
    private String representativeEmail;
    private String representativePhone;
    private String representativeIdFrontImage;
    private String representativeIdBackImage;
    
    // Step 5: Thông tin pháp lý
    private String taxCode;
    private String registrationCertificateNumber;
    private LocalDate registrationCertificateIssueDate;
    private String registrationCertificateIssuePlace;
    private String registrationCertificateImage;
    private String taxCodeCertificateImage;
    
    // Step 6: Thông tin kinh doanh
    @ElementCollection
    private List<String> productTypes;
    private String mainProductDescription;
    private BusinessScale businessScale;
    private Boolean hasSpecialCertification;
    
    // Status và tracking
    @Enumerated(EnumType.STRING)
    private ApprovalStatus status = ApprovalStatus.DRAFT;
    private Integer currentStep = 0;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    private Long reviewedBy;
    private String rejectionReason;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
}
```

### Enums

```java
public enum CooperativeType {
    AGRICULTURAL,    // HTX Nông nghiệp
    INDUSTRIAL,      // HTX Công nghiệp
    SERVICE,         // HTX Dịch vụ
    CONSUMER,        // HTX Tiêu dùng
    CREDIT,          // HTX Tín dụng
    HOUSING,         // HTX Nhà ở
    OTHER            // Loại hình khác
}

public enum CooperativeScale {
    SMALL,   // < 50 thành viên
    MEDIUM,  // 50-200 thành viên
    LARGE    // > 200 thành viên
}

public enum BusinessScale {
    SMALL,   // < 1 tỷ VNĐ/năm
    MEDIUM,  // 1-10 tỷ VNĐ/năm
    LARGE    // > 10 tỷ VNĐ/năm
}
```

---

## 🔐 Security & Authorization

### Endpoints Yêu Cầu Authentication
- Tất cả endpoints `/api/cooperative/registration/**` yêu cầu JWT token
- User chỉ có thể xem/chỉnh sửa đơn của chính mình
- Admin endpoints (`/api/admin/cooperative/registration/**`) yêu cầu role `SUPER_ADMIN` hoặc `ADMIN`

### Validation Rules
- Mỗi bước có validation riêng
- Không thể submit nếu thiếu thông tin bắt buộc
- Không thể chỉnh sửa đơn đã submit (trừ khi bị reject)

---

## 📧 Email Notifications

### 1. Khi Submit Đơn
- Gửi email xác nhận cho user: "Đơn đăng ký HTX của bạn đã được gửi thành công"
- Gửi email thông báo cho admin: "Có đơn đăng ký HTX mới cần duyệt"

### 2. Khi Phê Duyệt
- Gửi email cho user: "Đơn đăng ký HTX của bạn đã được phê duyệt. Bạn đã trở thành COOPERATIVE_MANAGER"

### 3. Khi Từ Chối
- Gửi email cho user: "Đơn đăng ký HTX của bạn đã bị từ chối. Lý do: [rejectionReason]"

---

## 🎯 Tóm Tắt Quy Trình

```
1. User đăng nhập
   ↓
2. Bắt đầu đăng ký HTX (Bước 1-6)
   ↓
3. Lưu nháp (DRAFT) hoặc Submit (PENDING)
   ↓
4. Admin xem và phê duyệt/từ chối
   ↓
5a. Nếu APPROVED:
    - User → COOPERATIVE_MANAGER
    - Tạo Store
    - Gửi email thông báo
   ↓
5b. Nếu REJECTED:
    - User nhận lý do từ chối
    - Có thể tạo đơn mới
```

---

## 📝 Lưu Ý Quan Trọng

1. **Multi-step Form**: User có thể lưu nháp và quay lại chỉnh sửa bất kỳ lúc nào (trước khi submit)

2. **Data Validation**: Mỗi bước có validation riêng, nhưng validation tổng thể chỉ khi submit

3. **File Upload**: Ảnh giấy tờ có thể upload qua endpoint riêng hoặc base64 trong request

4. **Unique Constraints**: 
   - `slug` phải unique
   - `cooperativeCode` phải unique
   - `taxCode` phải unique
   - `registrationCertificateNumber` phải unique

5. **Status Flow**: 
   - `DRAFT` → User đang điền form
   - `PENDING` → Đã submit, chờ admin duyệt
   - `APPROVED` → Đã được phê duyệt
   - `REJECTED` → Bị từ chối

6. **Role Upgrade**: Sau khi APPROVED, user được nâng cấp role trong cả Database và Keycloak

---

## 🔄 Tương Đồng Với Seller Registration

Quy trình đăng ký HTX tương tự như `SellerRegistration` nhưng:
- **Phức tạp hơn**: 6 bước thay vì 1 form đơn giản
- **Nhiều thông tin hơn**: Thông tin pháp lý, người đại diện, địa chỉ chi tiết
- **Yêu cầu cao hơn**: Cần giấy tờ pháp lý, chứng nhận
- **Quy trình phê duyệt**: Admin phải kiểm tra kỹ trước khi phê duyệt

