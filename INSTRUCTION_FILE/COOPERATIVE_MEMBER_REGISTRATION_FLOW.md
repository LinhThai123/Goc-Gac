# Luồng Đăng Ký Thành Viên HTX (Cooperative Member Registration)

## 📋 Tổng Quan

Chức năng này cho phép **User (CUSTOMER)** đăng ký trở thành thành viên của **HTX (Hợp Tác Xã)**. Sau khi HTX Manager phê duyệt, user trở thành thành viên HTX. Nếu muốn bán hàng, thành viên cần đăng ký **Seller** riêng, khi đó hệ thống sẽ tạo **Store** với cả `seller_id` và `cooperative_id`.

## 🎯 Mục Đích

- User (CUSTOMER) có thể đăng ký thành viên HTX
- HTX Manager có thể quản lý danh sách thành viên
- Thành viên HTX có thể đăng ký Seller để bán hàng (tạo Store)
- Mỗi HTX có 1 Store, mỗi Seller (thành viên) có 1 Store riêng

---

## 📊 Kiến Trúc

### **Entity: CooperativeMember**

```java
CooperativeMember {
    id: Long
    cooperativeId: Long          // ID của HTX
    userId: Long                // ID của User (CUSTOMER hoặc SELLER)
    role: String                // Vai trò trong HTX (member, manager, etc.)
    status: ApprovalStatus      // PENDING, APPROVED, REJECTED
    rejectionReason: String     // Lý do từ chối (nếu bị reject)
    appliedAt: LocalDateTime    // Thời gian đăng ký
    joinedAt: LocalDateTime     // Thời gian được approve
    reviewedAt: LocalDateTime   // Thời gian được review
    reviewedBy: Long            // ID của người review (COOPERATIVE_MANAGER)
}
```

### **Mối Quan Hệ**

```
User (CUSTOMER)
  ↓ (1:N)
CooperativeMember
  ↓ (N:1)
Cooperative (HTX)

Khi APPROVED:
  User trở thành thành viên HTX (KHÔNG tạo Store)

Nếu muốn bán hàng:
  User đăng ký Seller → Tạo Store (với cả seller_id và cooperative_id)
```

---

## 🔄 Luồng Hoạt Động Chi Tiết

### **Bước 1: User (CUSTOMER) Đăng Ký Thành Viên HTX**

**Actor:** User (CUSTOMER)

**API Endpoint:**
```
POST /api/cooperative/member/join
```

**Request Body:**
```json
{
  "cooperativeId": 1,
  "role": "member"  // Optional: Vai trò trong HTX
}
```

**Validation:**
- ✅ User phải tồn tại
- ✅ HTX phải tồn tại
- ✅ User chưa đăng ký vào HTX này
- ✅ **KHÔNG yêu cầu** userType = SELLER (CUSTOMER có thể đăng ký)

**Kết Quả:**
- Tạo `CooperativeMember` với `status = PENDING`
- Lưu thời gian đăng ký (`appliedAt`)
- **KHÔNG tạo Store** (Store chỉ tạo khi đăng ký Seller)

**Response:**
```json
{
  "message": "Đơn đăng ký thành viên HTX đã được gửi. Vui lòng chờ HTX phê duyệt.",
  "status": 200,
  "data": {
    "id": 1,
    "cooperativeId": 1,
    "userId": 5,
    "role": "member",
    "status": "PENDING",
    "appliedAt": "2025-12-25T10:00:00"
  }
}
```

---

### **Bước 2: HTX Manager Xem Danh Sách Đơn Đăng Ký**

**Actor:** HTX Manager (User với role COOPERATIVE_MANAGER)

**API Endpoint:**
```
GET /api/cooperative/members?status=PENDING&page=0&size=10
```

