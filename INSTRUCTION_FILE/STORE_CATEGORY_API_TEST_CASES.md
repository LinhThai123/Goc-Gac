# Store Category API - Test Cases

## Mục lục
1. [Test Cases - Create Store Category](#test-cases---create-store-category)
2. [Test Cases - Get Store Categories](#test-cases---get-store-categories)
3. [Test Cases - Update Store Category](#test-cases---update-store-category)
4. [Test Cases - Delete Store Category](#test-cases---delete-store-category)

---

## Test Cases - Create Store Category

### TC-STORE-CAT-001: Tạo root store category (Success)

**Endpoint**: `POST /api/store-categories`

**Headers**:
```
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body**:
```json
{
  "categoryName": "Hàng mới về",
  "parentId": null,
  "displayOrder": 1,
  "isActive": true
}
```

**Expected Response**: `201 Created`

**Assertions**:
- Status code = 201
- Response có `id` không null
- `storeId` = storeId của user
- `parentId` = null
- `level` = 1
- `categoryName` = "Hàng mới về"
- `isActive` = true

---

### TC-STORE-CAT-002: Tạo child store category (Success)

**Request Body**:
```json
{
  "categoryName": "Áo thun bán chạy",
  "parentId": 1,
  "displayOrder": 1,
  "isActive": true
}
```

**Giả sử parent category có id = 1 và level = 1**

**Expected Response**: `201 Created`

**Assertions**:
- Status code = 201
- `parentId` = 1
- `level` = 2 (parent level + 1)
- `categoryName` = "Áo thun bán chạy"

---

### TC-STORE-CAT-003: Tạo store category với tên trống (Error)

**Request Body**:
```json
{
  "categoryName": "",
  "parentId": null,
  "displayOrder": 1
}
```

**Expected Response**: `400 Bad Request`

**Assertions**:
- Status code = 400
- Error message chứa "Tên danh mục không được để trống"

---

### TC-STORE-CAT-004: Tạo store category với parent không tồn tại (Error)

**Request Body**:
```json
{
  "categoryName": "Category con",
  "parentId": 99999,
  "displayOrder": 1
}
```

**Expected Response**: `400 Bad Request`

**Assertions**:
- Status code = 400
- Error message chứa "Parent category không tồn tại"

---

### TC-STORE-CAT-005: Tạo store category với parent thuộc store khác (Error)

**Request Body**:
```json
{
  "categoryName": "Category con",
  "parentId": 10,
  "displayOrder": 1
}
```

**Giả sử parentId = 10 thuộc về store khác**

**Expected Response**: `400 Bad Request`

**Assertions**:
- Status code = 400
- Error message chứa "không thuộc về store của bạn"

---

### TC-STORE-CAT-006: Tạo store category với circular reference (Error)

**Request Body**:
```json
{
  "categoryName": "Category A",
  "parentId": 2,
  "displayOrder": 1
}
```

**Giả sử categoryId = 2 có parentId = 1, và đang cố set parentId = 2 cho category mới**

**Expected Response**: `400 Bad Request`

**Assertions**:
- Status code = 400
- Error message chứa "circular reference"

---

## Test Cases - Get Store Categories

### TC-STORE-CAT-007: Lấy tất cả store categories của store (Success)

**Endpoint**: `GET /api/store-categories`

**Headers**:
```
Authorization: Bearer {token}
```

**Expected Response**: `200 OK`

**Assertions**:
- Status code = 200
- Response có array `data`
- Tất cả categories có `storeId` = storeId của user
- Response có thể bao gồm cả inactive categories

---

### TC-STORE-CAT-008: Lấy tất cả store categories active (Success)

**Endpoint**: `GET /api/store-categories/active`

**Expected Response**: `200 OK`

**Assertions**:
- Status code = 200
- Tất cả categories có `isActive` = true
- Tất cả categories có `storeId` = storeId của user

---

### TC-STORE-CAT-009: Lấy store category theo ID (Success)

**Endpoint**: `GET /api/store-categories/{id}`

**Expected Response**: `200 OK`

**Assertions**:
- Status code = 200
- Response có `id` = {id}
- `storeId` = storeId của user

---

### TC-STORE-CAT-010: Lấy store category không tồn tại (Error)

**Endpoint**: `GET /api/store-categories/99999`

**Expected Response**: `404 Not Found`

**Assertions**:
- Status code = 404
- Error message chứa "không tồn tại"

---

### TC-STORE-CAT-011: Lấy store category thuộc store khác (Error)

**Endpoint**: `GET /api/store-categories/{otherStoreCategoryId}`

**Expected Response**: `404 Not Found` hoặc `400 Bad Request`

**Assertions**:
- Status code = 404 hoặc 400
- Error message chứa "không thuộc về store của bạn"

---

### TC-STORE-CAT-012: Lấy root store categories (Success)

**Endpoint**: `GET /api/store-categories/roots`

**Expected Response**: `200 OK`

**Assertions**:
- Status code = 200
- Tất cả categories có `parentId` = null
- Tất cả categories có `level` = 1
- Tất cả categories có `storeId` = storeId của user

---

### TC-STORE-CAT-013: Lấy child categories (Success)

**Endpoint**: `GET /api/store-categories/{parentId}/children`

**Expected Response**: `200 OK`

**Assertions**:
- Status code = 200
- Tất cả categories có `parentId` = {parentId}
- Tất cả categories có `storeId` = storeId của user

---

## Test Cases - Update Store Category

### TC-STORE-CAT-014: Cập nhật tên store category (Success)

**Endpoint**: `PUT /api/store-categories/{id}`

**Headers**:
```
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body**:
```json
{
  "categoryName": "Hàng mới về - Updated"
}
```

**Expected Response**: `200 OK`

**Assertions**:
- Status code = 200
- `categoryName` = "Hàng mới về - Updated"
- Các field khác không thay đổi

---

### TC-STORE-CAT-015: Cập nhật parent category (Success)

**Request Body**:
```json
{
  "parentId": 2
}
```

**Expected Response**: `200 OK`

**Assertions**:
- Status code = 200
- `parentId` = 2
- `level` được tính lại dựa trên parent mới

---

### TC-STORE-CAT-016: Cập nhật display order (Success)

**Request Body**:
```json
{
  "displayOrder": 5
}
```

**Expected Response**: `200 OK`

**Assertions**:
- Status code = 200
- `displayOrder` = 5

---

### TC-STORE-CAT-017: Cập nhật isActive (Success)

**Request Body**:
```json
{
  "isActive": false
}
```

**Expected Response**: `200 OK`

**Assertions**:
- Status code = 200
- `isActive` = false

---

### TC-STORE-CAT-018: Cập nhật nhiều fields (Success)

**Request Body**:
```json
{
  "categoryName": "Hàng mới về - Premium",
  "displayOrder": 10,
  "isActive": true
}
```

**Expected Response**: `200 OK`

**Assertions**:
- Status code = 200
- Tất cả fields được cập nhật đúng

---

### TC-STORE-CAT-019: Cập nhật store category không tồn tại (Error)

**Endpoint**: `PUT /api/store-categories/99999`

**Expected Response**: `404 Not Found` hoặc `400 Bad Request`

**Assertions**:
- Status code = 404 hoặc 400
- Error message chứa "không tồn tại"

---

### TC-STORE-CAT-020: Cập nhật store category thuộc store khác (Error)

**Expected Response**: `400 Bad Request`

**Assertions**:
- Status code = 400
- Error message chứa "không thuộc về store của bạn"

---

### TC-STORE-CAT-021: Cập nhật parent thành chính nó (Error)

**Request Body**:
```json
{
  "parentId": {id}
}
```

**Expected Response**: `400 Bad Request`

**Assertions**:
- Status code = 400
- Error message chứa "không thể là parent của chính nó"

---

## Test Cases - Delete Store Category

### TC-STORE-CAT-022: Xóa store category không có con và không có sản phẩm (Success)

**Endpoint**: `DELETE /api/store-categories/{id}`

**Headers**:
```
Authorization: Bearer {token}
```

**Expected Response**: `200 OK`

**Assertions**:
- Status code = 200
- Category có `isActive` = false (soft delete)

---

### TC-STORE-CAT-023: Xóa store category có con active (Error)

**Giả sử category có child categories đang active**

**Expected Response**: `400 Bad Request`

**Assertions**:
- Status code = 400
- Error message chứa "còn category con đang active"

---

### TC-STORE-CAT-024: Xóa store category có sản phẩm đang sử dụng (Error)

**Giả sử có sản phẩm đang sử dụng storeCategoryId = {id}`

**Expected Response**: `400 Bad Request`

**Assertions**:
- Status code = 400
- Error message chứa "còn sản phẩm đang sử dụng"

---

### TC-STORE-CAT-025: Xóa store category không tồn tại (Error)

**Endpoint**: `DELETE /api/store-categories/99999`

**Expected Response**: `400 Bad Request`

**Assertions**:
- Status code = 400
- Error message chứa "không tồn tại"

---

### TC-STORE-CAT-026: Xóa store category thuộc store khác (Error)

**Expected Response**: `400 Bad Request`

**Assertions**:
- Status code = 400
- Error message chứa "không thuộc về store của bạn"

---

## Test Data Setup

### Prerequisites

1. **User với role SELLER hoặc COOPERATIVE_MANAGER**
   - Cần có token để test protected endpoints
   - Cần có store để tạo store categories

2. **Store**
   - Store ID 1: Store của user test

### Test Execution Order

1. **Setup**: Tạo user, store
2. **Create Store Categories**: TC-STORE-CAT-001 đến TC-STORE-CAT-006
3. **Get Store Categories**: TC-STORE-CAT-007 đến TC-STORE-CAT-013
4. **Update Store Categories**: TC-STORE-CAT-014 đến TC-STORE-CAT-021
5. **Delete Store Categories**: TC-STORE-CAT-022 đến TC-STORE-CAT-026

---

## Expected Results Summary

| Test Case | Endpoint | Method | Expected Status | Authentication |
|-----------|----------|--------|----------------|----------------|
| TC-STORE-CAT-001 | /api/store-categories | POST | 201 | Required |
| TC-STORE-CAT-002 | /api/store-categories | POST | 201 | Required |
| TC-STORE-CAT-003 | /api/store-categories | POST | 400 | Required |
| TC-STORE-CAT-004 | /api/store-categories | POST | 400 | Required |
| TC-STORE-CAT-005 | /api/store-categories | POST | 400 | Required |
| TC-STORE-CAT-006 | /api/store-categories | POST | 400 | Required |
| TC-STORE-CAT-007 | /api/store-categories | GET | 200 | Required |
| TC-STORE-CAT-008 | /api/store-categories/active | GET | 200 | Required |
| TC-STORE-CAT-009 | /api/store-categories/{id} | GET | 200 | Required |
| TC-STORE-CAT-010 | /api/store-categories/99999 | GET | 404 | Required |
| TC-STORE-CAT-011 | /api/store-categories/{otherId} | GET | 404/400 | Required |
| TC-STORE-CAT-012 | /api/store-categories/roots | GET | 200 | Required |
| TC-STORE-CAT-013 | /api/store-categories/{parentId}/children | GET | 200 | Required |
| TC-STORE-CAT-014 | /api/store-categories/{id} | PUT | 200 | Required |
| TC-STORE-CAT-015 | /api/store-categories/{id} | PUT | 200 | Required |
| TC-STORE-CAT-016 | /api/store-categories/{id} | PUT | 200 | Required |
| TC-STORE-CAT-017 | /api/store-categories/{id} | PUT | 200 | Required |
| TC-STORE-CAT-018 | /api/store-categories/{id} | PUT | 200 | Required |
| TC-STORE-CAT-019 | /api/store-categories/99999 | PUT | 404/400 | Required |
| TC-STORE-CAT-020 | /api/store-categories/{otherId} | PUT | 400 | Required |
| TC-STORE-CAT-021 | /api/store-categories/{id} | PUT | 400 | Required |
| TC-STORE-CAT-022 | /api/store-categories/{id} | DELETE | 200 | Required |
| TC-STORE-CAT-023 | /api/store-categories/{id} | DELETE | 400 | Required |
| TC-STORE-CAT-024 | /api/store-categories/{id} | DELETE | 400 | Required |
| TC-STORE-CAT-025 | /api/store-categories/99999 | DELETE | 400 | Required |
| TC-STORE-CAT-026 | /api/store-categories/{otherId} | DELETE | 400 | Required |

---

## Test Scenarios

### Scenario 1: Tạo cây category hoàn chỉnh

```
1. Tạo root category: "Hàng mới về" (id=1)
2. Tạo root category: "Sale" (id=2)
3. Tạo child category: "Áo thun bán chạy" (parentId=1, id=3)
4. Tạo child category: "Quần jean outlet" (parentId=2, id=4)
5. Tạo child category: "Áo thun size M" (parentId=3, id=5)
```

### Scenario 2: Cập nhật và xóa

```
1. Cập nhật tên category id=1: "Hàng mới về - Updated"
2. Cập nhật parent của category id=3: chuyển từ parentId=1 sang parentId=2
3. Xóa category id=5 (không có con, không có sản phẩm)
4. Thử xóa category id=3 (có con id=5) → Error
```

### Scenario 3: Validation

```
1. Tạo category với parent không tồn tại → Error
2. Tạo category với parent thuộc store khác → Error
3. Cập nhật parent thành chính nó → Error
4. Xóa category có sản phẩm đang dùng → Error
```

---

**Last Updated**: 2024-01-XX  
**Version**: 1.0.0

