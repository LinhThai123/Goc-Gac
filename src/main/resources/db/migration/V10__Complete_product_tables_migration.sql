-- ==========================================
-- V10: Migration tổng hợp cho tất cả các bảng liên quan đến Product
-- File này đảm bảo tất cả các bảng Product được tạo đầy đủ và đúng cấu trúc
-- ==========================================

-- ========== 1. Bảng products (đã được cập nhật trong V8 và V9) ==========
-- Lưu ý: Bảng products đã được tạo trong V3 và cập nhật trong V8, V9
-- Các cột price, stock_quantity, weight_kg, length_cm, width_cm, height_cm đã được xóa
-- vì mỗi Product đều bắt buộc có ít nhất 1 SKU

-- ========== 2. Bảng product_variants (SKU) ==========
-- Lưu ý: Bảng này đã được tạo trong V3 và cập nhật trong V8
-- Đảm bảo tất cả các cột cần thiết đã có

-- Kiểm tra và thêm các cột nếu chưa có (cho trường hợp migration chạy độc lập)
DO $$
BEGIN
    -- Thêm các cột cho SKU nếu chưa có
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'product_variants' AND column_name = 'barcode') THEN
        ALTER TABLE product_variants ADD COLUMN barcode VARCHAR(100);
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'product_variants' AND column_name = 'compare_price') THEN
        ALTER TABLE product_variants ADD COLUMN compare_price NUMERIC(15, 2);
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'product_variants' AND column_name = 'cost_price') THEN
        ALTER TABLE product_variants ADD COLUMN cost_price NUMERIC(15, 2);
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'product_variants' AND column_name = 'reserved_quantity') THEN
        ALTER TABLE product_variants ADD COLUMN reserved_quantity INTEGER NOT NULL DEFAULT 0;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'product_variants' AND column_name = 'available_quantity') THEN
        ALTER TABLE product_variants ADD COLUMN available_quantity INTEGER NOT NULL DEFAULT 0;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'product_variants' AND column_name = 'weight_kg') THEN
        ALTER TABLE product_variants ADD COLUMN weight_kg NUMERIC(10, 3);
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'product_variants' AND column_name = 'length_cm') THEN
        ALTER TABLE product_variants ADD COLUMN length_cm NUMERIC(10, 2);
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'product_variants' AND column_name = 'width_cm') THEN
        ALTER TABLE product_variants ADD COLUMN width_cm NUMERIC(10, 2);
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'product_variants' AND column_name = 'height_cm') THEN
        ALTER TABLE product_variants ADD COLUMN height_cm NUMERIC(10, 2);
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'product_variants' AND column_name = 'display_order') THEN
        ALTER TABLE product_variants ADD COLUMN display_order INTEGER NOT NULL DEFAULT 0;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'product_variants' AND column_name = 'updated_at') THEN
        ALTER TABLE product_variants ADD COLUMN updated_at TIMESTAMP;
    END IF;
    
    -- Các thuộc tính biến thể
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'product_variants' AND column_name = 'size') THEN
        ALTER TABLE product_variants ADD COLUMN size VARCHAR(50);
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'product_variants' AND column_name = 'color') THEN
        ALTER TABLE product_variants ADD COLUMN color VARCHAR(50);
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'product_variants' AND column_name = 'material') THEN
        ALTER TABLE product_variants ADD COLUMN material VARCHAR(100);
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'product_variants' AND column_name = 'weight_variant') THEN
        ALTER TABLE product_variants ADD COLUMN weight_variant VARCHAR(50);
    END IF;
END $$;

-- Đảm bảo SKU là NOT NULL
DO $$
BEGIN
    ALTER TABLE product_variants ALTER COLUMN sku SET NOT NULL;
EXCEPTION
    WHEN others THEN NULL;
END $$;

-- Tạo indexes cho product_variants nếu chưa có
CREATE INDEX IF NOT EXISTS idx_variant_product ON product_variants(product_id);
CREATE INDEX IF NOT EXISTS idx_variant_sku ON product_variants(sku);
CREATE INDEX IF NOT EXISTS idx_variant_status ON product_variants(status);
CREATE INDEX IF NOT EXISTS idx_variant_size ON product_variants(size);
CREATE INDEX IF NOT EXISTS idx_variant_color ON product_variants(color);