**Query Parameters:**
- `status` (optional): PENDING, APPROVED, REJECTED
- `page` (default: 0): Số trang
- `size` (default: 10): Số items mỗi trang
- `sortBy` (default: "appliedAt"): Trường sắp xếp
- `sortDir` (default: "DESC"): ASC hoặc DESC

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "cooperativeId": 1,
      "cooperativeName": "HTX Nông Nghiệp Xanh",
      "sellerId": 5,
      "sellerName": "Nguyễn Văn A",
      "sellerEmail": "nguyenvana@example.com",
      "role": "member",
      "status": "PENDING",
      "appliedAt": "2025-12-25T10:00:00"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 10,
  "number": 0
}
```

---

### **Bước 3: HTX Manager Phê Duyệt Đơn Đăng Ký**

**Actor:** HTX Manager

**API Endpoint:**
```
POST /api/cooperative/members/{memberId}/approve
```

**Request Body:**
```json
{
  "status": "APPROVED"
}
```

**Validation:**
- ✅ Member phải tồn tại và có `status = PENDING`
- ✅ User phải là HTX Manager của HTX đó
- ✅ User tồn tại

**Quy Trình:**
1. Validate member và quyền
2. **KHÔNG tạo Store** (Store chỉ tạo khi user đăng ký Seller)
3. Update `CooperativeMember`:
   - `status = APPROVED`
   - `joinedAt = now()`
   - `reviewedAt = now()`
   - `reviewedBy = cooperativeManagerId`

**Response:**
```json
{
  "message": "Đơn đăng ký thành viên đã được phê duyệt. User đã trở thành thành viên HTX.",
  "status": 200,
  "data": {
    "id": 1,
    "cooperativeId": 1,
    "userId": 5,
    "status": "APPROVED",
    "joinedAt": "2025-12-25T11:00:00",
    "reviewedAt": "2025-12-25T11:00:00"
  }
}
```

---

### **Bước 4: Thành Viên HTX Đăng Ký Seller (Nếu Muốn Bán Hàng)**

**Actor:** Thành viên HTX (User đã được approve)

**Lưu Ý:** Bước này sử dụng chức năng đăng ký Seller hiện có (`SellerRegistration`), nhưng cần cập nhật logic để:
- Kiểm tra user đã là thành viên HTX chưa
- Nếu có, tạo Store với cả `seller_id` và `cooperative_id`
- Nếu không, tạo Store chỉ với `seller_id` (Seller độc lập)

**Quy Trình:**
1. User đăng ký Seller (qua `SellerRegistration`)
2. Admin approve
3. Hệ thống kiểm tra:
   - Nếu user là thành viên HTX → Tạo Store với `seller_id` và `cooperative_id`
   - Nếu không → Tạo Store chỉ với `seller_id`
4. Update `User.userType = SELLER`

**Store được tạo:**
```json
{
  "id": 10,
  "sellerId": 5,
  "cooperativeId": 1,  // Có cooperative_id vì user là thành viên HTX
  "storeName": "Store của Nguyễn Văn A",
  "storeCode": "STORE-5",
  "status": "ACTIVE"
}
```

---

### **Bước 5: HTX Manager Từ Chối Đơn Đăng Ký (Optional)**

**Actor:** HTX Manager

**API Endpoint:**
```
POST /api/cooperative/members/{memberId}/reject
```

**Request Body:**
```json
{
  "status": "REJECTED",
  "rejectionReason": "Không đủ điều kiện tham gia HTX"
}
```

**Validation:**
- ✅ Member phải tồn tại và có `status = PENDING`
- ✅ User phải là HTX Manager của HTX đó
- ✅ `rejectionReason` không được để trống

**Quy Trình:**
1. Validate member và quyền
2. Update `CooperativeMember`:
   - `status = REJECTED`
   - `rejectionReason = reason`
   - `reviewedAt = now()`
   - `reviewedBy = cooperativeManagerId`
3. **KHÔNG tạo Store** (vì bị reject)

**Response:**
```json
{
  "message": "Đơn đăng ký thành viên đã bị từ chối",
  "status": 200,
  "data": {
    "id": 1,
    "status": "REJECTED",
    "rejectionReason": "Không đủ điều kiện tham gia HTX",
    "reviewedAt": "2025-12-25T11:00:00"
  }
}
```

---

## 📱 API Endpoints Tổng Hợp

### **Cho User (CooperativeMemberController)**

| Method | Endpoint | Mô Tả | Auth |
|--------|----------|-------|------|
| POST | `/api/cooperative/member/join` | Đăng ký thành viên HTX | Authenticated |
| GET | `/api/cooperative/member/my-cooperatives` | Lấy HTX đã tham gia (APPROVED) | Authenticated |
| GET | `/api/cooperative/member/my-applications` | Lấy đơn đang chờ duyệt (PENDING) | Authenticated |

### **Cho HTX Manager (CooperativeController)**

| Method | Endpoint | Mô Tả | Auth |
|--------|----------|-------|------|
| GET | `/api/cooperative/members` | Lấy danh sách thành viên | COOPERATIVE_MANAGER |
| POST | `/api/cooperative/members/{memberId}/approve` | Phê duyệt thành viên | COOPERATIVE_MANAGER |
| POST | `/api/cooperative/members/{memberId}/reject` | Từ chối thành viên | COOPERATIVE_MANAGER |

---

## 🔐 Bảo Mật

### **Security Rules (SecurityConfig)**

```java
// User endpoints - Authenticated users
.requestMatchers("/api/cooperative/member/**").authenticated()

