-- ==========================================
-- V9: Xóa các cột không cần thiết ở Product
-- Vì mỗi Product đều bắt buộc có ít nhất 1 SKU,
-- nên các thông tin về giá, tồn kho, kích thước, trọng lượng
-- sẽ được quản lý ở cấp SKU (ProductVariant)
-- ==========================================

-- Xóa các cột về kích thước và trọng lượng (đã có ở ProductVariant)
ALTER TABLE products 
DROP COLUMN IF EXISTS weight_kg,
DROP COLUMN IF EXISTS length_cm,
DROP COLUMN IF EXISTS width_cm,
DROP COLUMN IF EXISTS height_cm;

-- Xóa các cột về giá (đã có ở ProductVariant)
-- Lưu ý: Nếu cần giữ lại để hiển thị giá "từ" hoặc giá thấp nhất,
-- có thể comment lại phần này
-- ALTER TABLE products 
-- DROP COLUMN IF EXISTS price,
-- DROP COLUMN IF EXISTS compare_price,
-- DROP COLUMN IF EXISTS cost_price;

-- Xóa cột stock_quantity (đã có ở ProductVariant)
ALTER TABLE products 
DROP COLUMN IF EXISTS stock_quantity;

