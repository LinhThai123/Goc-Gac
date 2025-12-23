# Các Bước Thực Hiện Chức Năng Đăng Ký Hợp Tác Xã (HTX)

## 📋 Tổng Quan

Tài liệu này liệt kê chi tiết các bước cần thực hiện để implement chức năng đăng ký HTX với 6 bước thông tin, tương tự như SellerRegistration nhưng phức tạp hơn.

---

## 🎯 Mục Tiêu

1. Tạo entity để lưu quá trình đăng ký HTX (6 bước)
2. Tạo entity để lưu thông tin HTX sau khi được phê duyệt
3. Implement API endpoints cho 6 bước đăng ký
4. Implement approval process (Admin review)
5. Tự động nâng cấp user lên `COOPERATIVE_MANAGER` sau khi approve

---

## 📝 Các Bước Thực Hiện

### **PHASE 1: Tạo Enums và Entity Classes**

#### **Bước 1.1: Tạo Enums Mới**

**File:** `src/main/java/com/ecommerce/gocgac/entity/enums/CooperativeType.java`

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
```

**File:** `src/main/java/com/ecommerce/gocgac/entity/enums/CooperativeScale.java`

```java
public enum CooperativeScale {
    SMALL,   // < 50 thành viên
    MEDIUM,  // 50-200 thành viên
    LARGE    // > 200 thành viên
}
```

**File:** `src/main/java/com/ecommerce/gocgac/entity/enums/BusinessScale.java`

```java
public enum BusinessScale {
    SMALL,   // < 1 tỷ VNĐ/năm
    MEDIUM,  // 1-10 tỷ VNĐ/năm
    LARGE    // > 10 tỷ VNĐ/năm
}
```

**Lưu ý:** `ApprovalStatus` enum đã có sẵn (DRAFT, PENDING, APPROVED, REJECTED)

---

#### **Bước 1.2: Tạo CooperativeRegistration Entity**

**File:** `src/main/java/com/ecommerce/gocgac/entity/CooperativeRegistration.java`

**Các trường cần có:**
- `id` (Long, Primary Key)
- `userId` (Long, Foreign Key → users.id)
- **Step 1 - Thông tin HTX:**
  - `cooperativeName` (String, NOT NULL)
  - `slug` (String, UNIQUE, NOT NULL)
  - `cooperativeCode` (String, UNIQUE, NOT NULL)
  - `establishmentDate` (LocalDate, NOT NULL)
  - `cooperativeType` (CooperativeType enum, NOT NULL)
  - `scale` (CooperativeScale enum, NOT NULL)
  - `shortDescription` (String, TEXT, NOT NULL)
- **Step 2 - Thông tin liên hệ:**
  - `contactEmail` (String, NOT NULL)
  - `contactPhone` (String, NOT NULL)
  - `contactPhoneAlt` (String, nullable)
  - `website` (String, nullable)
  - `facebookPage` (String, nullable)
- **Step 3 - Địa chỉ kinh doanh:**
  - `fullAddress` (String, TEXT, NOT NULL)
  - `province` (String, NOT NULL)
  - `district` (String, NOT NULL)
  - `ward` (String, NOT NULL)
  - `postalCode` (String, nullable)
  - `showMap` (Boolean, default false)
  - `latitude` (Double, nullable)
  - `longitude` (Double, nullable)
- **Step 4 - Thông tin người đại diện:**
  - `representativeName` (String, NOT NULL)
  - `representativePosition` (String, NOT NULL)
  - `representativeIdNumber` (String, NOT NULL)
  - `representativeIdIssueDate` (LocalDate, NOT NULL)
  - `representativeIdIssuePlace` (String, NOT NULL)
  - `representativeEmail` (String, NOT NULL)
  - `representativePhone` (String, NOT NULL)
  - `representativeIdFrontImage` (String, nullable) - URL to image
  - `representativeIdBackImage` (String, nullable) - URL to image
- **Step 5 - Thông tin pháp lý:**
  - `taxCode` (String, UNIQUE, nullable)
  - `registrationCertificateNumber` (String, NOT NULL)
  - `registrationCertificateIssueDate` (LocalDate, NOT NULL)
  - `registrationCertificateIssuePlace` (String, NOT NULL)
  - `registrationCertificateImage` (String, nullable) - URL to image
  - `taxCodeCertificateImage` (String, nullable) - URL to image
- **Step 6 - Thông tin kinh doanh:**
  - `productTypes` (List<String>, @ElementCollection) - Danh sách loại sản phẩm
  - `mainProductDescription` (String, TEXT, NOT NULL)
  - `businessScale` (BusinessScale enum, NOT NULL)
  - `hasSpecialCertification` (Boolean, default false)
  - `specialCertificationDetails` (String, TEXT, nullable)
- **Status và tracking:**
  - `status` (ApprovalStatus, default DRAFT)
  - `currentStep` (Integer, default 0) - Bước hiện tại đã hoàn thành (0-6)
  - `submittedAt` (LocalDateTime, nullable) - Thời điểm submit
  - `reviewedAt` (LocalDateTime, nullable)
  - `reviewedBy` (Long, nullable) - Admin ID
  - `rejectionReason` (String, TEXT, nullable)
  - `createdAt` (LocalDateTime, NOT NULL)
  - `updatedAt` (LocalDateTime, NOT NULL)

**Tham khảo:** `SellerRegistration.java` để xem cấu trúc tương tự

---

#### **Bước 1.3: Tạo Cooperative Entity**

**File:** `src/main/java/com/ecommerce/gocgac/entity/Cooperative.java`

**Các trường cần có:**
- `id` (Long, Primary Key)
- `userId` (Long, UNIQUE, Foreign Key → users.id)
- `registrationId` (Long, Foreign Key → cooperative_registrations.id) - Reference đến registration đã approve
- **Copy tất cả thông tin từ CooperativeRegistration** (sau khi approve)
- **Thông tin bổ sung:**
  - `logoUrl` (String, nullable)
  - `bannerUrl` (String, nullable)
  - `videoUrl` (String, nullable)
  - `isVerified` (Boolean, default false)
  - `isActive` (Boolean, default true)
- **Timestamps:**
  - `createdAt` (LocalDateTime, NOT NULL)
  - `updatedAt` (LocalDateTime, NOT NULL)

**Tham khảo:** `Store.java` để xem cấu trúc tương tự

---

### **PHASE 2: Tạo Repository Classes**

#### **Bước 2.1: Tạo CooperativeRegistrationRepository**

**File:** `src/main/java/com/ecommerce/gocgac/repository/CooperativeRegistrationRepository.java`

**Methods cần có:**
```java
public interface CooperativeRegistrationRepository extends JpaRepository<CooperativeRegistration, Long> {
    // Tìm registration của user
    Optional<CooperativeRegistration> findByUserId(Long userId);
    
