# Quy Trình Đăng Ký và Đăng Nhập với Keycloak

## 📋 Tổng Quan

Hệ thống sử dụng **Keycloak** làm Identity Provider (IdP) để quản lý xác thực và ủy quyền. Keycloak cung cấp:
- Quản lý user và credentials
- Phát hành JWT tokens (access token, refresh token)
- Quản lý roles và permissions

Hệ thống cũng lưu thông tin user trong **PostgreSQL database** để:
- Lưu thông tin bổ sung (phone, address, loyalty points, etc.)
- Đồng bộ với Keycloak thông qua `keycloakId`
- Quản lý business logic và relationships

---

## 🔐 QUY TRÌNH ĐĂNG KÝ (Register)

### Luồng xử lý:

```
Client → AuthController → AuthService → KeycloakClient → Keycloak Server
                                    ↓
                              PostgreSQL Database
```

### Các bước chi tiết:

#### **Bước 1: Client gửi request đăng ký**
```http
POST /api/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123",
  "fullName": "Nguyễn Văn A",
  "phone": "0123456789",
  "address": "123 Đường ABC",
  "userType": "CUSTOMER"
}
```

#### **Bước 2: AuthController nhận request**
- File: `AuthController.java`
- Validate request với `@Valid`
- Gọi `authService.register(request)`

#### **Bước 3: AuthService xử lý đăng ký**

**3.1. Kiểm tra email đã tồn tại:**
```java
if (userRepository.findByEmail(request.getEmail()).isPresent()) {
    throw new AuthException("Email đã được sử dụng");
}
```

**3.2. Đăng ký user trong Keycloak:**
```java
Map<String, Object> keycloakResult = keycloakClient.registerUser(
    request.getEmail(),
    request.getPassword(),
    request.getFullName()
);
```

#### **Bước 4: KeycloakClient.registerUser() - Tạo user trong Keycloak**

**4.1. Lấy Admin Token:**
```java
String adminToken = getAdminToken();
```
- **Ưu tiên**: Gọi `getMasterAdminToken()` để lấy token từ master realm
  - Endpoint: `POST /realms/master/protocol/openid-connect/token`
  - Grant type: `password`
  - Client: `admin-cli` (public client)
  - Username/Password: `admin/admin123` (development, configurable)
- **Fallback**: Nếu master admin token thất bại, gọi `getServiceAccountToken()`
  - Endpoint: `POST /realms/{realm}/protocol/openid-connect/token`
  - Grant type: `client_credentials`
  - Client ID: `gocgac_app`
  - Client Secret: (từ config)
  - **Lưu ý**: Service account cần có roles: `manage-users`, `view-users`, `query-users` từ `realm-management` client

**4.2. Tạo user trong Keycloak:**
```http
POST {keycloakServerUrl}/admin/realms/{realm}/users
Authorization: Bearer {adminToken}
Content-Type: application/json

{
  "username": "user@example.com",
  "email": "user@example.com",
  "firstName": "Nguyễn Văn A",
  "enabled": true,
  "emailVerified": false,
  "credentials": [{
    "type": "password",
    "value": "password123",
    "temporary": false
  }]
}
```

**4.3. Lấy User ID:**
- Keycloak trả về `201 Created` với `Location` header
- Format: `Location: .../users/{userId}`
- Extract userId từ Location header: `location.substring(location.lastIndexOf("/users/") + 7)`

**4.4. Set Password riêng (để đảm bảo password được set đúng):**
```java
setUserPassword(userId, password, adminToken);
```
- Endpoint: `PUT {keycloakServerUrl}/admin/realms/{realm}/users/{userId}/reset-password`
- Đảm bảo password được set đúng cách trong Keycloak

**4.5. Trả về kết quả:**
```java
{
  "userId": "abc-123-def-456"  // Keycloak user ID
}
```

#### **Bước 5: Tạo user trong PostgreSQL Database**

