-- ==========================================
-- V8: Cập nhật Product và thêm hỗ trợ SKU
-- ==========================================

-- Thêm cột product_type vào bảng products
ALTER TABLE products 
ADD COLUMN IF NOT EXISTS product_type VARCHAR(50) NOT NULL DEFAULT 'PHYSICAL';

-- Tạo index cho product_type
CREATE INDEX IF NOT EXISTS idx_product_type ON products(product_type);

-- Lưu ý: Vì mỗi Product đều bắt buộc có ít nhất 1 SKU,
-- nên các thông tin về giá, tồn kho, kích thước, trọng lượng
-- sẽ được quản lý ở cấp SKU (ProductVariant)
-- Các cột price, stock_quantity, weight_kg, length_cm, width_cm, height_cm
-- ở Product sẽ được xóa trong migration V9 vì không cần thiết

-- Thêm các cột cho sản phẩm phi vật lý (khóa học, dịch vụ...)
ALTER TABLE products 
ADD COLUMN IF NOT EXISTS duration_hours INTEGER,
ADD COLUMN IF NOT EXISTS access_period_days INTEGER,
ADD COLUMN IF NOT EXISTS is_unlimited_access BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS delivery_method VARCHAR(50),
ADD COLUMN IF NOT EXISTS has_variants BOOLEAN NOT NULL DEFAULT FALSE;

-- Cập nhật bảng product_variants để hỗ trợ SKU đầy đủ
ALTER TABLE product_variants 
ADD COLUMN IF NOT EXISTS barcode VARCHAR(100),
ADD COLUMN IF NOT EXISTS compare_price NUMERIC(15, 2),
ADD COLUMN IF NOT EXISTS cost_price NUMERIC(15, 2),
ADD COLUMN IF NOT EXISTS reserved_quantity INTEGER NOT NULL DEFAULT 0,
ADD COLUMN IF NOT EXISTS available_quantity INTEGER NOT NULL DEFAULT 0,
ADD COLUMN IF NOT EXISTS weight_kg NUMERIC(10, 3),
ADD COLUMN IF NOT EXISTS length_cm NUMERIC(10, 2),
ADD COLUMN IF NOT EXISTS width_cm NUMERIC(10, 2),
ADD COLUMN IF NOT EXISTS height_cm NUMERIC(10, 2),
ADD COLUMN IF NOT EXISTS display_order INTEGER NOT NULL DEFAULT 0,
ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP,
-- Các thuộc tính biến thể phổ biến (lưu trực tiếp để query nhanh)
ADD COLUMN IF NOT EXISTS size VARCHAR(50),
ADD COLUMN IF NOT EXISTS color VARCHAR(50),
ADD COLUMN IF NOT EXISTS material VARCHAR(100),
ADD COLUMN IF NOT EXISTS weight_variant VARCHAR(50);

-- Cập nhật SKU để NOT NULL
ALTER TABLE product_variants 
ALTER COLUMN sku SET NOT NULL;

-- Cập nhật status để có độ dài phù hợp
ALTER TABLE product_variants 
ALTER COLUMN status TYPE VARCHAR(50);

-- Tạo indexes cho product_variants
CREATE INDEX IF NOT EXISTS idx_variant_product ON product_variants(product_id);
CREATE INDEX IF NOT EXISTS idx_variant_sku ON product_variants(sku);
CREATE INDEX IF NOT EXISTS idx_variant_status ON product_variants(status);
CREATE INDEX IF NOT EXISTS idx_variant_size ON product_variants(size);
CREATE INDEX IF NOT EXISTS idx_variant_color ON product_variants(color);

-- Cập nhật available_quantity cho các SKU hiện có
UPDATE product_variants 
SET available_quantity = GREATEST(0, stock_quantity - COALESCE(reserved_quantity, 0))
WHERE available_quantity IS NULL OR available_quantity = 0;

-- Cập nhật updated_at cho các SKU hiện có
UPDATE product_variants 
SET updated_at = created_at 
WHERE updated_at IS NULL;

