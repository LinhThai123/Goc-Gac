# Hướng Dẫn Email Verification với Keycloak

## 📋 Tổng Quan

Hệ thống sử dụng **Keycloak's built-in email verification** để xác thực email. Keycloak sẽ tự động:
- Gửi email verification với link
- Xử lý verification flow
- Quản lý email verified status

---

## 🔧 Cấu Hình Keycloak

### 1. Cấu Hình Email trong Keycloak

**Bước 1:** Vào Keycloak Admin Console
- URL: `http://localhost:8080`
- Đăng nhập: `admin/admin123`

**Bước 2:** Chọn Realm `gocgac-htx`
- Dropdown ở góc trên bên trái

**Bước 3:** Vào **Realm Settings** > **Email**
- **Host**: `smtp.gmail.com`
- **Port**: `587`
- **From**: `your-email@gmail.com`
- **From Display Name**: `GocGac`
- **Enable StartTLS**: `ON`
- **Enable Authentication**: `ON`
- **Username**: `your-email@gmail.com`
- **Password**: `your-app-password` (Gmail App Password)
- Click **Save**

**Bước 4:** Test Email Configuration
- Click **Test connection**
- Nếu thành công, sẽ thấy "Email sent successfully"

### 2. Cấu Hình Email Verification Flow

**Bước 1:** Vào **Realm Settings** > **Login**
- **User registration**: `ON` (nếu muốn cho phép đăng ký)
- **Email as username**: `ON` (optional)

**Bước 2:** Vào **Realm Settings** > **Email**
- **Verify Email**: `ON`
- **Require Email Verification**: `ON` (optional - nếu muốn bắt buộc verify)

**Bước 3:** Customize Email Template (Optional)
- Vào **Realm Settings** > **Email** > **Email Templates**
- Chọn **Verify Email**
- Có thể customize nội dung email

---

## 🔄 Luồng Hoạt Động

### 1. Đăng Ký và Gửi Email Verification

```
User đăng ký
    ↓
Tạo user trong Keycloak + Database
    ↓
Gửi email verification từ Keycloak
    ↓
Keycloak gửi email với verification link
    ↓
User nhận email
```

**API Endpoint:**
```http
POST /api/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123",
  "fullName": "Nguyễn Văn A",
  "phone": "0123456789",
  "userType": "CUSTOMER"
}
```

**Response:**
```json
{
  "message": "Đăng ký thành công. Vui lòng kiểm tra email để xác thực tài khoản.",
  "status": 201,
  "data": {
    "id": 1,
    "email": "user@example.com",
    "fullName": "Nguyễn Văn A",
    ...
  }
}
```

### 2. User Click Link trong Email

```
User click link trong email từ Keycloak
    ↓
Keycloak xử lý verification
    ↓
Redirect về frontend (nếu có redirect_uri)
    ↓
Email được verify trong Keycloak
```

**Keycloak Verification Link:**
```
http://localhost:8080/realms/gocgac-htx/login-actions/action-token?key=xxx&client_id=gocgac_app
```

### 3. Kiểm Tra Trạng Thái Email Verified

```
Frontend/Backend gọi API check status
    ↓
Backend lấy status từ Keycloak
    ↓
Đồng bộ với Database
    ↓
Trả về status
```

**API Endpoint:**
```http
GET /api/auth/email-verification/status?email=user@example.com
```

**Response:**
```json
{
  "message": "Email đã được xác thực",
  "status": 200,
  "data": {
    "emailVerified": true
  }
}
```

### 4. Gửi Lại Email Verification

```
User yêu cầu gửi lại email
    ↓
Backend gọi Keycloak API
    ↓
Keycloak gửi email mới
```

**API Endpoint:**
```http
POST /api/auth/email-verification/resend
Content-Type: application/json

{
  "email": "user@example.com"
}
```

---

## 📁 Cấu Trúc Code

### 1. KeycloakClient Methods

**Methods:**
- `sendVerificationEmail(String userId)` - Gửi email verification
- `sendVerificationEmail(String userId, String clientId, String redirectUri)` - Gửi với custom redirect
- `verifyEmail(String userId)` - Verify email manually
- `isEmailVerified(String userId)` - Check email verified status
- `getUserIdByEmail(String email)` - Lấy Keycloak user ID từ email

### 2. EmailVerificationService

**Methods:**
- `sendVerificationEmail(String email)` - Gửi email verification
- `resendVerificationEmail(String email)` - Gửi lại email
- `checkEmailVerifiedStatus(String email)` - Check và đồng bộ status
- `verifyEmailManually(String email)` - Verify thủ công (nếu cần)

### 3. EmailVerificationController

**Endpoints:**
- `POST /api/auth/email-verification/send` - Gửi email verification
- `POST /api/auth/email-verification/resend` - Gửi lại email
- `GET /api/auth/email-verification/status?email=xxx` - Check status
- `POST /api/auth/email-verification/verify` - Verify thủ công

---

## 🔐 Keycloak Email Verification Flow

### Flow 1: Automatic (Recommended)

**Cách hoạt động:**
1. User đăng ký → Keycloak tạo user với `emailVerified: false`
2. Backend gọi `sendVerificationEmail()` → Keycloak gửi email
3. User click link trong email → Keycloak tự động verify
4. Keycloak set `emailVerified: true`
5. Frontend/Backend check status để đồng bộ với Database

