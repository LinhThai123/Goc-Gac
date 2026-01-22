# Product API - Test Cases

## Mục lục
1. [Test Cases - Create Product](#test-cases---create-product)
2. [Test Cases - Get Products](#test-cases---get-products)
3. [Test Cases - Update Product](#test-cases---update-product)
4. [Test Cases - Delete Product](#test-cases---delete-product)
5. [Test Cases - Public Endpoints](#test-cases---public-endpoints)
6. [Test Cases - Search Endpoints](#test-cases---search-endpoints)

---

## Test Cases - Create Product

### TC-PROD-001: Tạo sản phẩm vật lý với 1 SKU (Success)

**Endpoint**: `POST /api/products`

**Headers**:
```
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body**:
```json
{
  "productName": "Áo thun nam cổ tròn",
  "productCode": "PROD-001",
  "slug": "ao-thun-nam-co-tron",
  "description": "Áo thun nam chất liệu cotton 100%, thoáng mát, dễ giặt",
  "shortDescription": "Áo thun nam cotton 100%",
  "productType": "PHYSICAL",
  "categoryId": 1,
  "storeCategoryId": 1,
  "isCombo": false,
  "ocopCertified": false,
  "hasOriginTracking": false,
  "skus": [
    {
      "sku": "SKU-001",
      "variantName": "Áo thun - Size M",
      "size": "M",
      "color": "Trắng",
      "material": "Cotton",
      "price": 150000,
      "comparePrice": 200000,
      "costPrice": 100000,
      "stockQuantity": 100,
      "imageUrl": "https://example.com/images/ao-thun-m-trang.jpg",
      "weightKg": 0.2,
      "lengthCm": 70,
      "widthCm": 50,
      "heightCm": 2,
      "status": "active",
      "displayOrder": 1
    }
  ]
}
```

**Expected Response**: `201 Created`

**Assertions**:
- Status code = 201
- Response có `id` không null
- `productCode` = "PROD-001"
- `hasVariants` = false (vì chỉ có 1 SKU)
- Có 1 SKU trong response
- SKU có `sku` = "SKU-001"

---

### TC-PROD-002: Tạo sản phẩm vật lý với nhiều SKU (Success)

**Request Body**:
```json
{
  "productName": "Áo thun nam cổ tròn",
  "productType": "PHYSICAL",
  "categoryId": 1,
  "skus": [
    {
      "sku": "SKU-002-M",
      "variantName": "Áo thun - Size M - Trắng",
      "size": "M",
      "color": "Trắng",
      "price": 150000,
      "stockQuantity": 50,
      "status": "active"
    },
    {
      "sku": "SKU-002-L",
      "variantName": "Áo thun - Size L - Trắng",
      "size": "L",
      "color": "Trắng",
      "price": 150000,
      "stockQuantity": 50,
      "status": "active"
    },
    {
      "sku": "SKU-002-M-RED",
      "variantName": "Áo thun - Size M - Đỏ",
      "size": "M",
      "color": "Đỏ",
      "price": 150000,
      "stockQuantity": 30,
      "status": "active"
    }
  ]
}
```

**Expected Response**: `201 Created`

**Assertions**:
- Status code = 201
- `hasVariants` = true (vì có nhiều hơn 1 SKU)
- Có 3 SKUs trong response

---

### TC-PROD-003: Tạo sản phẩm khóa học (DIGITAL)

**Request Body**:
```json
{
  "productName": "Khóa học lập trình Java cơ bản",
  "productType": "DIGITAL",
  "categoryId": 2,
  "description": "Khóa học lập trình Java từ cơ bản đến nâng cao",
  "shortDescription": "Khóa học Java cơ bản",
  "durationHours": 40,
  "accessPeriodDays": 365,
  "isUnlimitedAccess": false,
  "deliveryMethod": "online_access",
  "skus": [
    {
      "sku": "COURSE-JAVA-001",
      "variantName": "Khóa học Java - Gói cơ bản",
      "price": 2000000,
      "comparePrice": 3000000,
      "stockQuantity": 999,
      "status": "active"
    }
  ]
}
```

**Expected Response**: `201 Created`

**Assertions**:
- Status code = 201
- `productType` = "DIGITAL"
- `durationHours` = 40
- `accessPeriodDays` = 365

---

### TC-PROD-004: Tạo sản phẩm OCOP certified

**Request Body**:
```json
{
  "productName": "Mật ong rừng nguyên chất",
  "productType": "PHYSICAL",
  "categoryId": 3,
  "ocopCertified": true,
  "ocopLevel": "3 sao",
  "isVerified": true,
  "hasOriginTracking": true,
  "skus": [
    {
      "sku": "HONEY-500G",
      "variantName": "Mật ong - 500g",
      "weightVariant": "500g",
      "price": 150000,
      "stockQuantity": 100,
      "status": "active"
    }
  ]
}
```

**Expected Response**: `201 Created`

**Assertions**:
- Status code = 201
- `ocopCertified` = true
- `ocopLevel` = "3 sao"
- `isVerified` = true
- `hasOriginTracking` = true

---

### TC-PROD-005: Tạo sản phẩm không có SKU (Error)

**Request Body**:
```json
{
  "productName": "Sản phẩm không có SKU",
  "productType": "PHYSICAL",
  "skus": []
}
```

**Expected Response**: `400 Bad Request`

**Assertions**:
- Status code = 400
- Error message chứa "ít nhất 1 SKU"

---

### TC-PROD-006: Tạo sản phẩm với SKU trùng (Error)

**Request Body**:
```json
{
  "productName": "Sản phẩm SKU trùng",
  "productType": "PHYSICAL",
  "skus": [
    {
      "sku": "SKU-001",
      "price": 100000,
      "stockQuantity": 10
    }
  ]
}
```

**Expected Response**: `400 Bad Request`

**Assertions**:
- Status code = 400
- Error message chứa "SKU đã tồn tại"

---

### TC-PROD-007: Tạo sản phẩm không có tên (Error)

**Request Body**:
```json
{
  "productType": "PHYSICAL",
  "skus": [
    {
      "sku": "SKU-007",
      "price": 100000,
      "stockQuantity": 10
    }
  ]
}
```

**Expected Response**: `400 Bad Request`

**Assertions**:
- Status code = 400
- Error message chứa "Tên sản phẩm không được để trống"

---

## Test Cases - Get Products

### TC-PROD-008: Lấy danh sách sản phẩm (Success)

**Endpoint**: `GET /api/products`

**Headers**:
```
Authorization: Bearer {token}
```

**Query Parameters**:
```
page=0
size=20
sortBy=createdAt
sortDir=DESC
includeSkus=true
```

**Expected Response**: `200 OK`

**Assertions**:
- Status code = 200
- Response có `content` array
- Response có `totalElements`
- Response có `totalPages`
- Mỗi product có `id`, `productName`, `status`

---

### TC-PROD-009: Lấy danh sách sản phẩm với filter theo status

**Query Parameters**:
```
status=ACTIVE
page=0
size=20
```

**Expected Response**: `200 OK`

**Assertions**:
- Status code = 200
- Tất cả products có `status` = "ACTIVE"

---

### TC-PROD-010: Lấy danh sách sản phẩm với filter theo productType

**Query Parameters**:
```
productType=PHYSICAL
page=0
size=20
```

**Expected Response**: `200 OK`

**Assertions**:
- Status code = 200
- Tất cả products có `productType` = "PHYSICAL"

---

### TC-PROD-011: Lấy danh sách sản phẩm với keyword search

**Query Parameters**:
```
keyword=áo thun
page=0
size=20
```

**Expected Response**: `200 OK`

**Assertions**:
- Status code = 200
- Tất cả products có `productName` chứa "áo thun" (case-insensitive)

---

### TC-PROD-012: Lấy thông tin chi tiết sản phẩm theo ID

**Endpoint**: `GET /api/products/{id}`

**Headers**:
```
Authorization: Bearer {token}
```

**Expected Response**: `200 OK`

**Assertions**:
- Status code = 200
- Response có `id` = {id}
- Response có `skus` array
- Mỗi SKU có đầy đủ thông tin

---

### TC-PROD-013: Lấy thông tin sản phẩm không tồn tại (Error)

**Endpoint**: `GET /api/products/99999`

**Expected Response**: `404 Not Found`

**Assertions**:
- Status code = 404
- Error message chứa "không tồn tại"

---

## Test Cases - Update Product

### TC-PROD-014: Cập nhật tên sản phẩm (Success)

**Endpoint**: `PUT /api/products/{id}`

**Headers**:
```
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body**:
```json
{
  "productName": "Áo thun nam cổ tròn - Updated"
}
```

**Expected Response**: `200 OK`

**Assertions**:
- Status code = 200
- `productName` = "Áo thun nam cổ tròn - Updated"
- Các field khác không thay đổi

---

### TC-PROD-015: Cập nhật giá SKU (Success)

**Request Body**:
```json
{
  "skus": [
    {
      "id": 1,
      "sku": "SKU-001",
      "price": 180000,
      "stockQuantity": 100
    }
  ]
}
```

**Expected Response**: `200 OK`

**Assertions**:
- Status code = 200
- SKU có `id` = 1 có `price` = 180000

---

### TC-PROD-016: Thêm SKU mới vào sản phẩm (Success)

**Request Body**:
```json
{
  "skus": [
    {
      "id": 1,
      "sku": "SKU-001",
      "price": 150000,
      "stockQuantity": 100
    },
    {
      "sku": "SKU-001-XL",
      "variantName": "Áo thun - Size XL",
      "size": "XL",
      "price": 150000,
      "stockQuantity": 50,
      "status": "active"
    }
  ]
}
```

**Expected Response**: `200 OK`

**Assertions**:
- Status code = 200
- Có 2 SKUs trong response
- SKU mới có `sku` = "SKU-001-XL"

---

### TC-PROD-017: Xóa SKU khỏi sản phẩm (Success)

**Request Body**:
```json
{
  "skus": [
    {
      "id": 1,
      "sku": "SKU-001",
      "price": 150000,
      "stockQuantity": 100
    }
  ]
}
```

**Giả sử sản phẩm có 2 SKUs (id: 1, 2), chỉ gửi SKU id=1**

**Expected Response**: `200 OK`

**Assertions**:
- Status code = 200
- Chỉ còn 1 SKU trong response

---

### TC-PROD-018: Xóa tất cả SKUs (Error)

**Request Body**:
```json
{
  "skus": []
}
```

**Expected Response**: `400 Bad Request`

**Assertions**:
- Status code = 400
- Error message chứa "ít nhất 1 SKU"

---

### TC-PROD-019: Cập nhật sản phẩm không thuộc về store của user (Error)

**Endpoint**: `PUT /api/products/{otherStoreProductId}`

**Expected Response**: `400 Bad Request`

**Assertions**:
- Status code = 400
- Error message chứa "không có quyền"

---

## Test Cases - Delete Product

### TC-PROD-020: Xóa mềm sản phẩm ACTIVE (Success)

**Endpoint**: `DELETE /api/products/{id}`

**Headers**:
```
Authorization: Bearer {token}
```

**Giả sử sản phẩm có `status` = "ACTIVE"**

**Expected Response**: `200 OK`

**Assertions**:
- Status code = 200
- Sản phẩm có `status` = "DELETED"
- Sản phẩm có `deletedAt` không null

---

### TC-PROD-021: Xóa cứng sản phẩm INACTIVE (Success)

**Giả sử sản phẩm có `status` = "INACTIVE"**

**Expected Response**: `200 OK`

**Assertions**:
- Status code = 200
- Sản phẩm bị xóa khỏi database (không tìm thấy khi query)

---

### TC-PROD-022: Xóa sản phẩm không tồn tại (Error)

**Endpoint**: `DELETE /api/products/99999`

**Expected Response**: `400 Bad Request`

**Assertions**:
- Status code = 400
- Error message chứa "không tồn tại"

---

## Test Cases - Public Endpoints

### TC-PROD-023: Lấy danh sách sản phẩm public (Success)

**Endpoint**: `GET /api/products/public`

**Headers**: Không cần authentication

**Query Parameters**:
```
page=0
size=20
```

**Expected Response**: `200 OK`

**Assertions**:
- Status code = 200
- Tất cả products có `approvalStatus` = "APPROVED"
- Tất cả products có `status` = "ACTIVE"
- Không có products có `status` = "DELETED"

---

### TC-PROD-024: Lấy sản phẩm public theo ID (Success)

**Endpoint**: `GET /api/products/public/{id}`

**Expected Response**: `200 OK`

**Assertions**:
- Status code = 200
- `approvalStatus` = "APPROVED"
- `status` = "ACTIVE"
- `viewCount` tăng lên 1

---

### TC-PROD-025: Lấy sản phẩm public theo slug (Success)

**Endpoint**: `GET /api/products/public/slug/{slug}`

**Expected Response**: `200 OK`

**Assertions**:
- Status code = 200
- `slug` = {slug}
- `viewCount` tăng lên 1

---

### TC-PROD-026: Lấy sản phẩm public chưa được duyệt (Error)

**Giả sử sản phẩm có `approvalStatus` = "DRAFT"**

**Expected Response**: `404 Not Found`

**Assertions**:
- Status code = 404

---

### TC-PROD-027: Lấy sản phẩm public theo storeId

**Endpoint**: `GET /api/products/public/store/{storeId}`

**Expected Response**: `200 OK`

**Assertions**:
- Status code = 200
- Tất cả products có `storeId` = {storeId}
- Tất cả products đã APPROVED và ACTIVE

---

### TC-PROD-028: Lấy sản phẩm public theo categoryId

**Endpoint**: `GET /api/products/public/category/{categoryId}`

**Expected Response**: `200 OK`

**Assertions**:
- Status code = 200
- Tất cả products có `categoryId` = {categoryId}

---

## Test Cases - Search Endpoints

### TC-PROD-029: Search sản phẩm với keyword (Success)

**Endpoint**: `POST /api/products/search`

**Headers**: Không cần authentication

**Request Body**:
```json
{
  "keyword": "áo thun",
  "page": 0,
  "size": 20,
  "highlight": true
}
```

**Expected Response**: `200 OK`

**Assertions**:
- Status code = 200
- Response có `products` array
- Response có `highlights` (nếu có keyword match)
- `searchTime` > 0

---

### TC-PROD-030: Search với filters (Success)

**Request Body**:
```json
{
  "keyword": "áo",
  "categoryId": 1,
  "minPrice": 100000,
  "maxPrice": 500000,
  "sizes": ["M", "L"],
  "colors": ["Trắng", "Đỏ"],
  "ocopCertified": true,
  "sortBy": "price_asc",
  "page": 0,
  "size": 20
}
```

**Expected Response**: `200 OK`

**Assertions**:
- Status code = 200
- Tất cả products có `categoryId` = 1
- Tất cả products có `minPrice` >= 100000
- Tất cả products có `maxPrice` <= 500000
- Products được sắp xếp theo price tăng dần

---

### TC-PROD-031: Search với GET method (Success)

**Endpoint**: `GET /api/products/search`

**Query Parameters**:
```
keyword=áo thun
categoryId=1
minPrice=100000
maxPrice=500000
sortBy=price_asc
page=0
size=20
```

**Expected Response**: `200 OK`

**Assertions**:
- Status code = 200
- Kết quả giống với POST method

---

### TC-PROD-032: Re-index tất cả sản phẩm (Success)

**Endpoint**: `POST /api/products/search/reindex`

**Headers**:
```
Authorization: Bearer {superAdminToken}
```

**Expected Response**: `200 OK`

**Assertions**:
- Status code = 200
- Message = "Re-index tất cả sản phẩm thành công"

---

### TC-PROD-033: Re-index không có quyền (Error)

**Headers**:
```
Authorization: Bearer {normalUserToken}
```

**Expected Response**: `403 Forbidden`

**Assertions**:
- Status code = 403

---

### TC-PROD-034: Index một sản phẩm cụ thể (Success)

**Endpoint**: `POST /api/products/search/index/{productId}`

**Headers**:
```
Authorization: Bearer {token}
```

**Expected Response**: `200 OK`

**Assertions**:
- Status code = 200
- Message = "Index sản phẩm thành công"
- Sản phẩm có thể tìm thấy trong Elasticsearch

---

## Test Data Setup

### Prerequisites

1. **User với role SELLER hoặc COOPERATIVE_MANAGER**
   - Cần có token để test protected endpoints
   - Cần có store để tạo sản phẩm

2. **Categories**
   - Category ID 1: Điện tử
   - Category ID 2: Giáo dục
   - Category ID 3: Thực phẩm

3. **Store**
   - Store ID 1: Store của user test

4. **Elasticsearch**
   - Elasticsearch phải đang chạy
   - Index "products" sẽ được tạo tự động

### Test Execution Order

1. **Setup**: Tạo user, store, categories
2. **Create Products**: TC-PROD-001 đến TC-PROD-007
3. **Get Products**: TC-PROD-008 đến TC-PROD-013
4. **Update Products**: TC-PROD-014 đến TC-PROD-019
5. **Delete Products**: TC-PROD-020 đến TC-PROD-022
6. **Public Endpoints**: TC-PROD-023 đến TC-PROD-028
7. **Search Endpoints**: TC-PROD-029 đến TC-PROD-034

---

## Expected Results Summary

| Test Case | Endpoint | Method | Expected Status | Authentication |
|-----------|----------|--------|----------------|----------------|
| TC-PROD-001 | /api/products | POST | 201 | Required |
| TC-PROD-002 | /api/products | POST | 201 | Required |
| TC-PROD-003 | /api/products | POST | 201 | Required |
| TC-PROD-004 | /api/products | POST | 201 | Required |
| TC-PROD-005 | /api/products | POST | 400 | Required |
| TC-PROD-006 | /api/products | POST | 400 | Required |
| TC-PROD-007 | /api/products | POST | 400 | Required |
| TC-PROD-008 | /api/products | GET | 200 | Required |
| TC-PROD-009 | /api/products | GET | 200 | Required |
| TC-PROD-010 | /api/products | GET | 200 | Required |
| TC-PROD-011 | /api/products | GET | 200 | Required |
| TC-PROD-012 | /api/products/{id} | GET | 200 | Required |
| TC-PROD-013 | /api/products/99999 | GET | 404 | Required |
| TC-PROD-014 | /api/products/{id} | PUT | 200 | Required |
| TC-PROD-015 | /api/products/{id} | PUT | 200 | Required |
| TC-PROD-016 | /api/products/{id} | PUT | 200 | Required |
| TC-PROD-017 | /api/products/{id} | PUT | 200 | Required |
| TC-PROD-018 | /api/products/{id} | PUT | 400 | Required |
| TC-PROD-019 | /api/products/{id} | PUT | 400 | Required |
| TC-PROD-020 | /api/products/{id} | DELETE | 200 | Required |
| TC-PROD-021 | /api/products/{id} | DELETE | 200 | Required |
| TC-PROD-022 | /api/products/99999 | DELETE | 400 | Required |
| TC-PROD-023 | /api/products/public | GET | 200 | Public |
| TC-PROD-024 | /api/products/public/{id} | GET | 200 | Public |
| TC-PROD-025 | /api/products/public/slug/{slug} | GET | 200 | Public |
| TC-PROD-026 | /api/products/public/{id} | GET | 404 | Public |
| TC-PROD-027 | /api/products/public/store/{storeId} | GET | 200 | Public |
| TC-PROD-028 | /api/products/public/category/{categoryId} | GET | 200 | Public |
| TC-PROD-029 | /api/products/search | POST | 200 | Public |
| TC-PROD-030 | /api/products/search | POST | 200 | Public |
| TC-PROD-031 | /api/products/search | GET | 200 | Public |
| TC-PROD-032 | /api/products/search/reindex | POST | 200 | SUPER_ADMIN |
| TC-PROD-033 | /api/products/search/reindex | POST | 403 | Normal User |
| TC-PROD-034 | /api/products/search/index/{id} | POST | 200 | Required |

---

**Last Updated**: 2024-01-XX  
**Version**: 1.0.0