// HTX Manager endpoints - COOPERATIVE_MANAGER role
.requestMatchers("/api/cooperative/members/**")
    .hasAnyRole("COOPERATIVE_MANAGER", "SUPER_ADMIN")
```

### **Validation trong Service**

1. **joinCooperative():**
   - Kiểm tra user tồn tại
   - Kiểm tra HTX tồn tại
   - Kiểm tra user chưa đăng ký vào HTX này
   - **KHÔNG yêu cầu** userType = SELLER

2. **approveMember():**
   - Kiểm tra member tồn tại và status = PENDING
   - Kiểm tra user là HTX Manager của HTX đó
   - **KHÔNG tạo Store** (Store chỉ tạo khi đăng ký Seller)

3. **rejectMember():**
   - Kiểm tra member tồn tại và status = PENDING
   - Kiểm tra user là HTX Manager của HTX đó
   - Kiểm tra rejectionReason không rỗng

---

## 📊 Database Schema

### **Bảng: cooperative_members**

```sql
CREATE TABLE cooperative_members (
    id BIGSERIAL PRIMARY KEY,
    cooperative_id BIGINT NOT NULL REFERENCES cooperatives(id),
    user_id BIGINT NOT NULL REFERENCES users(id),  -- Đổi từ seller_id thành user_id
    role VARCHAR(50),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    rejection_reason TEXT,
    applied_at TIMESTAMP NOT NULL DEFAULT NOW(),
    joined_at TIMESTAMP,
    reviewed_at TIMESTAMP,
    reviewed_by BIGINT REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    
    UNIQUE(cooperative_id, user_id)  -- Đổi từ seller_id thành user_id
);