-- ========== 3. Bảng product_images ==========
-- Đảm bảo bảng đã được tạo (từ V3)
-- Không cần thay đổi gì

-- ========== 4. Bảng product_attributes ==========
-- Đảm bảo bảng đã được tạo (từ V3)
-- Tạo index nếu chưa có
CREATE INDEX IF NOT EXISTS idx_product_attr ON product_attributes(product_id, attribute_name);

-- ========== 5. Bảng product_combo_items ==========
-- Đảm bảo bảng đã được tạo (từ V3)
-- Tạo indexes nếu chưa có
CREATE INDEX IF NOT EXISTS idx_combo_items_combo ON product_combo_items(combo_product_id);
CREATE INDEX IF NOT EXISTS idx_combo_items_product ON product_combo_items(product_id);

-- ========== 6. Bảng product_videos ==========
-- Đảm bảo bảng đã được tạo (từ V3)
-- Tạo index nếu chưa có
CREATE INDEX IF NOT EXISTS idx_product_videos_product ON product_videos(product_id);

-- ========== 7. Bảng product_origin_tracking ==========
-- Đảm bảo bảng đã được tạo (từ V3)
-- Tạo index nếu chưa có
CREATE INDEX IF NOT EXISTS idx_origin_tracking_product ON product_origin_tracking(product_id);
CREATE INDEX IF NOT EXISTS idx_origin_tracking_qr ON product_origin_tracking(qr_code);

-- ========== 8. Bảng product_reviews ==========
-- Đảm bảo bảng đã được tạo (từ V5)
-- Tạo indexes nếu chưa có
CREATE INDEX IF NOT EXISTS idx_product_reviews_product ON product_reviews(product_id);
CREATE INDEX IF NOT EXISTS idx_product_reviews_user ON product_reviews(user_id);
CREATE INDEX IF NOT EXISTS idx_rating ON product_reviews(rating);
CREATE INDEX IF NOT EXISTS idx_product_reviews_status ON product_reviews(status);

-- ========== 9. Bảng product_recommendations ==========
-- Đảm bảo bảng đã được tạo (từ V5)
-- Tạo indexes nếu chưa có
CREATE INDEX IF NOT EXISTS idx_source ON product_recommendations(source_product_id);
CREATE INDEX IF NOT EXISTS idx_recommended ON product_recommendations(recommended_product_id);
CREATE INDEX IF NOT EXISTS idx_product_recommendations_type ON product_recommendations(recommendation_type);

-- ========== 10. Cập nhật dữ liệu ==========
-- Cập nhật available_quantity cho các SKU hiện có
UPDATE product_variants 
SET available_quantity = GREATEST(0, stock_quantity - COALESCE(reserved_quantity, 0))
WHERE available_quantity IS NULL OR available_quantity = 0;

-- Cập nhật updated_at cho các SKU hiện có
UPDATE product_variants 
SET updated_at = created_at 
WHERE updated_at IS NULL;

-- ========== 11. Ràng buộc dữ liệu ==========
-- Đảm bảo mỗi Product có ít nhất 1 SKU (validation ở application level)
-- Có thể thêm trigger hoặc constraint nếu cần

-- ========== 12. Comments cho documentation ==========
COMMENT ON TABLE products IS 'Bảng sản phẩm chính - mỗi sản phẩm bắt buộc có ít nhất 1 SKU';
COMMENT ON TABLE product_variants IS 'Bảng SKU - lưu tất cả thông tin về giá, tồn kho, kích thước, thuộc tính của từng biến thể';
COMMENT ON TABLE product_images IS 'Bảng hình ảnh sản phẩm';
COMMENT ON TABLE product_attributes IS 'Bảng thuộc tính chung của sản phẩm (không phải biến thể)';
COMMENT ON TABLE product_combo_items IS 'Bảng các sản phẩm trong combo';
COMMENT ON TABLE product_videos IS 'Bảng video sản phẩm';
COMMENT ON TABLE product_origin_tracking IS 'Bảng thông tin truy xuất nguồn gốc sản phẩm';
COMMENT ON TABLE product_reviews IS 'Bảng đánh giá sản phẩm';
COMMENT ON TABLE product_recommendations IS 'Bảng gợi ý sản phẩm liên quan';

