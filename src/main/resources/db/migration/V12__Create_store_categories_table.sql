-- ==========================================
-- V12: Tạo bảng store_categories và indexes
-- Bảng này đã được tạo trong V3, nhưng file này đảm bảo đầy đủ indexes
-- ==========================================

-- Tạo bảng store_categories (nếu chưa tồn tại - đã có trong V3)
CREATE TABLE IF NOT EXISTS store_categories (
    id BIGSERIAL PRIMARY KEY,
    store_id BIGINT NOT NULL,
    parent_id BIGINT,
    category_name VARCHAR(255) NOT NULL,
    display_order INTEGER NOT NULL DEFAULT 0,
    level INTEGER NOT NULL DEFAULT 1,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_store_category_store FOREIGN KEY (store_id) REFERENCES stores(id) ON DELETE CASCADE,
    CONSTRAINT fk_store_category_parent FOREIGN KEY (parent_id) REFERENCES store_categories(id) ON DELETE SET NULL
);

-- Tạo indexes cho store_categories (nếu chưa tồn tại)
-- Index cho store_id (query theo store)
CREATE INDEX IF NOT EXISTS idx_store_categories_store ON store_categories(store_id);

-- Index cho parent_id (query child categories)
CREATE INDEX IF NOT EXISTS idx_store_categories_parent ON store_categories(parent_id);

-- Index cho is_active (filter active categories)
CREATE INDEX IF NOT EXISTS idx_store_categories_active ON store_categories(is_active);

-- Composite index cho store_id và is_active (query active categories của store)
CREATE INDEX IF NOT EXISTS idx_store_categories_store_active ON store_categories(store_id, is_active);

-- Composite index cho store_id và parent_id (query child categories của store)
CREATE INDEX IF NOT EXISTS idx_store_categories_store_parent ON store_categories(store_id, parent_id);

-- Index cho display_order (sorting)
CREATE INDEX IF NOT EXISTS idx_store_categories_display_order ON store_categories(display_order);

-- Comments
COMMENT ON TABLE store_categories IS 'Bảng lưu trữ danh mục sản phẩm riêng của từng store';
COMMENT ON COLUMN store_categories.store_id IS 'ID của store sở hữu category này';
COMMENT ON COLUMN store_categories.parent_id IS 'ID của category cha (null nếu là root category)';
COMMENT ON COLUMN store_categories.category_name IS 'Tên danh mục';
COMMENT ON COLUMN store_categories.display_order IS 'Thứ tự hiển thị';
COMMENT ON COLUMN store_categories.level IS 'Cấp độ của category (1 = root, 2 = level 2, ...)';
COMMENT ON COLUMN store_categories.is_active IS 'Trạng thái active/inactive';