CREATE INDEX idx_cooperative_members_cooperative ON cooperative_members(cooperative_id);
CREATE INDEX idx_cooperative_members_user ON cooperative_members(user_id);  -- Đổi từ seller thành user
CREATE INDEX idx_cooperative_members_status ON cooperative_members(status);
```

### **Bảng: stores (đã có sẵn)**

```sql
-- Store được tạo khi thành viên HTX đăng ký Seller
-- Có cả seller_id và cooperative_id
stores {
    seller_id: 5,
    cooperative_id: 1,  -- Có cooperative_id vì user là thành viên HTX
    store_name: "Store của Nguyễn Văn A",
    ...
}
```

---

## 🎯 Use Cases

### **Use Case 1: User Đăng Ký Thành Viên HTX**

**Actor:** User (CUSTOMER)

**Preconditions:**
- User đã đăng ký tài khoản với userType = CUSTOMER
- HTX đã được approve và tồn tại

**Main Flow:**
1. User gọi API `POST /api/cooperative/member/join`
2. Hệ thống tạo `CooperativeMember` với status PENDING
3. User nhận thông báo "Đơn đăng ký đã được gửi"

**Postconditions:**
- `CooperativeMember` được tạo với status PENDING
- User có thể xem đơn đăng ký qua `GET /api/cooperative/member/my-applications`
- **KHÔNG tạo Store** (chỉ tạo khi đăng ký Seller)

---

### **Use Case 2: HTX Manager Phê Duyệt Thành Viên**

**Actor:** HTX Manager

**Preconditions:**
- Có đơn đăng ký với status PENDING
- User là HTX Manager của HTX đó

**Main Flow:**
1. HTX Manager xem danh sách đơn đăng ký: `GET /api/cooperative/members?status=PENDING`
2. HTX Manager approve: `POST /api/cooperative/members/{memberId}/approve`
3. Hệ thống:
   - Update `CooperativeMember` status = APPROVED
   - **KHÔNG tạo Store** (Store chỉ tạo khi đăng ký Seller)
4. User nhận thông báo (nếu có notification system)

**Postconditions:**
- `CooperativeMember` status = APPROVED
- User trở thành thành viên HTX
- User có thể xem HTX đã tham gia qua `GET /api/cooperative/member/my-cooperatives`
- **KHÔNG có Store** (chỉ có khi đăng ký Seller)

---

### **Use Case 3: Thành Viên HTX Đăng Ký Seller**

**Actor:** Thành viên HTX (User đã được approve)

**Preconditions:**
- User đã là thành viên HTX (CooperativeMember status = APPROVED)
- User chưa đăng ký Seller

**Main Flow:**
1. User đăng ký Seller (qua `SellerRegistration`)
2. Admin approve
3. Hệ thống kiểm tra:
   - User có là thành viên HTX không?
   - Nếu có → Tạo Store với `seller_id` và `cooperative_id`
   - Nếu không → Tạo Store chỉ với `seller_id`
4. Update `User.userType = SELLER`

**Postconditions:**
- User trở thành SELLER
- Store được tạo với cả `seller_id` và `cooperative_id` (nếu là thành viên HTX)
- User có thể bán hàng

---

## 🔍 Ví Dụ Request/Response

### **1. User Đăng Ký Thành Viên HTX**

**Request:**
```bash
POST /api/cooperative/member/join
Authorization: Bearer {user_token}
Content-Type: application/json

{
  "cooperativeId": 1,
  "role": "member"
}
```

**Response (200 OK):**
```json
{
  "message": "Đơn đăng ký thành viên HTX đã được gửi. Vui lòng chờ HTX phê duyệt.",
  "status": 200,
  "data": {
    "id": 1,
    "cooperativeId": 1,
    "userId": 5,
    "role": "member",
    "status": "PENDING",
    "appliedAt": "2025-12-25T10:00:00",
    "joinedAt": null,
    "reviewedAt": null
  }
}
```

---

### **2. HTX Manager Xem Danh Sách Đơn Đăng Ký**

**Request:**
```bash
GET /api/cooperative/members?status=PENDING&page=0&size=10
Authorization: Bearer {cooperative_manager_token}
```

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "cooperativeId": 1,
      "cooperativeName": "HTX Nông Nghiệp Xanh",
      "sellerId": 5,
      "sellerName": "Nguyễn Văn A",
      "sellerEmail": "nguyenvana@example.com",
      "role": "member",
      "status": "PENDING",
      "appliedAt": "2025-12-25T10:00:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalElements": 1,
  "totalPages": 1
}
```

