# Hướng dẫn cấu hình Keycloak cho hệ thống GocGac

## 1. Khởi động Keycloak

Chạy lệnh sau để khởi động Keycloak và PostgreSQL:

```bash
docker-compose up -d
```

Keycloak sẽ chạy tại: http://localhost:8080

## 2. Đăng nhập vào Keycloak Admin Console

1. Truy cập: http://localhost:8080
2. Click vào "Administration Console"
3. Đăng nhập với:
   - Username: `admin`
   - Password: `admin123`

## 3. Tạo Realm

1. Trong Admin Console, click vào dropdown "Master" ở góc trên bên trái
2. Click "Create Realm"
3. Nhập tên realm: `gocgac-htx`
4. Click "Create"

## 4. Tạo Client

1. Trong menu bên trái, chọn "Clients"
2. Click "Create client"
3. Điền thông tin:
   - Client type: `OpenID Connect`
   - Client ID: `gocgac_app` (phải khớp với `keycloak.resource` trong application-dev.yml)
   - Click "Next"
4. Cấu hình Capability config:
   - Client authentication: `ON` (Confidential)
   - Authorization: `OFF`
   - Standard flow: `ON`
   - Direct access grants: `ON`
   - Service accounts roles: `ON` (quan trọng: để có thể tạo user qua API)
   - Click "Next"
5. Cấu hình Login settings:
   - Root URL: `http://localhost:8086`
   - Valid redirect URIs: `http://localhost:8086/*`
   - Web origins: `http://localhost:8086`
   - Click "Save"

## 5. Lấy Client Secret

1. Vào tab "Credentials" của client vừa tạo
2. Copy "Client secret"
3. Cập nhật vào file `application-dev.yml`:
   ```yaml
   keycloak:
     credentials:
       secret: <client-secret-here>
   ```

## 5.1. Cấu hình Service Account Roles (BẮT BUỘC)

**Quan trọng:** Code sẽ ưu tiên sử dụng service account từ realm `gocgac-htx` (đúng với realm đã tạo).
Nếu service account chưa được cấu hình roles, code sẽ fallback sang master realm admin (chỉ cho development).

**Cấu hình Service Account Roles:**

1. Vào Keycloak Admin Console: http://localhost:8080
2. Chọn realm `gocgac-htx` (không phải master realm)
3. Vào "Clients" > chọn client `gocgac_app`
4. Vào tab "Service accounts roles"
5. Click "Assign role"
6. Chọn "Filter by clients" > chọn `realm-management`
7. Gán các roles sau (quan trọng):
   - `manage-users` (để tạo/sửa/xóa user)
   - `view-users` (để xem danh sách user)
   - `query-users` (để tìm kiếm user)
8. Click "Assign"

**Hoặc** có thể gán realm roles (dễ hơn cho development):
- Chọn "Filter by roles" > chọn realm roles
- Gán role `realm-admin` (có tất cả quyền trong realm `gocgac-htx`)

**Lưu ý:**
- Service account sẽ dùng realm `gocgac-htx` (đúng với realm đã tạo)
- Master realm chỉ dùng như fallback nếu service account chưa cấu hình
- Trong production, nên dùng service account với roles cụ thể thay vì `realm-admin`

## 6. Tạo Roles

1. Trong menu bên trái, chọn "Realm roles"
2. Click "Create role" và tạo các role sau:
   - `SUPER_ADMIN`
   - `COOPERATIVE_MANAGER`
   - `SELLER`
   - `CUSTOMER`
   - `ACCOUNTANT`
   - `WAREHOUSE_STAFF`
   - `SHIPPER`
   - `MODERATOR`

## 7. Cấu hình Token Mapper (để roles xuất hiện trong JWT)

1. Vào "Clients" > `gocgac-backend` > "Client scopes"
2. Click vào "gocgac-backend-dedicated"
3. Vào tab "Mappers"
4. Click "Add mapper" > "By configuration" > "Realm roles"
5. Điền thông tin:
   - Name: `realm-roles`
   - Token Claim Name: `realm_access.roles`
   - Add to access token: `ON`
   - Add to ID token: `ON`
   - Click "Save"

## 8. Tạo User Test

1. Vào "Users" > "Create new user"
2. Điền thông tin:
   - Username: `test@example.com`
   - Email: `test@example.com`
   - First name: `Test`
   - Last name: `User`
   - Email verified: `ON`
   - Click "Create"
3. Vào tab "Credentials", set password:
   - Password: `password123`
   - Temporary: `OFF`
   - Click "Save"
4. Vào tab "Role mapping", assign role: `CUSTOMER`

## 9. Test API

### Kiểm tra cấu hình Keycloak (OpenID Connect Discovery)

Trước khi test các API khác, hãy kiểm tra xem Keycloak đã được cấu hình đúng chưa:

```bash
# Kiểm tra endpoint OpenID Connect Discovery
curl http://localhost:8080/realms/gocgac-htx/.well-known/openid-configuration
```

**Lưu ý quan trọng:**
- URL phải chính xác: `http://localhost:8080/realms/gocgac-htx/.well-known/openid-configuration`
- Không có dấu `/` thừa ở cuối
- Realm name phải khớp với realm đã tạo trong Keycloak Admin Console
- Keycloak phải đã khởi động hoàn toàn (kiểm tra bằng: `curl http://localhost:8080/health/ready`)

**Nếu gặp lỗi "Unable to find matching target resource method":**
1. Kiểm tra realm đã được tạo chưa:
   - Vào Keycloak Admin Console: http://localhost:8080
   - Đăng nhập với admin/admin123
   - Kiểm tra dropdown realm ở góc trên bên trái có `gocgac-htx` không
   
2. Kiểm tra Keycloak đã khởi động hoàn toàn:
   ```bash
   curl http://localhost:8080/health/ready
   ```
   Phải trả về `{"status":"UP"}`

3. Kiểm tra URL format:
   - Đúng: `http://localhost:8080/realms/gocgac-htx/.well-known/openid-configuration`
   - Sai: `http://localhost:8080/realms/gocgac-htx/.well-known/openid-configuration/` (có dấu `/` thừa)

### Đăng ký
```bash
curl -X POST http://localhost:8086/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123",
    "fullName": "Test User",
    "phone": "0123456789",
    "userType": "CUSTOMER"
  }'
```

### Đăng nhập
```bash
curl -X POST http://localhost:8086/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123"
  }'
```

### Sử dụng token
```bash
curl -X GET http://localhost:8086/api/customer/profile \
  -H "Authorization: Bearer <access-token>"
```

## 10. Lưu ý

- Đảm bảo Keycloak đã khởi động hoàn toàn trước khi chạy ứng dụng Spring Boot
- Client secret cần được cập nhật trong `application.properties`
- Roles trong Keycloak phải khớp với roles trong database
- JWT token có thời hạn, cần refresh token khi hết hạn