```java
User user = new User();
user.setEmail(request.getEmail());
user.setPasswordHash(passwordEncoder.encode(request.getPassword()));  // Hash password
user.setFullName(request.getFullName());
user.setPhone(request.getPhone());
user.setAddress(request.getAddress());
user.setUserType(request.getUserType());
user.setStatus(UserStatus.ACTIVE);
user.setKeycloakId(keycloakUserId);  // Liên kết với Keycloak
user.setEmailVerified(false);
user.setPhoneVerified(false);
user.setLoyaltyPoints(0);
```

#### **Bước 6: Gán Role mặc định**

**6.1. Tìm role trong database:**
```java
Role defaultRole = roleRepository.findByCode(request.getUserType().name())
    .orElseGet(() -> roleRepository.findByCode("CUSTOMER")
        .orElseThrow(() -> new AuthException("Role CUSTOMER không tồn tại trong hệ thống")));
```

**6.2. Gán role cho user trong database:**
```java
user.getRoles().add(defaultRole);
```

**6.3. Gán role trong Keycloak (BẮT BUỘC):**
```java
try {
    keycloakClient.assignRoleToUser(keycloakUserId, defaultRole.getCode());
    log.info("Đã gán role {} cho user {} trong Keycloak", defaultRole.getCode(), keycloakUserId);
} catch (Exception e) {
    log.error("Không thể gán role trong Keycloak: {}", e.getMessage(), e);
    // ROLLBACK: Xóa user trong Keycloak nếu không thể gán role
    try {
        keycloakClient.deleteUser(keycloakUserId);
        log.info("Đã xóa user {} trong Keycloak do không thể gán role", keycloakUserId);
    } catch (Exception deleteException) {
        log.error("Không thể xóa user trong Keycloak: {}", deleteException.getMessage());
    }
    throw new AuthException("Không thể gán role cho user. Vui lòng thử lại hoặc liên hệ quản trị viên.");
}
```

**Chi tiết `keycloakClient.assignRoleToUser()`:**

1. **Ưu tiên gán Client Role** (trong client `gocgac_app`):
   - Endpoint: `GET /admin/realms/{realm}/clients/{clientId}/roles/{roleName}`
   - Nếu client role tồn tại → Gán cho user
   - Endpoint gán: `POST /admin/realms/{realm}/users/{userId}/role-mappings/clients/{clientId}`

2. **Fallback: Gán Realm Role**:
   - Kiểm tra realm role có tồn tại không
   - Nếu chưa tồn tại → Tự động tạo realm role mới
   - Endpoint tạo: `POST /admin/realms/{realm}/roles`
   - Endpoint gán: `POST /admin/realms/{realm}/users/{userId}/role-mappings/realm`

3. **Nếu cả hai đều thất bại** → Throw exception → Rollback (xóa user trong Keycloak)

#### **Bước 7: Lưu user vào database**
```java
user = userRepository.save(user);
```

#### **Bước 8: Tạo UserDTO và MessageResponse**

```java
// Lấy roles của user
List<String> roles = user.getRoles().stream()
    .map(Role::getCode)
    .collect(Collectors.toList());

// Tạo UserDTO
UserDTO userDTO = new UserDTO();
userDTO.setId(user.getId());
userDTO.setEmail(user.getEmail());
userDTO.setFullName(user.getFullName());
userDTO.setPhone(user.getPhone());
userDTO.setAvatarUrl(user.getAvatarUrl());
userDTO.setRoles(roles);
userDTO.setUserType(user.getUserType().name());

// Tạo MessageResponse
MessageResponse response = new MessageResponse();
response.setMessage("Đăng ký thành công");
response.setStatus(HttpStatus.CREATED.value()); // 201
response.setData(userDTO);

return response;
```

**Lưu ý quan trọng:**
- **KHÔNG tự động đăng nhập** sau khi đăng ký
- User phải tự gọi `/api/auth/login` để lấy token
- Điều này tránh lỗi "Account is not fully set up" từ Keycloak

