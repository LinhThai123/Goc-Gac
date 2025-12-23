# Phân Tích Entity Structure Cho HTX (Hợp Tác Xã)

## 📊 Kết Quả Kiểm Tra Entity Hiện Tại

### ✅ Entity Đã Có

1. **User Entity**
   - Có `UserType.COOPERATIVE_MANAGER` enum
   - Lưu thông tin user cơ bản
   - **Thiếu**: Thông tin chi tiết về HTX

2. **Store Entity**
   - Có các trường: `storeName`, `storeCode`, `description`, `contactPhone`, `contactEmail`, `address`
   - **Vấn đề**: 
     - Có `seller_id` (nullable = false) → Chỉ dành cho Seller
     - Thiếu nhiều thông tin cần cho HTX (thông tin pháp lý, người đại diện, địa chỉ chi tiết)
   - **Được sử dụng bởi**: StorePermission, StoreSubscription, StoreCategory, Conversation

3. **SellerRegistration Entity**
   - Lưu quá trình đăng ký seller
   - Có approval status (DRAFT, PENDING, APPROVED, REJECTED)
   - **Tương tự**: Cần entity tương tự cho HTX

### ❌ Entity Chưa Có

1. **Cooperative Entity** - Chưa có
2. **CooperativeRegistration Entity** - Chưa có

---

## 🎯 Đề Xuất: Cần Tạo 2 Entity Mới

### **Option 1: Tạo Entity Riêng (RECOMMENDED) ✅**

#### **1. CooperativeRegistration Entity**
**Mục đích**: Lưu quá trình đăng ký HTX (6 bước, tương tự SellerRegistration)

**Lý do cần tạo:**
- Quy trình đăng ký HTX phức tạp hơn Seller (6 bước vs 1 form)
- Cần lưu trữ nhiều thông tin tạm thời trong quá trình đăng ký
- Cần tracking approval status và rejection reason
- Tách biệt giữa "đang đăng ký" và "đã được phê duyệt"

**Cấu trúc:**
```java
@Entity
@Table(name = "cooperative_registrations")
public class CooperativeRegistration {
    // Tương tự SellerRegistration nhưng có nhiều trường hơn
    // Lưu tất cả 6 bước thông tin
    // Status: DRAFT, PENDING, APPROVED, REJECTED
}
```

#### **2. Cooperative Entity**
**Mục đích**: Lưu thông tin HTX sau khi được phê duyệt (tương tự như Store cho Seller)

**Lý do cần tạo:**
- Store entity đã gắn với `seller_id` (không thể dùng chung)
- HTX cần nhiều thông tin hơn Store (thông tin pháp lý, người đại diện, etc.)
- Tách biệt rõ ràng giữa Seller và Cooperative
- Dễ maintain và mở rộng sau này

**Cấu trúc:**
```java
@Entity
@Table(name = "cooperatives")
public class Cooperative {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId; // User với COOPERATIVE_MANAGER role
    
    @Column(name = "registration_id", nullable = false)
    private Long registrationId; // Reference đến CooperativeRegistration đã approve
    
    // Thông tin từ registration (copy sau khi approve)
    private String cooperativeName;
    private String slug;
    private String cooperativeCode;
    // ... tất cả thông tin từ 6 bước
    
    // Thông tin bổ sung (có thể update sau)
    private String logoUrl;
    private String bannerUrl;
    // ...
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
```

**Relationship:**
```
User (COOPERATIVE_MANAGER) 
  ↓ (1:1)
Cooperative
  ↓ (1:1)
CooperativeRegistration (APPROVED)
```

---

### **Option 2: Mở Rộng Store Entity (KHÔNG KHUYẾN KHÍCH) ❌**

**Ý tưởng**: Thêm `cooperative_id` vào Store, cho phép Store thuộc về cả Seller và Cooperative

**Vấn đề:**
1. **Breaking Change**: Store hiện tại có `seller_id` (nullable = false), cần migration phức tạp
2. **Data Integrity**: Khó đảm bảo Store chỉ thuộc về Seller HOẶC Cooperative (không cả hai)
3. **Thiếu Thông Tin**: Store entity thiếu nhiều trường cần cho HTX
4. **Phức Tạp**: Logic sẽ phức tạp hơn khi phải check `seller_id` hoặc `cooperative_id`
5. **Maintainability**: Khó maintain và mở rộng sau này

**Nếu vẫn muốn dùng:**
```java
// Cần thay đổi Store entity:
@Column(name = "seller_id")
private Long sellerId; // Nullable

@Column(name = "cooperative_id")
private Long cooperativeId; // Nullable

// Cần constraint: seller_id XOR cooperative_id (không thể cả hai)
// Cần migration để update existing data
```

**Kết luận**: **KHÔNG NÊN** vì quá phức tạp và rủi ro cao

---

## 📋 So Sánh: Seller vs Cooperative