    // Tìm registration theo status
    List<CooperativeRegistration> findByStatus(ApprovalStatus status);
    
    // Tìm registration đang pending hoặc draft của user
    Optional<CooperativeRegistration> findByUserIdAndStatusIn(
        Long userId, 
        List<ApprovalStatus> statuses
    );
    
    // Kiểm tra slug đã tồn tại chưa
    boolean existsBySlug(String slug);
    
    // Kiểm tra cooperativeCode đã tồn tại chưa
    boolean existsByCooperativeCode(String cooperativeCode);
    
    // Kiểm tra taxCode đã tồn tại chưa (nếu có)
    boolean existsByTaxCode(String taxCode);
    
    // Tìm registration theo slug
    Optional<CooperativeRegistration> findBySlug(String slug);
}
```

#### **Bước 2.2: Tạo CooperativeRepository**

**File:** `src/main/java/com/ecommerce/gocgac/repository/CooperativeRepository.java`

**Methods cần có:**
```java
public interface CooperativeRepository extends JpaRepository<Cooperative, Long> {
    // Tìm cooperative của user
    Optional<Cooperative> findByUserId(Long userId);
    
    // Kiểm tra user đã có cooperative chưa
    boolean existsByUserId(Long userId);
    
    // Tìm cooperative theo slug
    Optional<Cooperative> findBySlug(String slug);
    
