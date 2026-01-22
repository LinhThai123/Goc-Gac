-- ==========================================
-- V11: Thêm hỗ trợ xóa mềm (soft delete) cho bảng products
-- ==========================================

-- Thêm cột deleted_at vào bảng products
ALTER TABLE products
ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

-- Tạo index cho deleted_at để tối ưu query
CREATE INDEX IF NOT EXISTS idx_products_deleted_at ON products(deleted_at);

-- Cập nhật comment cho cột
COMMENT ON COLUMN products.deleted_at IS 'Thời gian xóa mềm sản phẩm. NULL nếu sản phẩm chưa bị xóa.';