#### **Bước 9: Trả về cho Client**

```json
HTTP 201 Created

{
  "message": "Đăng ký thành công",
  "status": 201,
  "data": {
    "id": 1,
    "email": "user@example.com",
    "fullName": "Nguyễn Văn A",
    "phone": "0123456789",
    "avatarUrl": null,
    "roles": ["CUSTOMER"],
    "userType": "CUSTOMER"
  }
}
```

---

## 🔑 QUY TRÌNH ĐĂNG NHẬP (Login)

### Luồng xử lý:

```
Client → AuthController → AuthService → KeycloakClient → Keycloak Server
                                    ↓
                              PostgreSQL Database
```

### Các bước chi tiết:

#### **Bước 1: Client gửi request đăng nhập**
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

#### **Bước 2: AuthController nhận request**
- File: `AuthController.java`
- Validate request với `@Valid`
- Gọi `authService.login(request)`

#### **Bước 3: AuthService xử lý đăng nhập**

**3.1. Xác thực với Keycloak:**
```java
Map<String, Object> keycloakResponse = keycloakClient.login(
    request.getEmail(), request.getPassword());
```

**Chi tiết `keycloakClient.login()`:**
```http
POST {keycloakServerUrl}/realms/{realm}/protocol/openid-connect/token
Content-Type: application/x-www-form-urlencoded

grant_type=password
&client_id={clientId}
&client_secret={clientSecret}
&username=user@example.com
&password=password123
```

**Keycloak xác thực:**
- Kiểm tra user tồn tại
- Kiểm tra password đúng
- Kiểm tra user enabled
- Trả về tokens nếu thành công
- Trả về `401 Unauthorized` nếu sai

**Keycloak trả về:**
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI...",
  "refresh_token": "eyJhbGciOiJSUzI1NiIsInR5cCI...",
  "expires_in": 3600,
  "refresh_expires_in": 1800,
  "token_type": "Bearer"
}
```

**3.2. Lấy user từ database:**
```java
User user = userRepository.findByEmail(request.getEmail())
    .orElseThrow(() -> new AuthException("Người dùng không tồn tại trong hệ thống"));
```

**3.3. Kiểm tra user status:**
```java
if (user.getStatus() != UserStatus.ACTIVE) {
    throw new AuthException("Tài khoản của bạn đã bị khóa");
}
```

**3.4. Cập nhật thời gian đăng nhập cuối:**
```java
user.setLastLoginAt(LocalDateTime.now());
userRepository.save(user);
```

**3.5. Lấy roles của user:**
```java
List<String> roles = user.getRoles().stream()
    .map(Role::getCode)
    .collect(Collectors.toList());
```

**3.6. Tạo AuthResponse:**
```java
AuthResponse response = new AuthResponse();
response.setAccessToken(keycloakResponse.get("access_token"));
response.setRefreshToken(keycloakResponse.get("refresh_token"));
response.setExpiresIn(keycloakResponse.get("expires_in"));

AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo();
userInfo.setId(user.getId());
userInfo.setEmail(user.getEmail());
userInfo.setFullName(user.getFullName());
userInfo.setPhone(user.getPhone());
userInfo.setAvatarUrl(user.getAvatarUrl());
userInfo.setRoles(roles);
userInfo.setUserType(user.getUserType().name());

response.setUser(userInfo);
return response;
```

#### **Bước 4: Trả về cho Client**

```json
HTTP 200 OK

