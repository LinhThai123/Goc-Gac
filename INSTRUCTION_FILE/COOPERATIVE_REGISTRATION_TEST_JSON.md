# JSON Test Data cho Đăng Ký Hợp Tác Xã (HTX)

File này chứa các JSON mẫu để test chức năng đăng ký HTX trên Swagger hoặc Postman.

**Lưu ý:** Đăng ký HTX đã được đơn giản hóa thành **1 request duy nhất** (giống SellerRegistration), không còn multi-step nữa.

---

## 📋 Mục Lục

1. [Đăng ký HTX (Single Request)](#đăng-ký-htx-single-request)
2. [Lấy đơn đăng ký của tôi](#lấy-đơn-đăng-ký-của-tôi)
3. [Lấy chi tiết đơn đăng ký](#lấy-chi-tiết-đơn-đăng-ký)
4. [Admin: List đơn đăng ký](#admin-list-đơn-đăng-ký)
5. [Admin: Chi tiết đơn đăng ký](#admin-chi-tiết-đơn-đăng-ký)
6. [Admin: Approve đơn đăng ký](#admin-approve-đơn-đăng-ký)
7. [Admin: Reject đơn đăng ký](#admin-reject-đơn-đăng-ký)
8. [JSON Tối Giản (Minimal)](#json-tối-giản-minimal)

---

## 🔹 Đăng ký HTX (Single Request)

**Endpoint:** `POST /api/cooperative/registration`

**Request Body (Đầy đủ):**

```json
{
  "cooperativeName": "Hợp Tác Xã Nông Nghiệp Xanh Việt Nam",
  "slug": "htx-nong-nghiep-xanh-viet-nam",
  "cooperativeCode": "HTX-NNX-VN-2024",
  "establishmentDate": "2020-05-15",
  "cooperativeType": "AGRICULTURAL",
  "scale": "MEDIUM",
  "shortDescription": "Hợp tác xã chuyên sản xuất và cung cấp các sản phẩm nông nghiệp sạch, an toàn cho người tiêu dùng. Chúng tôi cam kết mang đến những sản phẩm chất lượng cao với giá cả hợp lý, góp phần phát triển nền nông nghiệp bền vững tại Việt Nam.",
  "contactEmail": "contact@htx-nnx-vn.com",
  "contactPhone": "0912345678",
  "contactPhoneAlt": "0987654321",
  "website": "https://www.htx-nnx-vn.com",
  "facebookPage": "https://www.facebook.com/htx-nnx-vn",
  "fullAddress": "123 Đường Nguyễn Văn Linh, Phường An Khánh",
  "province": "Thành phố Hồ Chí Minh",
  "district": "Quận 2",
  "ward": "Phường An Khánh",
  "postalCode": "700000",
  "showMap": true,
  "latitude": 10.7769,
  "longitude": 106.7009,
  "representativeName": "Nguyễn Văn A",
  "representativePosition": "Chủ nhiệm Hợp tác xã",
  "representativeIdNumber": "001234567890",
  "representativeIdIssueDate": "2015-03-20",
  "representativeIdIssuePlace": "Công an Thành phố Hồ Chí Minh",
  "representativeEmail": "nguyenvana@htx-nnx-vn.com",
  "representativePhone": "0901234567",
  "representativeIdFrontImage": "https://example.com/images/cccd-front.jpg",
  "representativeIdBackImage": "https://example.com/images/cccd-back.jpg",
  "taxCode": "0312345678",
  "registrationCertificateNumber": "CN-2020-123456",
  "registrationCertificateIssueDate": "2020-06-10",
  "registrationCertificateIssuePlace": "Sở Kế hoạch và Đầu tư Thành phố Hồ Chí Minh",
  "registrationCertificateImage": "https://example.com/images/registration-cert.jpg",
  "taxCodeCertificateImage": "https://example.com/images/tax-cert.jpg",
  "productTypes": [
    "Rau củ quả tươi",
    "Gạo và ngũ cốc",
    "Thực phẩm chế biến",
    "Cà phê và trà"
  ],
  "mainProductDescription": "Hợp tác xã chuyên sản xuất và cung cấp các sản phẩm nông nghiệp sạch, đảm bảo chất lượng và an toàn thực phẩm. Sản phẩm chính của chúng tôi bao gồm: Rau củ quả tươi được trồng theo phương pháp hữu cơ, không sử dụng thuốc trừ sâu và phân bón hóa học; Gạo và ngũ cốc chất lượng cao, được chọn lọc kỹ càng từ các vùng nguyên liệu tốt nhất; Thực phẩm chế biến từ nguồn nguyên liệu sạch, đảm bảo vệ sinh an toàn thực phẩm; Cà phê và trà được sản xuất theo quy trình khép kín, giữ nguyên hương vị đặc trưng. Tất cả sản phẩm đều được kiểm tra chất lượng nghiêm ngặt trước khi đưa ra thị trường.",
  "businessScale": "MEDIUM",
  "hasSpecialCertification": true,
  "specialCertificationDetails": "Chứng nhận VietGAP cho sản phẩm rau củ quả, Chứng nhận GlobalGAP cho sản phẩm cà phê, Giấy phép vệ sinh an toàn thực phẩm từ Bộ Y tế"
}
```

**Response mẫu (thành công):**
```json
{
  "status": 200,
  "message": "Đơn đăng ký HTX đã được gửi thành công. Vui lòng chờ admin phê duyệt.",
  "data": {
    "id": 1,
    "userId": 5,
    "cooperativeName": "Hợp Tác Xã Nông Nghiệp Xanh Việt Nam",
    "slug": "htx-nong-nghiep-xanh-viet-nam",
    "cooperativeCode": "HTX-NNX-VN-2024",
    "status": "PENDING",
    "currentStep": 6,
    "submittedAt": "2025-12-24T22:51:42.589918",
    // ... các trường khác
  }
}
```

**Response mẫu (lỗi - slug đã tồn tại):**
```json
{
  "status": 400,
  "message": "Slug đã tồn tại",
  "timestamp": "2025-12-24T22:51:55.4249292",
  "errors": null
}
```

**Giải thích các trường:**

### Thông tin HTX cơ bản:
- `cooperativeName`: Tên HTX (bắt buộc, 5-200 ký tự)
- `slug`: Slug URL (bắt buộc, chỉ chữ thường, số, dấu gạch ngang, 3-100 ký tự, unique)
- `cooperativeCode`: Mã số HTX (bắt buộc, chỉ chữ hoa, số, dấu gạch ngang, 5-50 ký tự, unique)
- `establishmentDate`: Ngày thành lập (bắt buộc, format: YYYY-MM-DD, không được là tương lai)
- `cooperativeType`: Loại hình HTX (bắt buộc, các giá trị: `AGRICULTURAL`, `INDUSTRIAL`, `SERVICE`, `CONSUMER`, `CREDIT`, `HOUSING`, `OTHER`)
- `scale`: Quy mô HTX (bắt buộc, các giá trị: `SMALL`, `MEDIUM`, `LARGE`)
- `shortDescription`: Mô tả ngắn (bắt buộc, 50-500 ký tự)

### Thông tin liên hệ:
- `contactEmail`: Email liên hệ (bắt buộc, phải là email hợp lệ)
- `contactPhone`: Số điện thoại (bắt buộc, 10-11 chữ số)
- `contactPhoneAlt`: Số điện thoại phụ (tùy chọn, 10-11 chữ số)
- `website`: Website (tùy chọn, phải là URL hợp lệ)
- `facebookPage`: Facebook Page (tùy chọn, có thể là URL hoặc username)

### Địa chỉ kinh doanh:
- `fullAddress`: Địa chỉ đầy đủ (bắt buộc, tối đa 500 ký tự)
- `province`: Tỉnh/Thành phố (bắt buộc, tối đa 100 ký tự)
- `district`: Quận/Huyện (bắt buộc, tối đa 100 ký tự)
- `ward`: Phường/Xã (bắt buộc, tối đa 100 ký tự)
- `postalCode`: Mã bưu chính (tùy chọn, tối đa 20 ký tự)
- `showMap`: Hiển thị bản đồ (bắt buộc, true/false)
- `latitude`: Vĩ độ (tùy chọn, -90.0 đến 90.0, chỉ cần khi showMap = true)
- `longitude`: Kinh độ (tùy chọn, -180.0 đến 180.0, chỉ cần khi showMap = true)

### Thông tin người đại diện:
- `representativeName`: Họ và tên người đại diện (bắt buộc, tối đa 200 ký tự)
- `representativePosition`: Chức vụ (bắt buộc, tối đa 100 ký tự)
- `representativeIdNumber`: Số CCCD/CMND (bắt buộc, tối đa 50 ký tự)
- `representativeIdIssueDate`: Ngày cấp CCCD (bắt buộc, format: YYYY-MM-DD, không được là tương lai)
- `representativeIdIssuePlace`: Nơi cấp CCCD (bắt buộc, tối đa 200 ký tự)
- `representativeEmail`: Email người đại diện (bắt buộc, phải là email hợp lệ)
- `representativePhone`: Số điện thoại người đại diện (bắt buộc, 10-11 chữ số)
- `representativeIdFrontImage`: URL ảnh mặt trước CCCD (tùy chọn, phải là URL hợp lệ)
- `representativeIdBackImage`: URL ảnh mặt sau CCCD (tùy chọn, phải là URL hợp lệ)

### Thông tin pháp lý:
- `taxCode`: Mã số thuế (tùy chọn, tối đa 50 ký tự)
- `registrationCertificateNumber`: Số Giấy CNĐK HTX (bắt buộc, tối đa 100 ký tự)
- `registrationCertificateIssueDate`: Ngày cấp Giấy CNĐK HTX (bắt buộc, format: YYYY-MM-DD, không được là tương lai)
- `registrationCertificateIssuePlace`: Nơi cấp Giấy CNĐK HTX (bắt buộc, tối đa 200 ký tự)
- `registrationCertificateImage`: URL ảnh Giấy CNĐK HTX (tùy chọn, phải là URL hợp lệ)
- `taxCodeCertificateImage`: URL ảnh Giấy chứng nhận mã số thuế (tùy chọn, phải là URL hợp lệ)

### Thông tin kinh doanh:
- `productTypes`: Danh sách loại sản phẩm kinh doanh (bắt buộc, ít nhất 1 loại, tối đa 20 loại)
- `mainProductDescription`: Mô tả sản phẩm chính (bắt buộc, 50-2000 ký tự)
- `businessScale`: Quy mô sản xuất/kinh doanh (bắt buộc, các giá trị: `SMALL`, `MEDIUM`, `LARGE`)
- `hasSpecialCertification`: Có chứng nhận/giấy phép đặc biệt không (bắt buộc, true/false)
- `specialCertificationDetails`: Chi tiết chứng nhận (tùy chọn, tối đa 1000 ký tự, nên điền nếu hasSpecialCertification = true)

---

## 🔹 Lấy đơn đăng ký của tôi

**Endpoint:** `GET /api/cooperative/registration/my-registration`

**Response mẫu:**
```json
{
  "id": 1,
  "userId": 5,
  "cooperativeName": "Hợp Tác Xã Nông Nghiệp Xanh Việt Nam",
  "slug": "htx-nong-nghiep-xanh-viet-nam",
  "status": "PENDING",
  "currentStep": 6,
  "submittedAt": "2025-12-24T22:51:42.589918",
  // ... các trường khác
}
```

**Lưu ý:** Endpoint này chỉ trả về đơn đăng ký ở trạng thái `PENDING`. Nếu không có, trả về 404.

---

## 🔹 Lấy chi tiết đơn đăng ký

**Endpoint:** `GET /api/cooperative/registration/{registrationId}`

**Ví dụ:** `GET /api/cooperative/registration/1`

**Response mẫu:**
```json
{
  "id": 1,
  "userId": 5,
  "cooperativeName": "Hợp Tác Xã Nông Nghiệp Xanh Việt Nam",
  // ... tất cả thông tin chi tiết
}
```

---

## 🔹 Admin: List đơn đăng ký

**Endpoint:** `GET /api/admin/cooperative/registrations?page=0&size=10&status=PENDING`

**Query Parameters:**
- `page`: Số trang (mặc định: 0)
- `size`: Số lượng mỗi trang (mặc định: 10)
- `status`: Lọc theo trạng thái (tùy chọn: `PENDING`, `APPROVED`, `REJECTED`)
- `sortBy`: Trường sắp xếp (mặc định: `submittedAt`)
- `sortDir`: Hướng sắp xếp (mặc định: `DESC`, có thể là `ASC`)

**Response mẫu:**
```json
{
  "content": [
    {
      "id": 1,
      "cooperativeName": "Hợp Tác Xã Nông Nghiệp Xanh Việt Nam",
      "status": "PENDING",
      "submittedAt": "2025-12-24T22:51:42.589918"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 10,
  "number": 0
}
```

---

## 🔹 Admin: Chi tiết đơn đăng ký

**Endpoint:** `GET /api/admin/cooperative/registrations/{registrationId}`

**Ví dụ:** `GET /api/admin/cooperative/registrations/1`

**Response mẫu:**
```json
{
  "id": 1,
  "userId": 5,
  "cooperativeName": "Hợp Tác Xã Nông Nghiệp Xanh Việt Nam",
  // ... tất cả thông tin chi tiết
}
```

---

## 🔹 Admin: Approve đơn đăng ký

**Endpoint:** `POST /api/admin/cooperative/registrations/{registrationId}/approve`

**Request Body:**

```json
{
  "status": "APPROVED"
}
```

**Giải thích các trường:**
- `status`: Trạng thái (bắt buộc, phải là `APPROVED`)

**Ví dụ:** `POST /api/admin/cooperative/registrations/1/approve`

**Response mẫu:**
```json
{
  "status": 200,
  "message": "Đơn đăng ký đã được phê duyệt thành công",
  "data": {
    "registrationId": 1,
    "status": "APPROVED",
    "cooperativeId": 1
  }
}
```

**Lưu ý:** 
- Endpoint này yêu cầu quyền `SUPER_ADMIN` hoặc `ADMIN`
- Sau khi approve, user sẽ được gán role `COOPERATIVE_MANAGER` trong Keycloak
- UserType của user sẽ được cập nhật thành `COOPERATIVE_MANAGER`
- Cooperative entity sẽ được tạo từ Registration

---

## 🔹 Admin: Reject đơn đăng ký

**Endpoint:** `POST /api/admin/cooperative/registrations/{registrationId}/reject`

**Request Body:**

```json
{
  "status": "REJECTED",
  "rejectionReason": "Thông tin pháp lý không đầy đủ. Vui lòng bổ sung Giấy CNĐK HTX và các tài liệu liên quan."
}
```

**Giải thích các trường:**
- `status`: Trạng thái (bắt buộc, phải là `REJECTED`)
- `rejectionReason`: Lý do từ chối (bắt buộc, không được để trống)

**Ví dụ:** `POST /api/admin/cooperative/registrations/1/reject`

**Response mẫu:**
```json
{
  "status": 200,
  "message": "Đơn đăng ký đã bị từ chối",
  "data": {
    "registrationId": 1,
    "status": "REJECTED",
    "rejectionReason": "Thông tin pháp lý không đầy đủ. Vui lòng bổ sung Giấy CNĐK HTX và các tài liệu liên quan."
  }
}
```

**Lưu ý:** 
- Endpoint này yêu cầu quyền `SUPER_ADMIN` hoặc `ADMIN`
- Sau khi reject, user có thể tạo đơn đăng ký mới

---

## 🔹 JSON Tối Giản (Minimal)

Nếu bạn muốn test nhanh với dữ liệu tối thiểu:

```json
{
  "cooperativeName": "HTX Test ABC",
  "slug": "htx-test-abc",
  "cooperativeCode": "HTX-TEST-001",
  "establishmentDate": "2020-01-01",
  "cooperativeType": "AGRICULTURAL",
  "scale": "SMALL",
  "shortDescription": "Đây là mô tả ngắn về hợp tác xã test với đủ 50 ký tự để đáp ứng yêu cầu validation của hệ thống.",
  "contactEmail": "test@example.com",
  "contactPhone": "0912345678",
  "fullAddress": "123 Đường Test",
  "province": "Hà Nội",
  "district": "Quận Ba Đình",
  "ward": "Phường Điện Biên",
  "showMap": false,
  "representativeName": "Nguyễn Văn Test",
  "representativePosition": "Chủ nhiệm",
  "representativeIdNumber": "001234567890",
  "representativeIdIssueDate": "2020-01-01",
  "representativeIdIssuePlace": "Công an Hà Nội",
  "representativeEmail": "test@example.com",
  "representativePhone": "0912345678",
  "registrationCertificateNumber": "CN-2020-001",
  "registrationCertificateIssueDate": "2020-01-01",
  "registrationCertificateIssuePlace": "Sở Kế hoạch và Đầu tư Hà Nội",
  "productTypes": ["Sản phẩm test"],
  "mainProductDescription": "Đây là mô tả sản phẩm chính với đủ 50 ký tự để đáp ứng yêu cầu validation của hệ thống. Mô tả này cung cấp thông tin chi tiết về sản phẩm.",
  "businessScale": "SMALL",
  "hasSpecialCertification": false
}
```

---

## 🎯 Quy Trình Test Đầy Đủ

### 1. Test Flow Đăng Ký (User)

1. **Đăng ký HTX:** 
   - Gọi `POST /api/cooperative/registration` với JSON đầy đủ
   - Status sẽ là `PENDING` ngay từ đầu (không cần submit)
   - Lưu lại `registrationId` từ response

2. **Kiểm tra đơn đăng ký:**
   - Gọi `GET /api/cooperative/registration/my-registration` để xem đơn của tôi
   - Gọi `GET /api/cooperative/registration/{registrationId}` để xem chi tiết

### 2. Test Flow Admin Review

1. **List:** 
   - Gọi `GET /api/admin/cooperative/registrations?status=PENDING` để xem danh sách

2. **Chi tiết:** 
   - Gọi `GET /api/admin/cooperative/registrations/{registrationId}` để xem chi tiết

3. **Approve hoặc Reject:**
   - Approve: `POST /api/admin/cooperative/registrations/{registrationId}/approve` với body `{"status": "APPROVED"}`
   - Reject: `POST /api/admin/cooperative/registrations/{registrationId}/reject` với body `{"status": "REJECTED", "rejectionReason": "..."}`

### 3. Test Flow Sau Khi Approve

1. **User đăng nhập lại** (để nhận role mới từ Keycloak)
2. **Xem HTX của tôi:** `GET /api/cooperative/my-cooperative` (nếu có endpoint này)
3. **Xem HTX công khai:** `GET /api/cooperative/{slug}`

---

## ⚠️ Lưu Ý Khi Test

1. **Authentication:** Tất cả endpoints (trừ public) đều yêu cầu JWT token trong header:
   ```
   Authorization: Bearer <your-jwt-token>
   ```

2. **Validation:** Các trường có validation, nếu không đúng format sẽ trả về lỗi 400

3. **Status Flow:**
   - `PENDING` → Sau khi đăng ký (chờ admin duyệt)
   - `APPROVED` → Sau khi admin approve
   - `REJECTED` → Sau khi admin reject

4. **Enum Values:**
   - `cooperativeType`: `AGRICULTURAL`, `INDUSTRIAL`, `SERVICE`, `CONSUMER`, `CREDIT`, `HOUSING`, `OTHER`
   - `scale` (CooperativeScale): `SMALL`, `MEDIUM`, `LARGE`
   - `businessScale` (BusinessScale): `SMALL`, `MEDIUM`, `LARGE`

5. **Date Format:** Tất cả các trường date đều dùng format `YYYY-MM-DD` (ví dụ: `2024-12-15`)

6. **URL Validation:** Các trường URL (website, facebookPage, image URLs) phải là URL hợp lệ (bắt đầu với `http://` hoặc `https://`)

7. **Unique Fields:** 
   - `slug` phải unique
   - `cooperativeCode` phải unique
   - `taxCode` phải unique (nếu có)

8. **Trường tùy chọn:** Các trường không bắt buộc có thể bỏ qua hoặc để `null`

---

## 📌 Ví Dụ JSON Không Hiển Thị Bản Đồ

```json
{
  "cooperativeName": "HTX Test ABC",
  "slug": "htx-test-abc-2",
  "cooperativeCode": "HTX-TEST-002",
  "establishmentDate": "2020-01-01",
  "cooperativeType": "AGRICULTURAL",
  "scale": "SMALL",
  "shortDescription": "Đây là mô tả ngắn về hợp tác xã test với đủ 50 ký tự để đáp ứng yêu cầu validation của hệ thống.",
  "contactEmail": "test@example.com",
  "contactPhone": "0912345678",
  "fullAddress": "456 Đường Lê Lợi, Phường Bến Nghé",
  "province": "Thành phố Hồ Chí Minh",
  "district": "Quận 1",
  "ward": "Phường Bến Nghé",
  "showMap": false,
  "representativeName": "Nguyễn Văn Test",
  "representativePosition": "Chủ nhiệm",
  "representativeIdNumber": "001234567890",
  "representativeIdIssueDate": "2020-01-01",
  "representativeIdIssuePlace": "Công an Hà Nội",
  "representativeEmail": "test@example.com",
  "representativePhone": "0912345678",
  "registrationCertificateNumber": "CN-2020-001",
  "registrationCertificateIssueDate": "2020-01-01",
  "registrationCertificateIssuePlace": "Sở Kế hoạch và Đầu tư Hà Nội",
  "productTypes": ["Sản phẩm test"],
  "mainProductDescription": "Đây là mô tả sản phẩm chính với đủ 50 ký tự để đáp ứng yêu cầu validation của hệ thống. Mô tả này cung cấp thông tin chi tiết về sản phẩm.",
  "businessScale": "SMALL",
  "hasSpecialCertification": false
}
```

---

## 📌 Ví Dụ JSON Không Có Chứng Nhận Đặc Biệt

```json
{
  "cooperativeName": "HTX Test ABC",
  "slug": "htx-test-abc-3",
  "cooperativeCode": "HTX-TEST-003",
  "establishmentDate": "2020-01-01",
  "cooperativeType": "SERVICE",
  "scale": "SMALL",
  "shortDescription": "Đây là mô tả ngắn về hợp tác xã test với đủ 50 ký tự để đáp ứng yêu cầu validation của hệ thống.",
  "contactEmail": "test@example.com",
  "contactPhone": "0912345678",
  "fullAddress": "123 Đường Test",
  "province": "Hà Nội",
  "district": "Quận Ba Đình",
  "ward": "Phường Điện Biên",
  "showMap": false,
  "representativeName": "Nguyễn Văn Test",
  "representativePosition": "Chủ nhiệm",
  "representativeIdNumber": "001234567890",
  "representativeIdIssueDate": "2020-01-01",
  "representativeIdIssuePlace": "Công an Hà Nội",
  "representativeEmail": "test@example.com",
  "representativePhone": "0912345678",
  "registrationCertificateNumber": "CN-2020-001",
  "registrationCertificateIssueDate": "2020-01-01",
  "registrationCertificateIssuePlace": "Sở Kế hoạch và Đầu tư Hà Nội",
  "productTypes": ["Sản phẩm thủ công mỹ nghệ"],
  "mainProductDescription": "Hợp tác xã chuyên sản xuất các sản phẩm thủ công mỹ nghệ truyền thống, được làm thủ công bởi các nghệ nhân lành nghề. Sản phẩm của chúng tôi bao gồm đồ gốm, đồ mây tre đan, và các sản phẩm dệt may thủ công. Mỗi sản phẩm đều mang đậm nét văn hóa dân tộc và được chế tác tỉ mỉ, cẩn thận.",
  "businessScale": "SMALL",
  "hasSpecialCertification": false
}
```

---

**Chúc bạn test thành công! 🚀**