**Ưu điểm:**
- ✅ Keycloak tự quản lý
- ✅ Không cần maintain code
- ✅ Standard flow

### Flow 2: Manual Verification

**Cách hoạt động:**
1. User đăng ký → Keycloak tạo user với `emailVerified: false`
2. Backend gọi `sendVerificationEmail()` → Keycloak gửi email
3. Admin/System gọi `verifyEmailManually()` → Verify thủ công

**Khi nào dùng:**
- Testing
- Admin verify thủ công
- Integration với hệ thống khác

---

## 🧪 Testing

### Test 1: Đăng ký và nhận email

```bash
curl -X POST http://localhost:8086/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123",
    "fullName": "Test User",
    "phone": "0123456789",
    "userType": "CUSTOMER"
  }'
```

**Kiểm tra:**
- Email được gửi từ Keycloak
- Link verification trong email
- `emailVerified = false` trong Keycloak và Database

### Test 2: Gửi lại email

```bash
curl -X POST http://localhost:8086/api/auth/email-verification/resend \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com"
  }'
```

**Kiểm tra:**
- Email mới được gửi
- Link verification mới

### Test 3: Check status

```bash
curl -X GET "http://localhost:8086/api/auth/email-verification/status?email=test@example.com"
```

**Kiểm tra:**
- Status được đồng bộ từ Keycloak
- Database được cập nhật

### Test 4: Verify thủ công

```bash
curl -X POST http://localhost:8086/api/auth/email-verification/verify \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com"
  }'
```

**Kiểm tra:**
- `emailVerified = true` trong Keycloak
- `emailVerified = true` trong Database

---

## ⚠️ Lưu Ý Quan Trọng

### 1. Keycloak Email Configuration

- ✅ **Bắt buộc** cấu hình SMTP trong Keycloak
- ✅ **Bắt buộc** test email connection
- ✅ Gmail cần App Password (không dùng password thông thường)

### 2. Email Verification Link

- Link verification là của Keycloak (không phải custom)
- Format: `http://localhost:8080/realms/{realm}/login-actions/action-token?key=xxx`
- Có thể customize redirect URI khi gửi email

### 3. Đồng Bộ Database

- Keycloak là source of truth cho `emailVerified`
- Database được đồng bộ khi check status
- Nên check status định kỳ hoặc sau khi user login

### 4. Frontend Integration

**Option 1: Dùng Keycloak's Built-in Flow**
- User click link → Keycloak xử lý → Redirect về frontend
- Frontend check status sau khi redirect

**Option 2: Custom Flow**
- User click link → Keycloak verify → Frontend gọi API check status
- Frontend hiển thị thông báo

---

## 📊 Database Schema

**User Entity:**
```java
@Column(name = "email_verified", nullable = false)
private Boolean emailVerified = false;
```

**Lưu ý:**
- Field `email_verified` được đồng bộ với Keycloak
- Không tự set, chỉ sync từ Keycloak

---

## 🔄 Đồng Bộ Email Verified Status

### Tự Động Đồng Bộ

**Khi nào:**
- Khi check status (`/api/auth/email-verification/status`)
- Có thể thêm vào login flow

**Code:**
```java
// Trong login hoặc check status
boolean isVerified = keycloakClient.isEmailVerified(user.getKeycloakId());
if (isVerified != user.getEmailVerified()) {
    user.setEmailVerified(isVerified);
    userRepository.save(user);
}
```

### Manual Sync

**Khi nào:**
- Admin muốn sync tất cả users
- Scheduled task để sync định kỳ

---

## 🎯 Best Practices

### 1. Gửi Email Sau Đăng Ký

✅ **Recommended:**
```java
// Trong AuthService.register()
try {
    emailVerificationService.sendVerificationEmail(user.getEmail());
} catch (Exception e) {
    log.warn("Không thể gửi email verification: {}", e.getMessage());
    // Không throw exception vì user đã được tạo
}
```

### 2. Check Status Sau Login

✅ **Recommended:**
```java
// Trong AuthService.login()
// Có thể thêm check và sync email verified status
boolean isVerified = keycloakClient.isEmailVerified(user.getKeycloakId());
if (isVerified != user.getEmailVerified()) {
    user.setEmailVerified(isVerified);
    userRepository.save(user);
}
```

### 3. Frontend Flow

✅ **Recommended:**
1. User đăng ký → Nhận message "Vui lòng kiểm tra email"
2. User click link trong email → Keycloak verify
3. Keycloak redirect về frontend
4. Frontend gọi API check status
5. Frontend hiển thị "Email đã được xác thực"

---

## 📝 Tóm Tắt

1. **Đăng ký**: Tự động gửi email verification từ Keycloak
2. **Verification**: Keycloak tự xử lý khi user click link
3. **Status Check**: API để check và đồng bộ status
4. **Resend**: API để gửi lại email
5. **Sync**: Đồng bộ `emailVerified` giữa Keycloak và Database

Hệ thống sử dụng **Keycloak's built-in email verification** - đơn giản, reliable, và không cần maintain code! 🎉