{
  "accessToken": "eyJhbGciOiJSUzI1NiIsInR5cCI...",
  "refreshToken": "eyJhbGciOiJSUzI1NiIsInR5cCI...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "user": {
    "id": 1,
    "email": "user@example.com",
    "fullName": "Nguyễn Văn A",
    "phone": "0123456789",
    "avatarUrl": null,
    "roles": ["CUSTOMER"],
    "userType": "CUSTOMER"
  }
}
```

---

## 🔄 QUY TRÌNH REFRESH TOKEN

### Mục đích:
- Access token có thời hạn ngắn (thường 1 giờ)
- Refresh token có thời hạn dài hơn (thường 30 phút)
- Dùng refresh token để lấy access token mới mà không cần đăng nhập lại

### Các bước:

#### **Bước 1: Client gửi refresh token**
```http
POST /api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJSUzI1NiIsInR5cCI..."
}
```

#### **Bước 2: AuthService gọi Keycloak**
```java
Map<String, Object> keycloakResponse = keycloakClient.refreshToken(refreshToken);
```

**Chi tiết `keycloakClient.refreshToken()`:**
```http
POST {keycloakServerUrl}/realms/{realm}/protocol/openid-connect/token
Content-Type: application/x-www-form-urlencoded

grant_type=refresh_token
&client_id={clientId}
&client_secret={clientSecret}
&refresh_token={refreshToken}
```

**Keycloak trả về:**
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI...",  // Token mới
  "refresh_token": "eyJhbGciOiJSUzI1NiIsInR5cCI...",  // Refresh token mới (có thể)
  "expires_in": 3600
}
```

#### **Bước 3: Trả về tokens mới**
```json
HTTP 200 OK

{
  "accessToken": "eyJhbGciOiJSUzI1NiIsInR5cCI...",
  "refreshToken": "eyJhbGciOiJSUzI1NiIsInR5cCI...",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

---

## 🔐 CÁC THÀNH PHẦN QUAN TRỌNG

### 1. Keycloak Configuration
- **Server URL**: `http://localhost:8080`
- **Realm**: `gocgac-htx`
- **Client ID**: `gocgac_app` (hoặc `gocgac-backend` tùy config)
- **Client Secret**: (được cấu hình trong Keycloak)
- **Admin Username**: `admin` (development, configurable)
- **Admin Password**: `admin123` (development, configurable)

### 2. Token Types
- **Access Token**: JWT token dùng để xác thực các API requests
  - Thời hạn: 1 giờ (3600 giây)
  - Chứa: user info, roles, permissions
- **Refresh Token**: Dùng để lấy access token mới
  - Thời hạn: 30 phút (1800 giây)

### 3. Database Schema
- **User table**: Lưu thông tin user, liên kết với Keycloak qua `keycloakId`
- **Role table**: Lưu roles trong hệ thống
- **User_Role table**: Many-to-many relationship

### 4. Security Flow
```
1. User đăng ký → Tạo trong Keycloak + Database
2. User đăng nhập → Keycloak xác thực → Trả về tokens
3. Client dùng access token → Gửi trong Authorization header
4. Server validate token → Cho phép truy cập API
5. Token hết hạn → Dùng refresh token để lấy token mới
```

---

## ⚠️ LƯU Ý QUAN TRỌNG

### 1. Đồng bộ dữ liệu
- User phải tồn tại trong **cả Keycloak và Database**
- `keycloakId` là cầu nối giữa 2 hệ thống
- Nếu user bị xóa trong Keycloak nhưng còn trong Database → Không thể đăng nhập

### 2. Password
- Keycloak lưu password dạng hash
- Database cũng lưu password hash (BCrypt)
- Khi đăng nhập, chỉ Keycloak xác thực password

### 3. Roles
- Roles được quản lý trong **Database** (source of truth cho business logic)
- Roles được **đồng bộ với Keycloak** (BẮT BUỘC cho authentication)
- **Client Roles** (trong client `gocgac_app`) được ưu tiên
- **Realm Roles** được dùng làm fallback
- Nếu role không tồn tại trong Keycloak, hệ thống tự động tạo realm role mới
- **Lưu ý**: Nếu không thể gán role trong Keycloak, user sẽ bị rollback (xóa khỏi Keycloak)

