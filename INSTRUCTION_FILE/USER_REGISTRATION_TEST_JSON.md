# JSON Test Data cho Đăng Ký User theo UserType

File này chứa các JSON mẫu để test chức năng đăng ký user với các UserType khác nhau trên Swagger hoặc Postman.

**Lưu ý:** 
- Endpoint đăng ký: `POST /api/auth/register`
- UserType mặc định là `CUSTOMER` nếu không chỉ định
- Một số UserType (như SUPER_ADMIN, COOPERATIVE_MANAGER) thường được tạo bởi admin hoặc thông qua quy trình đăng ký HTX

---

## 📋 Mục Lục

1. [CUSTOMER - Khách hàng](#customer---khách-hàng)
2. [SELLER - Người bán](#seller---người-bán)
3. [MEMBER - Thành viên HTX](#member---thành-viên-htx)
4. [ACCOUNTANT - Kế toán HTX](#accountant---kế-toán-htx)
5. [WAREHOUSE_STAFF - Nhân viên kho](#warehouse_staff---nhân-viên-kho)
6. [SHIPPER - Giao hàng](#shipper---giao-hàng)
7. [MODERATOR - Duyệt nội dung](#moderator---duyệt-nội-dung)
8. [AFFILIATE - Đối tác liên kết](#affiliate---đối-tác-liên-kết)
9. [COOPERATIVE_MANAGER - Quản lý HTX](#cooperative_manager---quản-lý-htx)
10. [SUPER_ADMIN - Super Admin](#super_admin---super-admin)
11. [JSON Tối Giản (Minimal)](#json-tối-giản-minimal)
12. [Lưu ý quan trọng](#lưu-ý-quan-trọng)

---

## 🔹 CUSTOMER - Khách hàng

**Endpoint:** `POST /api/auth/register`

**Mô tả:** UserType mặc định, dành cho khách hàng mua hàng.

**Request Body (Đầy đủ):**

```json
{
  "email": "customer@example.com",
  "password": "Customer123!@#",
  "fullName": "Nguyễn Văn Khách",
  "phone": "0912345678",
  "address": "123 Đường Nguyễn Văn Linh, Phường An Khánh, Quận 2, TP. Hồ Chí Minh",
  "userType": "CUSTOMER"
}
```

**Request Body (Tối giản - không cần chỉ định userType):**

```json
{
  "email": "customer2@example.com",
  "password": "Customer123!@#",
  "fullName": "Trần Thị Mua",
  "phone": "0987654321"
}
```

**Response mẫu (thành công - 201 Created):**

```json
{
  "status": 201,
  "message": "Đăng ký thành công",
  "data": {
    "id": 1,
    "email": "customer@example.com",
    "fullName": "Nguyễn Văn Khách",
    "phone": "0912345678",
    "avatarUrl": null,
    "roles": ["CUSTOMER"],
    "userType": "CUSTOMER"
  }
}
```

**Quyền hạn:**
- Xem sản phẩm
- Tạo và xem đơn hàng của mình
- Hủy đơn hàng
- Xem danh mục
- Xem HTX và cửa hàng
- Tạo và sửa đánh giá sản phẩm
- Xem khuyến mãi

---

## 🔹 SELLER - Người bán

**Endpoint:** `POST /api/auth/register`

**Mô tả:** Dành cho người bán (xã viên/thành viên HTX) muốn bán hàng trên hệ thống.

**Request Body:**

```json
{
  "email": "seller@example.com",
  "password": "Seller123!@#",
  "fullName": "Lê Văn Bán",
  "phone": "0912345679",
  "address": "456 Đường Lê Lợi, Phường Bến Nghé, Quận 1, TP. Hồ Chí Minh",
  "userType": "SELLER"
}
```

**Response mẫu:**

```json
{
  "status": 201,
  "message": "Đăng ký thành công",
  "data": {
    "id": 2,
    "email": "seller@example.com",
    "fullName": "Lê Văn Bán",
    "phone": "0912345679",
    "avatarUrl": null,
    "roles": ["SELLER"],
    "userType": "SELLER"
  }
}
```

**Quyền hạn:**
- Quản lý cửa hàng của mình
- Tạo, sửa, xóa sản phẩm
- Xem và cập nhật đơn hàng
- Quản lý tồn kho
- Tạo và quản lý khuyến mãi

**Lưu ý:** Sau khi đăng ký, Seller cần đăng ký Store để bắt đầu bán hàng.

---

## 🔹 MEMBER - Thành viên HTX

**Endpoint:** `POST /api/auth/register`

**Mô tả:** Dành cho thành viên HTX (đã được approve). Thường được tạo khi user đăng ký thành viên HTX và được HTX Manager phê duyệt.

**Request Body:**

```json
{
  "email": "member@example.com",
  "password": "Member123!@#",
  "fullName": "Phạm Văn Thành Viên",
  "phone": "0912345680",
  "address": "789 Đường Trần Hưng Đạo, Phường Cầu Ông Lãnh, Quận 1, TP. Hồ Chí Minh",
  "userType": "MEMBER"
}
```

**Response mẫu:**

```json
{
  "status": 201,
  "message": "Đăng ký thành công",
  "data": {
    "id": 3,
    "email": "member@example.com",
    "fullName": "Phạm Văn Thành Viên",
    "phone": "0912345680",
    "avatarUrl": null,
    "roles": ["MEMBER"],
    "userType": "MEMBER"
  }
}
```

**Quyền hạn:**
- Xem thông tin HTX
- Xem sản phẩm
- Xem danh mục
- Xem cửa hàng
- Xem đơn hàng
- Xem khuyến mãi

**Lưu ý:** Thường được tạo tự động khi user đăng ký thành viên HTX và được approve.

---

## 🔹 ACCOUNTANT - Kế toán HTX

**Endpoint:** `POST /api/auth/register`

**Mô tả:** Dành cho kế toán HTX, quản lý tài chính và thanh toán.

**Request Body:**

```json
{
  "email": "accountant@example.com",
  "password": "Accountant123!@#",
  "fullName": "Hoàng Thị Kế Toán",
  "phone": "0912345681",
  "address": "321 Đường Nguyễn Huệ, Phường Bến Nghé, Quận 1, TP. Hồ Chí Minh",
  "userType": "ACCOUNTANT"
}
```

**Response mẫu:**

```json
{
  "status": 201,
  "message": "Đăng ký thành công",
  "data": {
    "id": 4,
    "email": "accountant@example.com",
    "fullName": "Hoàng Thị Kế Toán",
    "phone": "0912345681",
    "avatarUrl": null,
    "roles": ["ACCOUNTANT"],
    "userType": "ACCOUNTANT"
  }
}
```

**Quyền hạn:**
- Xem và xử lý thanh toán
- Hoàn tiền
- Xem đơn hàng
- Xem thông tin người dùng
- Xem HTX và cửa hàng

---

## 🔹 WAREHOUSE_STAFF - Nhân viên kho

**Endpoint:** `POST /api/auth/register`

**Mô tả:** Dành cho nhân viên kho HTX, quản lý tồn kho.

**Request Body:**

```json
{
  "email": "warehouse@example.com",
  "password": "Warehouse123!@#",
  "fullName": "Võ Văn Kho",
  "phone": "0912345682",
  "address": "654 Đường Pasteur, Phường Nguyễn Thái Bình, Quận 1, TP. Hồ Chí Minh",
  "userType": "WAREHOUSE_STAFF"
}
```

**Response mẫu:**

```json
{
  "status": 201,
  "message": "Đăng ký thành công",
  "data": {
    "id": 5,
    "email": "warehouse@example.com",
    "fullName": "Võ Văn Kho",
    "phone": "0912345682",
    "avatarUrl": null,
    "roles": ["WAREHOUSE_STAFF"],
    "userType": "WAREHOUSE_STAFF"
  }
}
```

**Quyền hạn:**
- Xem và quản lý tồn kho
- Quản lý nhập xuất kho
- Xem và sửa sản phẩm
- Xem đơn hàng
- Xem cửa hàng

---

## 🔹 SHIPPER - Giao hàng

**Endpoint:** `POST /api/auth/register`

**Mô tả:** Dành cho nhân viên giao hàng, quản lý vận chuyển.

**Request Body:**

```json
{
  "email": "shipper@example.com",
  "password": "Shipper123!@#",
  "fullName": "Đặng Văn Giao",
  "phone": "0912345683",
  "address": "987 Đường Điện Biên Phủ, Phường 25, Quận Bình Thạnh, TP. Hồ Chí Minh",
  "userType": "SHIPPER"
}
```

**Response mẫu:**

```json
{
  "status": 201,
  "message": "Đăng ký thành công",
  "data": {
    "id": 6,
    "email": "shipper@example.com",
    "fullName": "Đặng Văn Giao",
    "phone": "0912345683",
    "avatarUrl": null,
    "roles": ["SHIPPER"],
    "userType": "SHIPPER"
  }
}
```

**Quyền hạn:**
- Xem và quản lý vận chuyển
- Cập nhật trạng thái vận chuyển
- Xem và cập nhật đơn hàng
- Xem thông tin người dùng

---

## 🔹 MODERATOR - Duyệt nội dung

**Endpoint:** `POST /api/auth/register`

**Mô tả:** Dành cho người duyệt nội dung, phê duyệt sản phẩm và đánh giá.

**Request Body:**

```json
{
  "email": "moderator@example.com",
  "password": "Moderator123!@#",
  "fullName": "Bùi Thị Duyệt",
  "phone": "0912345684",
  "address": "147 Đường Võ Văn Tần, Phường 6, Quận 3, TP. Hồ Chí Minh",
  "userType": "MODERATOR"
}
```

**Response mẫu:**

```json
{
  "status": 201,
  "message": "Đăng ký thành công",
  "data": {
    "id": 7,
    "email": "moderator@example.com",
    "fullName": "Bùi Thị Duyệt",
    "phone": "0912345684",
    "avatarUrl": null,
    "roles": ["MODERATOR"],
    "userType": "MODERATOR"
  }
}
```

**Quyền hạn:**
- Xem và phê duyệt sản phẩm
- Xem, phê duyệt và xóa đánh giá
- Xem cửa hàng
- Xem danh mục

---

## 🔹 AFFILIATE - Đối tác liên kết

**Endpoint:** `POST /api/auth/register`

**Mô tả:** Dành cho đối tác liên kết, xem và quản lý đơn hàng liên quan.

**Request Body:**

```json
{
  "email": "affiliate@example.com",
  "password": "Affiliate123!@#",
  "fullName": "Ngô Văn Đối Tác",
  "phone": "0912345685",
  "address": "258 Đường Lý Tự Trọng, Phường Bến Thành, Quận 1, TP. Hồ Chí Minh",
  "userType": "AFFILIATE"
}
```

**Response mẫu:**

```json
{
  "status": 201,
  "message": "Đăng ký thành công",
  "data": {
    "id": 8,
    "email": "affiliate@example.com",
    "fullName": "Ngô Văn Đối Tác",
    "phone": "0912345685",
    "avatarUrl": null,
    "roles": ["AFFILIATE"],
    "userType": "AFFILIATE"
  }
}
```

**Quyền hạn:**
- Xem đơn hàng
- Xem sản phẩm
- Xem cửa hàng
- Xem danh mục
- Xem khuyến mãi

---

## 🔹 COOPERATIVE_MANAGER - Quản lý HTX

**Endpoint:** `POST /api/auth/register`

**Mô tả:** Dành cho quản lý HTX. Thường được tạo tự động khi user đăng ký HTX và được Super Admin phê duyệt.

**Request Body:**

```json
{
  "email": "coopmanager@example.com",
  "password": "CoopManager123!@#",
  "fullName": "Trương Văn Quản Lý",
  "phone": "0912345686",
  "address": "369 Đường Nguyễn Đình Chiểu, Phường 5, Quận 3, TP. Hồ Chí Minh",
  "userType": "COOPERATIVE_MANAGER"
}
```

**Response mẫu:**

```json
{
  "status": 201,
  "message": "Đăng ký thành công",
  "data": {
    "id": 9,
    "email": "coopmanager@example.com",
    "fullName": "Trương Văn Quản Lý",
    "phone": "0912345686",
    "avatarUrl": null,
    "roles": ["COOPERATIVE_MANAGER"],
    "userType": "COOPERATIVE_MANAGER"
  }
}
```

**Quyền hạn:**
- Quản lý HTX của mình
- Quản lý thành viên HTX
- Quản lý cửa hàng HTX
- Tạo, sửa, xóa sản phẩm
- Quản lý đơn hàng
- Quản lý tồn kho
- Quản lý vận chuyển
- Phê duyệt đánh giá
- Tạo và quản lý khuyến mãi

**Lưu ý:** 
- Thường được tạo tự động khi đăng ký HTX được approve
- Có thể được tạo thủ công bởi Super Admin

---

## 🔹 SUPER_ADMIN - Super Admin

**Endpoint:** `POST /api/auth/register`

**Mô tả:** Dành cho Super Admin - quyền cao nhất trong hệ thống. Thường được tạo thủ công hoặc qua script khởi tạo.

**Request Body:**

```json
{
  "email": "admin@example.com",
  "password": "Admin123!@#",
  "fullName": "Admin Hệ Thống",
  "phone": "0912345687",
  "address": "159 Đường Nam Kỳ Khởi Nghĩa, Phường 6, Quận 3, TP. Hồ Chí Minh",
  "userType": "SUPER_ADMIN"
}
```

**Response mẫu:**

```json
{
  "status": 201,
  "message": "Đăng ký thành công",
  "data": {
    "id": 10,
    "email": "admin@example.com",
    "fullName": "Admin Hệ Thống",
    "phone": "0912345687",
    "avatarUrl": null,
    "roles": ["SUPER_ADMIN"],
    "userType": "SUPER_ADMIN"
  }
}
```

**Quyền hạn:**
- **Tất cả quyền** trong hệ thống
- Quản lý users, roles, permissions
- Phê duyệt đăng ký HTX
- Quản lý toàn bộ sản phẩm, đơn hàng, thanh toán
- Cấu hình hệ thống

**Lưu ý:** 
- Thường được tạo thủ công hoặc qua script khởi tạo
- Nên hạn chế số lượng Super Admin

---

## 🔹 JSON Tối Giản (Minimal)

### CUSTOMER (mặc định):

```json
{
  "email": "test@example.com",
  "password": "Test123456",
  "fullName": "Nguyễn Văn Test",
  "phone": "0912345678"
}
```

### SELLER:

```json
{
  "email": "seller@example.com",
  "password": "Seller123456",
  "fullName": "Lê Văn Seller",
  "phone": "0987654321",
  "userType": "SELLER"
}
```

### ACCOUNTANT:

```json
{
  "email": "accountant@example.com",
  "password": "Accountant123456",
  "fullName": "Hoàng Thị Kế Toán",
  "phone": "0911111111",
  "userType": "ACCOUNTANT"
}
```

---

## 🔹 Lưu ý quan trọng

### 1. Validation Rules

- **email**: 
  - Bắt buộc
  - Phải là email hợp lệ
  - Phải unique trong hệ thống
  
- **password**: 
  - Bắt buộc
  - Tối thiểu 8 ký tự
  - Nên có chữ hoa, chữ thường, số và ký tự đặc biệt
  
- **fullName**: 
  - Bắt buộc
  - Không được để trống
  
- **phone**: 
  - Pattern: `^[0-9]{10,11}$`
  - Phải có 10 hoặc 11 chữ số
  
- **address**: 
  - Tùy chọn
  - Có thể để null hoặc bỏ qua
  
- **userType**: 
  - Tùy chọn
  - Mặc định là `CUSTOMER`
  - Phải là một trong các giá trị: `SUPER_ADMIN`, `COOPERATIVE_MANAGER`, `SELLER`, `CUSTOMER`, `MEMBER`, `ACCOUNTANT`, `WAREHOUSE_STAFF`, `SHIPPER`, `MODERATOR`, `AFFILIATE`

### 2. Authentication

- Sau khi đăng ký thành công, user **KHÔNG tự động đăng nhập**
- User phải gọi `POST /api/auth/login` để lấy JWT token
- Token cần được gửi trong header `Authorization: Bearer <token>` cho các API yêu cầu authentication

### 3. Role Assignment

- Mỗi UserType sẽ được gán role tương ứng tự động:
  - `CUSTOMER` → Role `CUSTOMER`
  - `SELLER` → Role `SELLER`
  - `COOPERATIVE_MANAGER` → Role `COOPERATIVE_MANAGER`
  - `SUPER_ADMIN` → Role `SUPER_ADMIN`
  - Và tương tự cho các UserType khác

### 4. Keycloak Integration

- User được tạo trong Keycloak tự động
- Role được gán trong Keycloak (realm role hoặc client role)
- Nếu tạo user thất bại trong Keycloak, user trong database sẽ bị rollback

### 5. UserType Flow

- **CUSTOMER**: Có thể đăng ký trực tiếp
- **SELLER**: Có thể đăng ký trực tiếp, sau đó cần đăng ký Store
- **COOPERATIVE_MANAGER**: Thường được tạo khi đăng ký HTX được approve
- **MEMBER**: Thường được tạo khi đăng ký thành viên HTX được approve
- **SUPER_ADMIN**: Thường được tạo thủ công hoặc qua script

### 6. Response Codes

- **201 Created**: Đăng ký thành công
- **400 Bad Request**: Validation lỗi hoặc email đã tồn tại
- **500 Internal Server Error**: Lỗi server hoặc Keycloak

### 7. Error Response Mẫu

**Email đã tồn tại:**
```json
{
  "status": 400,
  "message": "Email đã được sử dụng",
  "timestamp": "2025-12-24T22:51:55.4249292",
  "errors": null
}
```

**Validation lỗi:**
```json
{
  "status": 400,
  "message": "Validation failed",
  "timestamp": "2025-12-24T22:51:55.4249292",
  "errors": {
    "email": "Email không hợp lệ",
    "password": "Mật khẩu phải có ít nhất 8 ký tự"
  }
}
```

---

## 🎯 Quy Trình Test Đầy Đủ

### 1. Đăng ký User

1. **Chọn UserType phù hợp** (ví dụ: CUSTOMER)
2. **Gọi `POST /api/auth/register`** với JSON body
3. **Lưu lại thông tin user** từ response (id, email)

### 2. Đăng nhập

1. **Gọi `POST /api/auth/login`** với email và password vừa đăng ký
2. **Lưu lại JWT token** từ response
3. **Sử dụng token** cho các API yêu cầu authentication

### 3. Test Permissions

1. **Gọi các API** theo quyền của UserType
2. **Kiểm tra** xem user có thể thực hiện các hành động phù hợp không

---

**Chúc bạn test thành công! 🚀**