    // Tìm cooperative theo cooperativeCode
    Optional<Cooperative> findByCooperativeCode(String cooperativeCode);
}
```

---

### **PHASE 3: Tạo DTO Classes**

#### **Bước 3.1: Tạo DTOs cho 6 bước đăng ký**

**File:** `src/main/java/com/ecommerce/gocgac/dto/cooperative/Step1Request.java`
- `cooperativeName`, `slug`, `cooperativeCode`, `establishmentDate`, `cooperativeType`, `scale`, `shortDescription`

**File:** `src/main/java/com/ecommerce/gocgac/dto/cooperative/Step2Request.java`
- `registrationId`, `contactEmail`, `contactPhone`, `contactPhoneAlt`, `website`, `facebookPage`

**File:** `src/main/java/com/ecommerce/gocgac/dto/cooperative/Step3Request.java`
- `registrationId`, `fullAddress`, `province`, `district`, `ward`, `postalCode`, `showMap`, `latitude`, `longitude`

**File:** `src/main/java/com/ecommerce/gocgac/dto/cooperative/Step4Request.java`
- `registrationId`, `representativeName`, `representativePosition`, `representativeIdNumber`, `representativeIdIssueDate`, `representativeIdIssuePlace`, `representativeEmail`, `representativePhone`, `representativeIdFrontImage`, `representativeIdBackImage`

**File:** `src/main/java/com/ecommerce/gocgac/dto/cooperative/Step5Request.java`
- `registrationId`, `taxCode`, `registrationCertificateNumber`, `registrationCertificateIssueDate`, `registrationCertificateIssuePlace`, `registrationCertificateImage`, `taxCodeCertificateImage`

**File:** `src/main/java/com/ecommerce/gocgac/dto/cooperative/Step6Request.java`
- `registrationId`, `productTypes` (List<String>), `mainProductDescription`, `businessScale`, `hasSpecialCertification`, `specialCertificationDetails`

#### **Bước 3.2: Tạo Response DTOs**

**File:** `src/main/java/com/ecommerce/gocgac/dto/cooperative/RegistrationStepResponse.java`
- `registrationId`, `step`, `status`, `message`

**File:** `src/main/java/com/ecommerce/gocgac/dto/cooperative/RegistrationDetailResponse.java`
- Tất cả thông tin từ 6 bước + status + timestamps

**File:** `src/main/java/com/ecommerce/gocgac/dto/cooperative/CooperativeResponse.java`
- Thông tin HTX sau khi approve (từ Cooperative entity)

#### **Bước 3.3: Tạo DTO cho Submit và Admin Review**

**File:** `src/main/java/com/ecommerce/gocgac/dto/cooperative/SubmitRegistrationRequest.java`
- `registrationId` (Long)

**File:** `src/main/java/com/ecommerce/gocgac/dto/admin/ReviewCooperativeRequest.java`
- `registrationId` (Long)
- `status` (ApprovalStatus) - APPROVED hoặc REJECTED
- `rejectionReason` (String, nullable) - Bắt buộc nếu REJECTED

---

### **PHASE 4: Tạo Service Classes**

#### **Bước 4.1: Tạo CooperativeRegistrationService**

**File:** `src/main/java/com/ecommerce/gocgac/service/cooperative/CooperativeRegistrationService.java`

**Methods cần implement:**

1. **`createOrUpdateStep1(Long userId, Step1Request request)`**
   - Kiểm tra user đã có registration DRAFT/PENDING chưa
   - Nếu chưa → tạo mới với status DRAFT
   - Nếu có → update thông tin step 1
   - Validate: slug unique, cooperativeCode unique
   - Set `currentStep = 1`

2. **`updateStep2(Long userId, Step2Request request)`**
   - Tìm registration theo `registrationId` và `userId`
   - Validate registration thuộc về user
   - Update thông tin step 2
   - Set `currentStep = 2`

3. **`updateStep3(Long userId, Step3Request request)`**
   - Tương tự step 2
   - Set `currentStep = 3`

4. **`updateStep4(Long userId, Step4Request request)`**
   - Tương tự step 2
   - Set `currentStep = 4`

5. **`updateStep5(Long userId, Step5Request request)`**
   - Tương tự step 2
   - Validate taxCode unique (nếu có)
   - Set `currentStep = 5`

6. **`updateStep6(Long userId, Step6Request request)`**
   - Tương tự step 2
   - Set `currentStep = 6`

7. **`submitRegistration(Long userId, Long registrationId)`**
   - Validate tất cả 6 bước đã điền đầy đủ
   - Validate status = DRAFT
   - Set status = PENDING
   - Set `submittedAt = now()`
   - Gửi email notification cho admin (nếu có)

8. **`getRegistrationDetail(Long userId, Long registrationId)`**
   - Lấy thông tin chi tiết registration
   - Validate user có quyền xem

9. **`getMyRegistration(Long userId)`**
   - Lấy registration hiện tại của user (DRAFT hoặc PENDING)

10. **`canCreateNewRegistration(Long userId)`**
    - Kiểm tra user có thể tạo registration mới không
    - Return false nếu đang có DRAFT hoặc PENDING

#### **Bước 4.2: Tạo CooperativeService**

**File:** `src/main/java/com/ecommerce/gocgac/service/cooperative/CooperativeService.java`

**Methods cần implement:**

1. **`approveRegistration(Long registrationId, Long adminId)`**
   - Tìm registration theo ID và status = PENDING
   - Validate tất cả thông tin đã đầy đủ
   - Tạo `Cooperative` entity từ `CooperativeRegistration`
   - Copy tất cả thông tin từ registration → cooperative
   - Set `registration.status = APPROVED`
   - Set `registration.reviewedAt = now()`
   - Set `registration.reviewedBy = adminId`
   - **Nâng cấp User:**
     - Update `User.userType = COOPERATIVE_MANAGER`
     - Assign role `COOPERATIVE_MANAGER` trong Keycloak (dùng `KeycloakClient`)
   - Gửi email notification cho user

2. **`rejectRegistration(Long registrationId, Long adminId, String reason)`**
   - Set `registration.status = REJECTED`
   - Set `registration.rejectionReason = reason`
   - Set `registration.reviewedAt = now()`
   - Set `registration.reviewedBy = adminId`
   - Gửi email notification cho user

3. **`getCooperativeByUserId(Long userId)`**
   - Lấy thông tin cooperative của user

4. **`getCooperativeBySlug(String slug)`**
   - Lấy thông tin cooperative theo slug (public)

5. **`updateCooperative(Long userId, UpdateCooperativeRequest request)`**
   - Update thông tin cooperative (logo, banner, description, etc.)
   - Chỉ cho phép update một số trường nhất định

---

### **PHASE 5: Tạo Controller Classes**

#### **Bước 5.1: Tạo CooperativeRegistrationController**

**File:** `src/main/java/com/ecommerce/gocgac/controller/cooperative/CooperativeRegistrationController.java`

**Endpoints:**

1. **`POST /api/cooperative/registration/step1`**
   - Authentication: Required
   - Body: `Step1Request`
   - Response: `RegistrationStepResponse`

2. **`POST /api/cooperative/registration/step2`**
   - Authentication: Required
   - Body: `Step2Request`
   - Response: `RegistrationStepResponse`

3. **`POST /api/cooperative/registration/step3`**
   - Authentication: Required
   - Body: `Step3Request`
   - Response: `RegistrationStepResponse`

4. **`POST /api/cooperative/registration/step4`**
   - Authentication: Required
   - Body: `Step4Request`
   - Response: `RegistrationStepResponse`

5. **`POST /api/cooperative/registration/step5`**
   - Authentication: Required
   - Body: `Step5Request`
   - Response: `RegistrationStepResponse`

6. **`POST /api/cooperative/registration/step6`**
   - Authentication: Required
   - Body: `Step6Request`
   - Response: `RegistrationStepResponse`

7. **`POST /api/cooperative/registration/submit`**
   - Authentication: Required
   - Body: `SubmitRegistrationRequest`
   - Response: `MessageResponse`

8. **`GET /api/cooperative/registration/my-registration`**
   - Authentication: Required
   - Response: `RegistrationDetailResponse`

9. **`GET /api/cooperative/registration/{registrationId}`**
   - Authentication: Required
   - Response: `RegistrationDetailResponse`
   - Validate: User chỉ xem được registration của mình

#### **Bước 5.2: Tạo AdminCooperativeController**

**File:** `src/main/java/com/ecommerce/gocgac/controller/admin/AdminCooperativeController.java`

**Endpoints:**

1. **`GET /api/admin/cooperative/registrations`**
   - Authentication: Required (SUPER_ADMIN hoặc ADMIN)
   - Query params: `status` (optional), `page`, `size`
   - Response: `Page<RegistrationDetailResponse>`

2. **`GET /api/admin/cooperative/registrations/{registrationId}`**
   - Authentication: Required (SUPER_ADMIN hoặc ADMIN)
   - Response: `RegistrationDetailResponse`

3. **`POST /api/admin/cooperative/registrations/{registrationId}/approve`**
   - Authentication: Required (SUPER_ADMIN hoặc ADMIN)
   - Body: `ReviewCooperativeRequest` (status = APPROVED)
   - Response: `MessageResponse`

4. **`POST /api/admin/cooperative/registrations/{registrationId}/reject`**
   - Authentication: Required (SUPER_ADMIN hoặc ADMIN)
   - Body: `ReviewCooperativeRequest` (status = REJECTED, rejectionReason required)
   - Response: `MessageResponse`

#### **Bước 5.3: Tạo CooperativeController (Public)**

**File:** `src/main/java/com/ecommerce/gocgac/controller/cooperative/CooperativeController.java`

**Endpoints:**

1. **`GET /api/cooperative/my-cooperative`**
   - Authentication: Required (COOPERATIVE_MANAGER)
   - Response: `CooperativeResponse`

2. **`GET /api/cooperative/{slug}`**
   - Authentication: Optional (public)
   - Response: `CooperativeResponse`

3. **`PUT /api/cooperative/update`**
   - Authentication: Required (COOPERATIVE_MANAGER)
   - Body: `UpdateCooperativeRequest`
   - Response: `CooperativeResponse`

---

### **PHASE 6: Database Migration**

#### **Bước 6.1: Tạo Migration SQL**

**File:** `src/main/resources/db/migration/V{version}__Create_cooperative_tables.sql`

**Nội dung:**
1. Tạo bảng `cooperative_registrations` với tất cả columns
2. Tạo bảng `cooperatives` với tất cả columns
3. Tạo indexes:
   - `idx_cooperative_registrations_user_id`
   - `idx_cooperative_registrations_status`
   - `idx_cooperative_registrations_slug` (UNIQUE)
   - `idx_cooperative_registrations_cooperative_code` (UNIQUE)
   - `idx_cooperatives_user_id` (UNIQUE)
   - `idx_cooperatives_slug` (UNIQUE)
   - `idx_cooperatives_cooperative_code` (UNIQUE)
4. Tạo foreign keys:
   - `cooperative_registrations.user_id` → `users.id`
   - `cooperatives.user_id` → `users.id`
   - `cooperatives.registration_id` → `cooperative_registrations.id`

---

### **PHASE 7: Cập Nhật Security Config**

#### **Bước 7.1: Cập nhật SecurityConfig**

**File:** `src/main/java/com/ecommerce/gocgac/config/SecurityConfig.java`

**Thêm các rules:**
```java
.requestMatchers("/api/cooperative/registration/**").authenticated()
.requestMatchers("/api/cooperative/my-cooperative", "/api/cooperative/update").hasAnyRole("COOPERATIVE_MANAGER", "SUPER_ADMIN")
.requestMatchers("/api/cooperative/**").permitAll() // Public endpoints
.requestMatchers("/api/admin/cooperative/**").hasAnyRole("SUPER_ADMIN", "ADMIN")
```

---

### **PHASE 8: Validation và Error Handling**

#### **Bước 8.1: Tạo Custom Validators (nếu cần)**

- Validator cho slug format
- Validator cho cooperativeCode format
- Validator cho phone number
- Validator cho email

#### **Bước 8.2: Tạo Custom Exceptions**

**File:** `src/main/java/com/ecommerce/gocgac/exception/CooperativeException.java`

**Các exception types:**
- `RegistrationNotFoundException`
- `RegistrationAlreadySubmittedException`
- `InvalidRegistrationStepException`
- `CooperativeAlreadyExistsException`
- `SlugAlreadyExistsException`
- `CooperativeCodeAlreadyExistsException`

---

### **PHASE 9: Integration với Keycloak**

#### **Bước 9.1: Cập nhật KeycloakClient (nếu cần)**

- Đảm bảo có method `assignRoleToUser()` để assign role `COOPERATIVE_MANAGER`
- Method này đã có sẵn trong `KeycloakClient.java`

#### **Bước 9.2: Cập nhật UserService (nếu cần)**

- Method để update `User.userType`
- Method để sync với Keycloak

---

### **PHASE 10: Testing**

#### **Bước 10.1: Unit Tests**

- Test `CooperativeRegistrationService` methods
- Test `CooperativeService` methods
- Test validation logic

#### **Bước 10.2: Integration Tests**

- Test full registration flow (6 steps)
- Test approval process
- Test user role upgrade

#### **Bước 10.3: Manual Testing**

- Test trên Swagger UI
- Test các edge cases
- Test error handling

---

## 📋 Checklist Implementation

### Phase 1: Enums & Entities
- [ ] Tạo `CooperativeType` enum
- [ ] Tạo `CooperativeScale` enum
- [ ] Tạo `BusinessScale` enum
- [ ] Tạo `CooperativeRegistration` entity
- [ ] Tạo `Cooperative` entity

### Phase 2: Repositories
- [ ] Tạo `CooperativeRegistrationRepository`
- [ ] Tạo `CooperativeRepository`

### Phase 3: DTOs
- [ ] Tạo `Step1Request` - `Step6Request`
- [ ] Tạo `RegistrationStepResponse`
- [ ] Tạo `RegistrationDetailResponse`
- [ ] Tạo `CooperativeResponse`
- [ ] Tạo `SubmitRegistrationRequest`
- [ ] Tạo `ReviewCooperativeRequest`
- [ ] Tạo `UpdateCooperativeRequest`

### Phase 4: Services
- [ ] Tạo `CooperativeRegistrationService`
- [ ] Implement tất cả methods trong service
- [ ] Tạo `CooperativeService`
- [ ] Implement tất cả methods trong service

### Phase 5: Controllers
- [ ] Tạo `CooperativeRegistrationController`
- [ ] Tạo `AdminCooperativeController`
- [ ] Tạo `CooperativeController`

### Phase 6: Database
- [ ] Tạo migration SQL file
- [ ] Test migration

### Phase 7: Security
- [ ] Cập nhật `SecurityConfig`

### Phase 8: Validation
- [ ] Tạo custom validators (nếu cần)
- [ ] Tạo custom exceptions
- [ ] Update `GlobalExceptionHandler`

### Phase 9: Integration
- [ ] Test Keycloak integration
- [ ] Test user role upgrade

### Phase 10: Testing
- [ ] Unit tests
- [ ] Integration tests
- [ ] Manual testing

---

## 🔗 Tham Khảo

### Files để tham khảo:
1. **`SellerRegistration.java`** - Entity structure
2. **`Store.java`** - Entity structure cho Cooperative
3. **`AuthService.java`** - Cách assign role trong Keycloak
4. **`KeycloakClient.java`** - Methods để assign role
5. **`SecurityConfig.java`** - Security rules

### Patterns:
- Repository pattern (JPA)
- Service layer pattern
- DTO pattern
- Exception handling pattern

---

## ⚠️ Lưu Ý Quan Trọng

1. **Transaction Management:**
   - Sử dụng `@Transactional` cho các operations quan trọng
   - Đặc biệt khi approve (tạo Cooperative + update User + Keycloak)

2. **Validation:**
   - Validate tất cả input ở Controller level (DTO validation)
   - Validate business logic ở Service level

3. **Error Handling:**
   - Sử dụng custom exceptions
   - Return error messages rõ ràng

4. **Security:**
   - Luôn validate user có quyền truy cập resource
   - Không cho phép user xem/sửa registration của user khác

5. **Keycloak Integration:**
   - Khi approve, phải assign role `COOPERATIVE_MANAGER` trong Keycloak
   - Update `User.userType` trong database

6. **Data Consistency:**
   - Đảm bảo slug, cooperativeCode unique
   - Đảm bảo user chỉ có 1 Cooperative

---

## 🚀 Thứ Tự Ưu Tiên Implementation

1. **Phase 1-2**: Enums, Entities, Repositories (Foundation)
2. **Phase 3**: DTOs (Data structure)
3. **Phase 6**: Database Migration (Setup database)
4. **Phase 4**: Services (Business logic)
5. **Phase 5**: Controllers (API endpoints)
6. **Phase 7-8**: Security & Validation
7. **Phase 9**: Keycloak Integration
8. **Phase 10**: Testing

---

## 📝 Notes

- Có thể implement từng bước một, test từng bước
- Có thể tạo TODO list để track progress
- Nên commit code sau mỗi phase hoàn thành
- Có thể refactor sau khi hoàn thành tất cả