---

### **3. HTX Manager Phê Duyệt**

**Request:**
```bash
POST /api/cooperative/members/1/approve
Authorization: Bearer {cooperative_manager_token}
Content-Type: application/json

{
  "status": "APPROVED"
}
```

**Response (200 OK):**
```json
{
  "message": "Đơn đăng ký thành viên đã được phê duyệt. User đã trở thành thành viên HTX.",
  "status": 200,
  "data": {
    "id": 1,
    "cooperativeId": 1,
    "userId": 5,
    "role": "member",
    "status": "APPROVED",
    "appliedAt": "2025-12-25T10:00:00",
    "joinedAt": "2025-12-25T11:00:00",
    "reviewedAt": "2025-12-25T11:00:00",
    "reviewedBy": 2
  }
}
```

**Lưu ý:** **KHÔNG tạo Store** ở bước này. Store chỉ được tạo khi user đăng ký Seller.

---

## ⚠️ Lưu Ý Quan Trọng

### **1. Luồng Đăng Ký 2 Bước**

```
Bước 1: CUSTOMER → Đăng ký thành viên HTX → APPROVED (KHÔNG tạo Store)
Bước 2: Thành viên HTX → Đăng ký Seller → APPROVED (Tạo Store với cả seller_id và cooperative_id)
```

### **2. Store Chỉ Được Tạo Khi Đăng Ký Seller**

- Khi approve thành viên HTX: **KHÔNG tạo Store**
- Khi approve Seller (thành viên HTX): **Tạo Store** với cả `seller_id` và `cooperative_id`
- Khi approve Seller (không phải thành viên): **Tạo Store** chỉ với `seller_id`

### **3. Validation Quyền**

- Chỉ HTX Manager của HTX đó mới có thể approve/reject members
- User chỉ có thể xem đơn đăng ký của chính mình

### **4. Status Flow**

```
PENDING → APPROVED (trở thành thành viên, KHÔNG tạo Store)
PENDING → REJECTED (không trở thành thành viên)
```

### **5. Entity Thay Đổi**

- `CooperativeMember.sellerId` → Đổi thành `userId` (vì có thể là CUSTOMER)
- Repository methods: `findBySellerId` → `findByUserId`

---

## 🚀 Các Bước Tiếp Theo (Future Enhancements)

1. **Cập Nhật SellerRegistration Logic:**
   - Khi approve Seller, kiểm tra user có là thành viên HTX không
   - Nếu có → Tạo Store với cả `seller_id` và `cooperative_id`
   - Nếu không → Tạo Store chỉ với `seller_id`

2. **Notification System:**
   - Gửi email/notification cho User khi được approve/reject
   - Gửi email cho HTX Manager khi có đơn đăng ký mới

3. **Bulk Operations:**
   - HTX Manager có thể approve/reject nhiều members cùng lúc

4. **Member Roles:**
   - Phân quyền chi tiết cho các role khác nhau (member, manager, etc.)

5. **Statistics:**
   - Thống kê số lượng members của HTX
   - Thống kê số lượng HTX mà User đã tham gia

---

## 📝 Tóm Tắt

Chức năng đăng ký thành viên HTX cho phép:
- ✅ User (CUSTOMER) đăng ký thành viên HTX
- ✅ HTX Manager quản lý danh sách thành viên
- ✅ Thành viên HTX có thể đăng ký Seller để bán hàng
- ✅ Store được tạo khi đăng ký Seller (không phải khi approve thành viên)
- ✅ Validation đầy đủ và bảo mật
- ✅ Hỗ trợ pagination và filtering

**Luồng chính:**
```
CUSTOMER đăng ký thành viên → PENDING → HTX Manager approve → APPROVED (thành viên)
Thành viên đăng ký Seller → PENDING → Admin approve → APPROVED + Tạo Store (với cả seller_id và cooperative_id)
```