| Aspect | Seller | Cooperative |
|--------|--------|-------------|
| **Registration Entity** | `SellerRegistration` | `CooperativeRegistration` (CẦN TẠO) |
| **Main Entity** | `Store` (với seller_id) | `Cooperative` (CẦN TẠO) |
| **User Type** | `SELLER` | `COOPERATIVE_MANAGER` |
| **Registration Steps** | 1 form đơn giản | 6 bước phức tạp |
| **Thông tin cần** | Cơ bản (business name, tax code) | Chi tiết (pháp lý, người đại diện, địa chỉ chi tiết) |
| **Approval Process** | Đơn giản | Phức tạp (cần kiểm tra kỹ) |

---

## 🏗️ Kiến Trúc Đề Xuất

### **1. Quy Trình Đăng Ký**

```
User (CUSTOMER/SELLER)
  ↓
Tạo CooperativeRegistration (DRAFT)
  ↓
Điền 6 bước thông tin
  ↓
Submit → PENDING
  ↓
Admin Review
  ↓
APPROVED → Tạo Cooperative entity
  ↓
User → COOPERATIVE_MANAGER
```

### **2. Entity Relationships**

```
User
  ├── (1:1) Cooperative (nếu là COOPERATIVE_MANAGER)
  │         └── (1:1) CooperativeRegistration (APPROVED)
  │
  └── (1:1) Store (nếu là SELLER)
            └── (1:1) SellerRegistration (APPROVED)
```

### **3. Store vs Cooperative**

**Store (cho Seller):**
- Đơn giản hơn
- Gắn với `seller_id`
- Dùng cho seller cá nhân hoặc doanh nghiệp nhỏ

**Cooperative (cho HTX):**
- Phức tạp hơn
- Gắn với `user_id` (COOPERATIVE_MANAGER)
- Dùng cho HTX (tổ chức lớn hơn)
- Có thông tin pháp lý đầy đủ

---

## ✅ Kết Luận và Khuyến Nghị

### **CẦN TẠO 2 Entity Mới:**

1. ✅ **CooperativeRegistration Entity**
   - Lưu quá trình đăng ký HTX (6 bước)
   - Tương tự `SellerRegistration`
   - Status: DRAFT, PENDING, APPROVED, REJECTED

2. ✅ **Cooperative Entity**
   - Lưu thông tin HTX sau khi được phê duyệt
   - Tương tự `Store` nhưng riêng biệt
   - Reference đến `CooperativeRegistration` đã approve
   - Chứa đầy đủ thông tin từ 6 bước + thông tin bổ sung

### **KHÔNG NÊN:**
- ❌ Dùng chung Store entity (quá phức tạp, breaking change)
- ❌ Chỉ dùng User entity (thiếu quá nhiều thông tin)

### **Lợi Ích:**
- ✅ Tách biệt rõ ràng giữa Seller và Cooperative
- ✅ Dễ maintain và mở rộng
- ✅ Không breaking change với code hiện tại
- ✅ Data integrity tốt hơn
- ✅ Có thể reuse logic từ SellerRegistration

---

## 📝 Next Steps

1. **Tạo CooperativeRegistration Entity** (ưu tiên)
   - Copy structure từ SellerRegistration
   - Thêm các trường cho 6 bước đăng ký
   - Thêm enums: CooperativeType, CooperativeScale, BusinessScale

2. **Tạo Cooperative Entity**
   - Tham khảo Store entity
   - Thêm các trường từ CooperativeRegistration
   - Thêm relationship với User và CooperativeRegistration

3. **Tạo Repository, Service, Controller**
   - Tương tự SellerRegistration flow
   - Implement 6 bước đăng ký
   - Implement approval process

4. **Migration Database**
   - Tạo bảng `cooperative_registrations`
   - Tạo bảng `cooperatives`
   - Tạo indexes và constraints

---

## 🔗 Reference Entities

### SellerRegistration (Tham khảo)
```java
@Entity
@Table(name = "seller_registrations")
public class SellerRegistration {
    private Long id;
    private Long userId;
    private BusinessType businessType;
    private String businessName;
    private String taxCode;
    private ApprovalStatus status;
    // ...
}
```

### Store (Tham khảo)
```java
@Entity
@Table(name = "stores")
public class Store {
    private Long id;
    private Long sellerId; // ← Chỉ cho Seller
    private String storeName;
    private String storeCode;
    // ...
}
```

---

## 💡 Lưu Ý Quan Trọng

1. **CooperativeRegistration** là **tạm thời** (chỉ lưu trong quá trình đăng ký)
2. **Cooperative** là **vĩnh viễn** (lưu thông tin HTX sau khi approve)
3. Sau khi approve, có thể **copy** thông tin từ `CooperativeRegistration` → `Cooperative`
4. Có thể **xóa** `CooperativeRegistration` sau khi approve (hoặc giữ lại để audit)
5. `Cooperative` entity sẽ được dùng trong các chức năng khác (quản lý sản phẩm, đơn hàng, etc.)