### 4. Error Handling
- Tất cả error messages được định nghĩa trong `KeycloakErrorConstants` (file: `common/constant/KeycloakErrorConstants.java`)
- Keycloak errors → Được catch và convert thành `AuthException` hoặc `RuntimeException`
- Database errors → Được catch và log
- Client nhận được error message rõ ràng từ constants
- **Rollback logic**: Nếu gán role thất bại, user sẽ bị xóa khỏi Keycloak để tránh orphaned users

**Các loại error constants:**
- `LOGIN_*`: Lỗi đăng nhập (invalid credentials, no token, etc.)
- `REGISTER_*`: Lỗi đăng ký (email exists, no permission, cannot create user, etc.)
- `ADMIN_TOKEN_*`: Lỗi lấy admin token (failed, no credentials, etc.)
- `USER_*`: Lỗi quản lý user (cannot set password, cannot delete, not found)
- `ROLE_*`: Lỗi gán role (cannot assign, not exists, cannot create)
- `CLIENT_*`: Lỗi client (not found, cannot get ID)
- `REFRESH_TOKEN_*`: Lỗi refresh token (failed, invalid)

### 5. Admin Token
- **Ưu tiên**: Master realm admin token (`admin/admin123` với client `admin-cli`)
  - Dùng cho development
  - Có quyền cao nhất trong tất cả realms
- **Fallback**: Service account token (client credentials)
  - Cần cấu hình roles: `manage-users`, `view-users`, `query-users` từ `realm-management` client
  - Phù hợp cho production
- Admin token dùng để:
  - Tạo user trong Keycloak
  - Gán roles cho user
  - Set password cho user
  - Xóa user (rollback)

---

## 📊 Sơ Đồ Luồng Tổng Quan

```
┌─────────┐
│ Client  │
└────┬────┘
     │ POST /api/auth/register
     ▼
┌─────────────────┐
│ AuthController  │
└────┬────────────┘
     │
     ▼
┌─────────────────┐
│  AuthService    │
└────┬────────────┘
     │
     ├──► KeycloakClient ──► Keycloak Server (Tạo user, Lấy token)
     │
     └──► UserRepository ──► PostgreSQL (Lưu thông tin user)
     │
     └──► RoleRepository ──► PostgreSQL (Lấy roles)
     │
     ▼
┌─────────────────┐
│ MessageResponse │ (Đăng ký: UserDTO)
│  AuthResponse   │ (Đăng nhập: Tokens + User Info)
└─────────────────┘
```

---

## 🧪 Test Flow

### Test đăng ký:
```bash
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123",
    "fullName": "Test User",
    "phone": "0123456789",
    "userType": "CUSTOMER"
  }'
```

### Test đăng nhập:
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'
```

### Test refresh token:
```bash
curl -X POST http://localhost:8081/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "eyJhbGciOiJSUzI1NiIsInR5cCI..."
  }'
```

---

## 📝 Tóm Tắt

1. **Đăng ký**: 
   - Tạo user trong Keycloak (với admin token) → Set password riêng
   - Lưu vào Database → Gán role trong Database
   - **Gán role trong Keycloak (BẮT BUỘC)** → Nếu thất bại, rollback (xóa user trong Keycloak)
   - Trả về `MessageResponse` với `UserDTO` (KHÔNG tự động đăng nhập)

2. **Đăng nhập**: 
   - Xác thực với Keycloak → Lấy tokens
   - Lấy user từ Database → Kiểm tra status
   - Cập nhật `lastLoginAt` → Trả về `AuthResponse` với tokens và user info

3. **Refresh Token**: 
   - Gửi refresh token → Keycloak trả về access token mới → Trả về cho client

## 🔄 Rollback Logic trong Đăng Ký

Nếu gán role trong Keycloak thất bại:
1. Xóa user khỏi Keycloak (để tránh orphaned users)
2. Transaction rollback trong database (do `@Transactional`)
3. Throw `AuthException` với message rõ ràng

Hệ thống sử dụng **hybrid approach**: 
- **Keycloak**: Quản lý authentication, credentials, và roles (cho JWT)
- **Database**: Quản lý business data, relationships, và roles (source of truth)

