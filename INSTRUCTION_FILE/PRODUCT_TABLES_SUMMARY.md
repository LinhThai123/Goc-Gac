# Tóm tắt các bảng liên quan đến Product

## Danh sách các bảng

### 1. **products** - Bảng sản phẩm chính
- **Mô tả**: Lưu thông tin chung về sản phẩm
- **Migration**: V3 (tạo), V8 (cập nhật), V9 (xóa cột không cần thiết)
- **Đặc điểm**: 
  - Mỗi Product bắt buộc có ít nhất 1 SKU
  - Không lưu giá, tồn kho, kích thước (đã chuyển sang ProductVariant)
  - Lưu thông tin chung: tên, mô tả, category, product_type, etc.

### 2. **product_variants** - Bảng SKU (Stock Keeping Unit)
- **Mô tả**: Lưu tất cả thông tin cụ thể của từng biến thể sản phẩm
- **Migration**: V3 (tạo), V8 (cập nhật đầy đủ)
- **Đặc điểm**:
  - Mỗi SKU có: giá, tồn kho, kích thước, trọng lượng
  - Các thuộc tính: size, color, material, weight_variant
  - SKU là unique và NOT NULL

### 3. **product_images** - Bảng hình ảnh sản phẩm
- **Mô tả**: Lưu các hình ảnh của sản phẩm
- **Migration**: V3
- **Đặc điểm**: Có thể có nhiều hình ảnh, có is_primary để đánh dấu ảnh chính

### 4. **product_attributes** - Bảng thuộc tính sản phẩm
- **Mô tả**: Lưu các thuộc tính chung của sản phẩm (không phải biến thể)
- **Migration**: V3
- **Ví dụ**: "Chất liệu: Cotton", "Xuất xứ: Việt Nam"

### 5. **product_combo_items** - Bảng sản phẩm trong combo
- **Mô tả**: Lưu các sản phẩm con trong combo
- **Migration**: V3
- **Đặc điểm**: Một combo_product_id có thể có nhiều product_id

### 6. **product_videos** - Bảng video sản phẩm
- **Mô tả**: Lưu các video của sản phẩm
- **Migration**: V3
- **Đặc điểm**: Có video_type (DIRECT, YOUTUBE, etc.)

### 7. **product_origin_tracking** - Bảng truy xuất nguồn gốc
- **Mô tả**: Lưu thông tin truy xuất nguồn gốc sản phẩm
- **Migration**: V3
- **Đặc điểm**: 1-1 với Product, có QR code

### 8. **product_reviews** - Bảng đánh giá sản phẩm
- **Mô tả**: Lưu đánh giá và rating của khách hàng
- **Migration**: V5
- **Đặc điểm**: Có rating (1-5), status (PENDING, APPROVED, REJECTED)

### 9. **product_recommendations** - Bảng gợi ý sản phẩm
- **Mô tả**: Lưu các sản phẩm được gợi ý liên quan
- **Migration**: V5
- **Đặc điểm**: Có recommendation_type và score

## Quan hệ giữa các bảng

```
products (1) ──< (N) product_variants (SKU)
products (1) ──< (N) product_images
products (1) ──< (N) product_attributes
products (1) ──< (N) product_videos
products (1) ──< (1) product_origin_tracking
products (1) ──< (N) product_reviews
products (1) ──< (N) product_recommendations (source)
products (1) ──< (N) product_recommendations (recommended)
products (1) ──< (N) product_combo_items (combo_product_id)
products (1) ──< (N) product_combo_items (product_id)
```

## Migration Files

1. **V3__Create_category_and_product_tables.sql**
   - Tạo các bảng: products, product_variants, product_images, product_attributes, product_combo_items, product_videos, product_origin_tracking

2. **V5__Create_remaining_tables.sql**
   - Tạo các bảng: product_reviews, product_recommendations

3. **V8__Update_product_and_add_sku_support.sql**
   - Thêm product_type vào products
   - Cập nhật product_variants với đầy đủ thông tin SKU

4. **V9__Remove_redundant_product_columns.sql**
   - Xóa các cột không cần thiết ở products (price, stock_quantity, kích thước)

5. **V10__Complete_product_tables_migration.sql**
   - Migration tổng hợp đảm bảo tất cả các bảng được tạo đầy đủ
   - Tạo indexes và constraints cần thiết

## Lưu ý quan trọng

1. **Mỗi Product bắt buộc có ít nhất 1 SKU**
   - Validation này nên được thực hiện ở application level
   - Khi tạo Product, phải tạo ít nhất 1 ProductVariant

2. **Giá và tồn kho được quản lý ở cấp SKU**
   - Product không lưu price, stock_quantity
   - Tất cả thông tin về giá, tồn kho, kích thước đều ở ProductVariant

3. **ProductType**
   - PHYSICAL: Sản phẩm vật lý
   - DIGITAL: Sản phẩm số (khóa học, ebook...)
   - SERVICE: Dịch vụ
   - VOUCHER: Voucher, coupon

4. **SKU Attributes**
   - size, color, material, weight_variant được lưu trực tiếp trong ProductVariant
   - Không cần bảng sku_attributes riêng (đã xóa)

