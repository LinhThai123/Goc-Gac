# Giải thích về Store Category

## Mục lục
1. [Store Category là gì?](#store-category-là-gì)
2. [Sự khác biệt giữa Category và Store Category](#sự-khác-biệt-giữa-category-và-store-category)
3. [Tại sao cần có Store Category?](#tại-sao-cần-có-store-category)
4. [Mục đích sử dụng](#mục-đích-sử-dụng)
5. [Ví dụ thực tế](#ví-dụ-thực-tế)
6. [Cách sử dụng trong hệ thống](#cách-sử-dụng-trong-hệ-thống)

---

## Store Category là gì?

**Store Category** (Danh mục của cửa hàng) là hệ thống phân loại sản phẩm **riêng của từng store/cửa hàng**, cho phép mỗi store tự tạo và quản lý danh mục sản phẩm của riêng mình.

### Đặc điểm:
- ✅ **Riêng biệt cho từng store**: Mỗi store có thể tạo danh mục riêng của mình
- ✅ **Tự quản lý**: Store owner có thể tự tạo, sửa, xóa danh mục
- ✅ **Linh hoạt**: Không bị ràng buộc bởi danh mục chung của hệ thống
- ✅ **Tùy chỉnh**: Có thể đặt tên danh mục theo cách riêng của store

---

## Sự khác biệt giữa Category và Store Category

### 1. **Category (Danh mục chung)**

| Đặc điểm | Mô tả |
|----------|-------|
| **Phạm vi** | Toàn hệ thống (global) |
| **Quản lý** | Admin/System quản lý |
| **Mục đích** | Phân loại sản phẩm chung cho tất cả stores |
| **Ví dụ** | "Điện tử", "Thời trang", "Thực phẩm" |
| **Dùng cho** | Tìm kiếm, filter, navigation chung |
| **Bắt buộc** | Không (optional) |

**Ví dụ Category:**
```
- Điện tử
  - Điện thoại
  - Laptop
  - Máy tính bảng
- Thời trang
  - Quần áo nam
  - Quần áo nữ
  - Phụ kiện
```

### 2. **Store Category (Danh mục của store)**

| Đặc điểm | Mô tả |
|----------|-------|
| **Phạm vi** | Riêng từng store |
| **Quản lý** | Store owner quản lý |
| **Mục đích** | Phân loại sản phẩm theo cách riêng của store |
| **Ví dụ** | "Sản phẩm bán chạy", "Hàng mới về", "Sale 50%" |
| **Dùng cho** | Hiển thị trong store, quản lý nội bộ |
| **Bắt buộc** | Không (optional) |

**Ví dụ Store Category:**
```
Store A:
- Hàng mới về
- Bán chạy nhất
- Sale cuối tuần
- Combo tiết kiệm

Store B:
- Sản phẩm OCOP
- Đặc sản địa phương
- Quà tặng
- Hàng giảm giá
```

---

## Tại sao cần có Store Category?

### 1. **Tự chủ trong quản lý**

Mỗi store có thể:
- Tạo danh mục phù hợp với đặc thù kinh doanh
- Đặt tên danh mục theo cách riêng
- Tổ chức sản phẩm theo logic riêng

**Ví dụ:**
- Store bán quần áo có thể tạo: "Hàng mới", "Sale", "Outlet"
- Store bán thực phẩm có thể tạo: "Đặc sản", "Hữu cơ", "Nhập khẩu"

### 2. **Linh hoạt trong marketing**

Store có thể:
- Tạo danh mục theo chiến dịch marketing
- Tổ chức sản phẩm theo chương trình khuyến mãi
- Phân loại theo mùa, sự kiện

**Ví dụ:**
- "Black Friday 2024"
- "Tết Nguyên Đán"
- "Back to School"

### 3. **Không phụ thuộc vào hệ thống**

- Không cần chờ admin tạo category mới
- Không bị giới hạn bởi category chung
- Có thể tạo category nhanh chóng khi cần

### 4. **Tối ưu trải nghiệm người dùng**

- Khách hàng xem store có thể thấy danh mục phù hợp với store đó
- Dễ dàng tìm sản phẩm trong store
- Hiển thị sản phẩm theo cách store muốn

---

## Mục đích sử dụng

### 1. **Phân loại sản phẩm trong store**

Store có thể tổ chức sản phẩm theo:
- **Mức độ phổ biến**: "Bán chạy", "Mới về", "Hot"
- **Giá cả**: "Dưới 100k", "100k-500k", "Trên 500k"
- **Chương trình**: "Sale", "Combo", "Flash sale"
- **Đặc thù sản phẩm**: "OCOP", "Hữu cơ", "Nhập khẩu"

### 2. **Hiển thị trong store front**

Khi khách hàng vào store, có thể:
- Xem danh mục riêng của store
- Lọc sản phẩm theo danh mục store
- Điều hướng dễ dàng trong store

### 3. **Quản lý nội bộ**

Store owner có thể:
- Tổ chức sản phẩm để quản lý dễ dàng
- Tạo báo cáo theo danh mục riêng
- Phân tích hiệu quả từng danh mục

### 4. **Marketing và Promotion**

- Tạo danh mục cho từng chiến dịch
- Tổ chức sản phẩm theo chương trình khuyến mãi
- Hiển thị sản phẩm nổi bật

---

## Ví dụ thực tế

### Scenario 1: Store bán quần áo

**Category (chung):**
- Thời trang
  - Quần áo nam
  - Quần áo nữ

**Store Category (riêng của store):**
- Hàng mới về tuần này
- Sale 50%
- Combo 2 sản phẩm
- Áo thun bán chạy
- Quần jean outlet

**Sản phẩm:**
- `categoryId`: 2 (Quần áo nam) - để tìm kiếm chung
- `storeCategoryId`: 5 (Áo thun bán chạy) - để hiển thị trong store

### Scenario 2: Store bán đặc sản địa phương

**Category (chung):**
- Thực phẩm
  - Đặc sản

**Store Category (riêng của store):**
- Đặc sản Hà Giang
- Đặc sản Sapa
- Quà tặng
- Hàng OCOP
- Sản phẩm mùa

**Sản phẩm:**
- `categoryId`: 3 (Đặc sản) - để tìm kiếm chung
- `storeCategoryId`: 8 (Hàng OCOP) - để highlight trong store

### Scenario 3: Store bán khóa học online

**Category (chung):**
- Giáo dục
  - Khóa học online

**Store Category (riêng của store):**
- Khóa học mới
- Khóa học bán chạy
- Combo khóa học
- Khóa học miễn phí
- Khóa học nâng cao

**Sản phẩm:**
- `categoryId`: 4 (Khóa học online) - để tìm kiếm chung
- `storeCategoryId`: 12 (Combo khóa học) - để marketing

---

## Cách sử dụng trong hệ thống

### 1. **Trong Product Entity**

Mỗi sản phẩm có thể có:
- `categoryId`: Danh mục chung (optional)
- `storeCategoryId`: Danh mục của store (optional)

```java
Product product = new Product();
product.setCategoryId(1L);           // Category chung: "Điện tử"
product.setStoreCategoryId(5L);     // Store category: "Hàng mới về"
```

### 2. **Khi tạo sản phẩm**

```json
{
  "productName": "Áo thun nam",
  "categoryId": 2,              // Category chung: "Quần áo nam"
  "storeCategoryId": 5,         // Store category: "Áo thun bán chạy"
  "skus": [...]
}
```

### 3. **Validation**

- `categoryId`: Có thể null, nếu có thì phải tồn tại trong `categories`
- `storeCategoryId`: Có thể null, nếu có thì phải:
  - Tồn tại trong `store_categories`
  - Thuộc về store của user (được validate trong `ProductService`)

### 4. **Query sản phẩm**

**Theo Category (chung):**
```sql
SELECT * FROM products WHERE category_id = 2;
```

**Theo Store Category:**
```sql
SELECT * FROM products WHERE store_category_id = 5;
```

**Cả hai:**
```sql
SELECT * FROM products 
WHERE category_id = 2 
  AND store_category_id = 5;
```

---

## So sánh trực quan

```
┌─────────────────────────────────────────────────────────┐
│                    HỆ THỐNG                              │
│                                                          │
│  ┌──────────────────────────────────────────────────┐   │
│  │  CATEGORY (Chung - Toàn hệ thống)                │   │
│  │  - Điện tử                                       │   │
│  │  - Thời trang                                    │   │
│  │  - Thực phẩm                                     │   │
│  │  - Giáo dục                                      │   │
│  └──────────────────────────────────────────────────┘   │
│                                                          │
│  ┌──────────────────────────────────────────────────┐   │
│  │  STORE A                                         │   │
│  │  ┌──────────────────────────────────────────┐   │   │
│  │  │  STORE CATEGORY (Riêng Store A)          │   │   │
│  │  │  - Hàng mới về                            │   │   │
│  │  │  - Sale 50%                               │   │   │
│  │  │  - Combo tiết kiệm                        │   │   │
│  │  └──────────────────────────────────────────┘   │   │
│  │                                                   │   │
│  │  Products:                                       │   │
│  │  - Áo thun (categoryId=2, storeCategoryId=1)     │   │
│  │  - Quần jean (categoryId=2, storeCategoryId=2)   │   │
│  └──────────────────────────────────────────────────┘   │
│                                                          │
│  ┌──────────────────────────────────────────────────┐   │
│  │  STORE B                                         │   │
│  │  ┌──────────────────────────────────────────┐   │   │
│  │  │  STORE CATEGORY (Riêng Store B)          │   │   │
│  │  │  - Đặc sản OCOP                           │   │   │
│  │  │  - Quà tặng                               │   │   │
│  │  │  - Hàng giảm giá                         │   │   │
│  │  └──────────────────────────────────────────┘   │   │
│  │                                                   │   │
│  │  Products:                                       │   │
│  │  - Mật ong (categoryId=3, storeCategoryId=10)     │   │
│  │  - Chè shan tuyết (categoryId=3, storeCategoryId=10)│
│  └──────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
```

---

## Lợi ích

### 1. **Cho Store Owner**
- ✅ Tự chủ trong quản lý danh mục
- ✅ Linh hoạt trong marketing
- ✅ Tổ chức sản phẩm theo ý muốn
- ✅ Không phụ thuộc vào admin

### 2. **Cho Khách hàng**
- ✅ Dễ tìm sản phẩm trong store
- ✅ Xem danh mục phù hợp với store
- ✅ Trải nghiệm tốt hơn khi duyệt store

### 3. **Cho Hệ thống**
- ✅ Category chung: Tìm kiếm, filter toàn hệ thống
- ✅ Store Category: Tổ chức nội bộ store
- ✅ Kết hợp cả hai: Tối ưu trải nghiệm

---

## Kết luận

**Store Category** là một tính năng quan trọng cho phép:
- Mỗi store tự quản lý danh mục riêng
- Linh hoạt trong tổ chức sản phẩm
- Tối ưu trải nghiệm cho cả store owner và khách hàng
- Bổ sung cho Category chung, không thay thế

**Lưu ý:**
- `storeCategoryId` là **optional** - không bắt buộc
- Nếu có, phải thuộc về store của user
- Nếu không hợp lệ, sẽ được set `null` (không throw error)

---

**Last Updated**: 2024-01-XX  
**Version**: 1.0.0

