# JSON Test Data cho Quản Lý Role và Permission

File này chứa các JSON mẫu để test chức năng quản lý Role và Permission trên Swagger hoặc Postman.

**Lưu ý:** Tất cả các endpoints yêu cầu quyền `SUPER_ADMIN`.

---

## 📋 Mục Lục

1. [Tạo Permission mới](#tạo-permission-mới)
2. [Tạo Role mới](#tạo-role-mới)
3. [Gán Permission cho Role](#gán-permission-cho-role)
4. [Lấy danh sách Roles](#lấy-danh-sách-roles)
5. [Lấy danh sách Permissions](#lấy-danh-sách-permissions)
6. [JSON Tối Giản (Minimal)](#json-tối-giản-minimal)
7. [Ví dụ các Permission phổ biến](#ví-dụ-các-permission-phổ-biến)
8. [Ví dụ các Role phổ biến](#ví-dụ-các-role-phổ-biến)

---

## 🔹 Tạo Permission mới

**Endpoint:** `POST /api/admin/roles/permissions`

**Request Body (Đầy đủ):**

```json
{
  "name": "Tạo sản phẩm",
  "code": "product:create",
  "description": "Quyền tạo sản phẩm mới trong hệ thống",
  "resource": "product",
  "action": "create"
}
```

**Request Body (Tối giản - chỉ các trường bắt buộc):**

```json
{
  "name": "Tạo sản phẩm",
  "code": "product:create"
}
```

**Response mẫu (thành công - 201 Created):**

```json
{
  "id": 1,
  "name": "Tạo sản phẩm",
  "code": "product:create",
  "description": "Quyền tạo sản phẩm mới trong hệ thống",
  "resource": "product",
  "action": "create",
  "createdAt": "2025-12-24T22:51:42.589918",
  "updatedAt": "2025-12-24T22:51:42.589918"
}
```

**Response mẫu (lỗi - code đã tồn tại):**

```json
{
  "status": 400,
  "message": "Code permission đã tồn tại",
  "timestamp": "2025-12-24T22:51:55.4249292",
  "errors": null
}
```

**Giải thích các trường:**

- `name`: Tên permission (bắt buộc, tối đa 100 ký tự, unique)
- `code`: Mã permission (bắt buộc, tối đa 100 ký tự, unique, format: `resource:action`)
  - Ví dụ: `product:create`, `order:view`, `user:delete`
  - Pattern: `^[a-z0-9_]+:[a-z0-9_]+$`
- `description`: Mô tả permission (tùy chọn, TEXT)
- `resource`: Tên resource (tùy chọn, tối đa 50 ký tự)
  - Ví dụ: `product`, `order`, `user`, `cooperative`, `store`
- `action`: Hành động (tùy chọn, tối đa 50 ký tự)
  - Ví dụ: `create`, `read`, `update`, `delete`, `view`, `approve`, `reject`

**Lưu ý:**
- `code` phải unique trong hệ thống
- `code` phải tuân theo format `resource:action` (chữ thường, số, dấu gạch dưới)
- `name` cũng phải unique

---

## 🔹 Tạo Role mới

**Endpoint:** `POST /api/admin/roles`

**Cách 1: Sử dụng Query Parameters (Hiện tại)**

```
POST /api/admin/roles?name=Quản lý sản phẩm&code=PRODUCT_MANAGER&description=Role quản lý sản phẩm
```

**Cách 2: Sử dụng JSON Body (Đề xuất - cần cập nhật Controller)**

**Request Body:**

```json
{
  "name": "Quản lý sản phẩm",
  "code": "PRODUCT_MANAGER",
  "description": "Role quản lý sản phẩm trong hệ thống, có quyền tạo, sửa, xóa sản phẩm"
}
```

**Request Body (Tối giản):**

```json
{
  "name": "Quản lý sản phẩm",
  "code": "PRODUCT_MANAGER"
}
```

**Response mẫu (thành công - 200 OK):**

```json
{
  "id": 1,
  "name": "Quản lý sản phẩm",
  "code": "PRODUCT_MANAGER",
  "description": "Role quản lý sản phẩm trong hệ thống, có quyền tạo, sửa, xóa sản phẩm",
  "permissions": [],
  "createdAt": "2025-12-24T22:51:42.589918",
  "updatedAt": "2025-12-24T22:51:42.589918"
}
```

**Response mẫu (lỗi - code đã tồn tại):**

```json
{
  "status": 400,
  "message": "Code role đã tồn tại",
  "timestamp": "2025-12-24T22:51:55.4249292",
  "errors": null
}
```

**Giải thích các trường:**

- `name`: Tên role (bắt buộc, tối đa 100 ký tự, unique)
- `code`: Mã role (bắt buộc, tối đa 50 ký tự, unique)
  - Thường dùng format UPPER_SNAKE_CASE: `PRODUCT_MANAGER`, `ORDER_ADMIN`, `COOPERATIVE_MANAGER`
- `description`: Mô tả role (tùy chọn, TEXT)

**Lưu ý:**
- `code` phải unique trong hệ thống
- `name` cũng phải unique
- Sau khi tạo role, cần gán permissions cho role (xem phần tiếp theo)

---

## 🔹 Gán Permission cho Role

**Endpoint:** `POST /api/admin/roles/{roleId}/permissions/{permissionId}`

**Ví dụ:** `POST /api/admin/roles/1/permissions/5`

**Request:** Không cần body, chỉ cần path parameters

**Response mẫu (thành công - 200 OK):**

```json
{
  "id": 1,
  "name": "Quản lý sản phẩm",
  "code": "PRODUCT_MANAGER",
  "description": "Role quản lý sản phẩm trong hệ thống",
  "permissions": [
    {
      "id": 5,
      "name": "Tạo sản phẩm",
      "code": "product:create",
      "description": "Quyền tạo sản phẩm mới trong hệ thống",
      "resource": "product",
      "action": "create",
      "createdAt": "2025-12-24T22:50:00.000000",
      "updatedAt": "2025-12-24T22:50:00.000000"
    }
  ],
  "createdAt": "2025-12-24T22:51:42.589918",
  "updatedAt": "2025-12-24T22:51:42.589918"
}
```

**Response mẫu (lỗi - role không tồn tại):**

```json
{
  "status": 404,
  "message": "Role không tồn tại",
  "timestamp": "2025-12-24T22:51:55.4249292",
  "errors": null
}
```

**Response mẫu (lỗi - permission không tồn tại):**

```json
{
  "status": 404,
  "message": "Permission không tồn tại",
  "timestamp": "2025-12-24T22:51:55.4249292",
  "errors": null
}
```

**Lưu ý:**
- Có thể gán nhiều permissions cho một role bằng cách gọi endpoint nhiều lần
- Mỗi role có thể có nhiều permissions
- Mỗi permission có thể được gán cho nhiều roles
- Nếu permission đã được gán cho role, hệ thống sẽ không báo lỗi (idempotent)

---

## 🔹 Lấy danh sách Roles

**Endpoint:** `GET /api/admin/roles`

**Response mẫu:**

```json
[
  {
    "id": 1,
    "name": "Quản lý sản phẩm",
    "code": "PRODUCT_MANAGER",
    "description": "Role quản lý sản phẩm trong hệ thống",
    "permissions": [
      {
        "id": 1,
        "name": "Tạo sản phẩm",
        "code": "product:create",
        "description": "Quyền tạo sản phẩm mới",
        "resource": "product",
        "action": "create",
        "createdAt": "2025-12-24T22:50:00.000000",
        "updatedAt": "2025-12-24T22:50:00.000000"
      },
      {
        "id": 2,
        "name": "Sửa sản phẩm",
        "code": "product:update",
        "description": "Quyền sửa sản phẩm",
        "resource": "product",
        "action": "update",
        "createdAt": "2025-12-24T22:50:00.000000",
        "updatedAt": "2025-12-24T22:50:00.000000"
      }
    ],
    "createdAt": "2025-12-24T22:51:42.589918",
    "updatedAt": "2025-12-24T22:51:42.589918"
  },
  {
    "id": 2,
    "name": "Quản lý đơn hàng",
    "code": "ORDER_MANAGER",
    "description": "Role quản lý đơn hàng",
    "permissions": [],
    "createdAt": "2025-12-24T22:52:00.000000",
    "updatedAt": "2025-12-24T22:52:00.000000"
  }
]
```

---

## 🔹 Lấy danh sách Permissions

**Endpoint:** `GET /api/admin/roles/permissions`

**Response mẫu:**

```json
[
  {
    "id": 1,
    "name": "Tạo sản phẩm",
    "code": "product:create",
    "description": "Quyền tạo sản phẩm mới trong hệ thống",
    "resource": "product",
    "action": "create",
    "createdAt": "2025-12-24T22:50:00.000000",
    "updatedAt": "2025-12-24T22:50:00.000000"
  },
  {
    "id": 2,
    "name": "Xem sản phẩm",
    "code": "product:view",
    "description": "Quyền xem danh sách và chi tiết sản phẩm",
    "resource": "product",
    "action": "view",
    "createdAt": "2025-12-24T22:50:00.000000",
    "updatedAt": "2025-12-24T22:50:00.000000"
  },
  {
    "id": 3,
    "name": "Sửa sản phẩm",
    "code": "product:update",
    "description": "Quyền sửa thông tin sản phẩm",
    "resource": "product",
    "action": "update",
    "createdAt": "2025-12-24T22:50:00.000000",
    "updatedAt": "2025-12-24T22:50:00.000000"
  }
]
```

---

## 🔹 JSON Tối Giản (Minimal)

### Tạo Permission tối giản:

```json
{
  "name": "Xem đơn hàng",
  "code": "order:view"
}
```

### Tạo Role tối giản (Query Params):

```
POST /api/admin/roles?name=Quản lý đơn hàng&code=ORDER_MANAGER
```

---

## 🔹 Ví dụ các Permission phổ biến

### Permissions cho Product (Sản phẩm):

```json
// 1. Tạo sản phẩm
{
  "name": "Tạo sản phẩm",
  "code": "product:create",
  "description": "Quyền tạo sản phẩm mới",
  "resource": "product",
  "action": "create"
}

// 2. Xem sản phẩm
{
  "name": "Xem sản phẩm",
  "code": "product:view",
  "description": "Quyền xem danh sách và chi tiết sản phẩm",
  "resource": "product",
  "action": "view"
}

// 3. Sửa sản phẩm
{
  "name": "Sửa sản phẩm",
  "code": "product:update",
  "description": "Quyền sửa thông tin sản phẩm",
  "resource": "product",
  "action": "update"
}

// 4. Xóa sản phẩm
{
  "name": "Xóa sản phẩm",
  "code": "product:delete",
  "description": "Quyền xóa sản phẩm",
  "resource": "product",
  "action": "delete"
}

// 5. Phê duyệt sản phẩm
{
  "name": "Phê duyệt sản phẩm",
  "code": "product:approve",
  "description": "Quyền phê duyệt sản phẩm",
  "resource": "product",
  "action": "approve"
}
```

### Permissions cho Order (Đơn hàng):

```json
// 1. Xem đơn hàng
{
  "name": "Xem đơn hàng",
  "code": "order:view",
  "description": "Quyền xem danh sách và chi tiết đơn hàng",
  "resource": "order",
  "action": "view"
}

// 2. Tạo đơn hàng
{
  "name": "Tạo đơn hàng",
  "code": "order:create",
  "description": "Quyền tạo đơn hàng mới",
  "resource": "order",
  "action": "create"
}

// 3. Cập nhật đơn hàng
{
  "name": "Cập nhật đơn hàng",
  "code": "order:update",
  "description": "Quyền cập nhật trạng thái đơn hàng",
  "resource": "order",
  "action": "update"
}

// 4. Hủy đơn hàng
{
  "name": "Hủy đơn hàng",
  "code": "order:cancel",
  "description": "Quyền hủy đơn hàng",
  "resource": "order",
  "action": "cancel"
}
```

### Permissions cho User (Người dùng):

```json
// 1. Xem người dùng
{
  "name": "Xem người dùng",
  "code": "user:view",
  "description": "Quyền xem danh sách và thông tin người dùng",
  "resource": "user",
  "action": "view"
}

// 2. Tạo người dùng
{
  "name": "Tạo người dùng",
  "code": "user:create",
  "description": "Quyền tạo tài khoản người dùng mới",
  "resource": "user",
  "action": "create"
}

// 3. Sửa người dùng
{
  "name": "Sửa người dùng",
  "code": "user:update",
  "description": "Quyền sửa thông tin người dùng",
  "resource": "user",
  "action": "update"
}

// 4. Xóa người dùng
{
  "name": "Xóa người dùng",
  "code": "user:delete",
  "description": "Quyền xóa tài khoản người dùng",
  "resource": "user",
  "action": "delete"
}
```

### Permissions cho Cooperative (Hợp tác xã):

```json
// 1. Xem HTX
{
  "name": "Xem HTX",
  "code": "cooperative:view",
  "description": "Quyền xem danh sách và thông tin HTX",
  "resource": "cooperative",
  "action": "view"
}

// 2. Phê duyệt đăng ký HTX
{
  "name": "Phê duyệt đăng ký HTX",
  "code": "cooperative:approve",
  "description": "Quyền phê duyệt đơn đăng ký HTX",
  "resource": "cooperative",
  "action": "approve"
}

// 3. Từ chối đăng ký HTX
{
  "name": "Từ chối đăng ký HTX",
  "code": "cooperative:reject",
  "description": "Quyền từ chối đơn đăng ký HTX",
  "resource": "cooperative",
  "action": "reject"
}
```

### Permissions cho Role & Permission:

```json
// 1. Quản lý Role
{
  "name": "Quản lý Role",
  "code": "role:manage",
  "description": "Quyền tạo, sửa, xóa role",
  "resource": "role",
  "action": "manage"
}

// 2. Quản lý Permission
{
  "name": "Quản lý Permission",
  "code": "permission:manage",
  "description": "Quyền tạo, sửa, xóa permission",
  "resource": "permission",
  "action": "manage"
}
```

---

## 🔹 Ví dụ các Role phổ biến

### Role: PRODUCT_MANAGER

**Tạo Role:**

```
POST /api/admin/roles?name=Quản lý sản phẩm&code=PRODUCT_MANAGER&description=Role quản lý sản phẩm, có quyền tạo, sửa, xóa, phê duyệt sản phẩm
```

**Gán Permissions (giả sử đã có các permission IDs):**

```
POST /api/admin/roles/1/permissions/1  // product:create
POST /api/admin/roles/1/permissions/2  // product:view
POST /api/admin/roles/1/permissions/3  // product:update
POST /api/admin/roles/1/permissions/4  // product:delete
POST /api/admin/roles/1/permissions/5  // product:approve
```

### Role: ORDER_MANAGER

**Tạo Role:**

```
POST /api/admin/roles?name=Quản lý đơn hàng&code=ORDER_MANAGER&description=Role quản lý đơn hàng, có quyền xem, cập nhật, hủy đơn hàng
```

**Gán Permissions:**

```
POST /api/admin/roles/2/permissions/6  // order:view
POST /api/admin/roles/2/permissions/7  // order:create
POST /api/admin/roles/2/permissions/8  // order:update
POST /api/admin/roles/2/permissions/9  // order:cancel
```

### Role: COOPERATIVE_ADMIN

**Tạo Role:**

```
POST /api/admin/roles?name=Quản lý HTX&code=COOPERATIVE_ADMIN&description=Role quản lý HTX, có quyền phê duyệt, từ chối đơn đăng ký HTX
```

**Gán Permissions:**

```
POST /api/admin/roles/3/permissions/10  // cooperative:view
POST /api/admin/roles/3/permissions/11  // cooperative:approve
POST /api/admin/roles/3/permissions/12  // cooperative:reject
```

### Role: USER_ADMIN

**Tạo Role:**

```
POST /api/admin/roles?name=Quản lý người dùng&code=USER_ADMIN&description=Role quản lý người dùng, có quyền xem, tạo, sửa, xóa người dùng
```

**Gán Permissions:**

```
POST /api/admin/roles/4/permissions/13  // user:view
POST /api/admin/roles/4/permissions/14  // user:create
POST /api/admin/roles/4/permissions/15  // user:update
POST /api/admin/roles/4/permissions/16  // user:delete
```

---

## 🎯 Quy Trình Test Đầy Đủ

### 1. Tạo Permissions trước

1. **Tạo các permissions cần thiết:**
   - Gọi `POST /api/admin/roles/permissions` với JSON body
   - Lưu lại `id` của mỗi permission từ response

### 2. Tạo Roles

1. **Tạo role:**
   - Gọi `POST /api/admin/roles` với query params hoặc JSON body
   - Lưu lại `id` của role từ response

### 3. Gán Permissions cho Roles

1. **Gán permissions:**
   - Gọi `POST /api/admin/roles/{roleId}/permissions/{permissionId}` cho mỗi permission
   - Có thể gán nhiều permissions cho một role

### 4. Kiểm tra kết quả

1. **Xem danh sách roles:**
   - Gọi `GET /api/admin/roles` để xem tất cả roles và permissions của chúng

2. **Xem danh sách permissions:**
   - Gọi `GET /api/admin/roles/permissions` để xem tất cả permissions

---

## ⚠️ Lưu Ý Khi Test

1. **Authentication:** Tất cả endpoints đều yêu cầu JWT token với role `SUPER_ADMIN`:
   ```
   Authorization: Bearer <your-jwt-token>
   ```

2. **Validation:**
   - `code` của permission phải tuân theo format `resource:action` (ví dụ: `product:create`)
   - `code` của role thường dùng UPPER_SNAKE_CASE (ví dụ: `PRODUCT_MANAGER`)
   - `code` và `name` phải unique trong hệ thống

3. **Thứ tự tạo:**
   - Nên tạo Permissions trước, sau đó mới tạo Roles
   - Sau khi có Roles và Permissions, mới gán Permissions cho Roles

4. **Code Format:**
   - Permission code: `resource:action` (chữ thường, số, dấu gạch dưới)
   - Role code: `UPPER_SNAKE_CASE` (chữ hoa, dấu gạch dưới)

5. **Unique Constraints:**
   - Permission `code` phải unique
   - Permission `name` phải unique
   - Role `code` phải unique
   - Role `name` phải unique

6. **Relationship:**
   - Một Role có thể có nhiều Permissions
   - Một Permission có thể được gán cho nhiều Roles
   - Gán permission cho role là idempotent (gán lại không báo lỗi)

---

## 📌 Ví Dụ Workflow Hoàn Chỉnh

### Bước 1: Tạo Permissions cho Product

```json
// Permission 1: product:create
POST /api/admin/roles/permissions
{
  "name": "Tạo sản phẩm",
  "code": "product:create",
  "resource": "product",
  "action": "create"
}
// Response: { "id": 1, ... }

// Permission 2: product:view
POST /api/admin/roles/permissions
{
  "name": "Xem sản phẩm",
  "code": "product:view",
  "resource": "product",
  "action": "view"
}
// Response: { "id": 2, ... }

// Permission 3: product:update
POST /api/admin/roles/permissions
{
  "name": "Sửa sản phẩm",
  "code": "product:update",
  "resource": "product",
  "action": "update"
}
// Response: { "id": 3, ... }
```

### Bước 2: Tạo Role PRODUCT_MANAGER

```
POST /api/admin/roles?name=Quản lý sản phẩm&code=PRODUCT_MANAGER&description=Role quản lý sản phẩm
// Response: { "id": 1, ... }
```

### Bước 3: Gán Permissions cho Role

```
POST /api/admin/roles/1/permissions/1  // product:create
POST /api/admin/roles/1/permissions/2  // product:view
POST /api/admin/roles/1/permissions/3  // product:update
```

### Bước 4: Kiểm tra kết quả

```
GET /api/admin/roles
// Response sẽ hiển thị role PRODUCT_MANAGER với 3 permissions đã gán
```

---

**Chúc bạn test thành công! 🚀**

