-- ==========================================
-- V7: Channel và ShopCatalog Management
-- ==========================================

-- ========== Channel (Micro-site cho shop) ==========
-- Tạo bảng channels
CREATE TABLE IF NOT EXISTS channels (
    id BIGSERIAL PRIMARY KEY,
    store_id BIGINT NOT NULL,
    cooperative_id BIGINT, -- Reference đến cooperative (nếu store thuộc cooperative)
    
    -- Thông tin cơ bản
    channel_slug VARCHAR(100) NOT NULL UNIQUE,
    channel_name VARCHAR(255) NOT NULL,
    description TEXT,
    short_description VARCHAR(500),
    
    -- Media
    banner_url VARCHAR(500),
    logo_url VARCHAR(500),
    video_url VARCHAR(500),
    
    -- Story/Nguồn gốc sản phẩm
    story_content TEXT,
    origin_story TEXT,
    
    -- Theme customization (JSON string để lưu theme settings)
    theme_settings TEXT, -- JSON: {"primaryColor": "#ff0000", "secondaryColor": "#00ff00", "logo": "url"}
    
    -- Social features
    follower_count INTEGER NOT NULL DEFAULT 0,
    view_count INTEGER NOT NULL DEFAULT 0,
    
    -- Status
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_featured BOOLEAN NOT NULL DEFAULT FALSE,
    
    -- SEO
    meta_title VARCHAR(255),
    meta_description TEXT,
    meta_keywords VARCHAR(500),
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_channel_store FOREIGN KEY (store_id) REFERENCES stores(id) ON DELETE CASCADE,
    CONSTRAINT fk_channel_cooperative FOREIGN KEY (cooperative_id) REFERENCES cooperatives(id) ON DELETE SET NULL
);

-- Tạo indexes cho channels
CREATE INDEX IF NOT EXISTS idx_channels_store ON channels(store_id);
CREATE INDEX IF NOT EXISTS idx_channels_cooperative ON channels(cooperative_id);
CREATE INDEX IF NOT EXISTS idx_channels_slug ON channels(channel_slug);
CREATE INDEX IF NOT EXISTS idx_channels_active ON channels(is_active);
CREATE INDEX IF NOT EXISTS idx_channels_featured ON channels(is_featured);

-- ========== Channel Follow (Social feature) ==========
-- Tạo bảng channel_follows
CREATE TABLE IF NOT EXISTS channel_follows (
    id BIGSERIAL PRIMARY KEY,
    channel_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    followed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_channel_follow_channel FOREIGN KEY (channel_id) REFERENCES channels(id) ON DELETE CASCADE,
    CONSTRAINT fk_channel_follow_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT unique_channel_follow UNIQUE (channel_id, user_id)
);

-- Tạo indexes cho channel_follows
CREATE INDEX IF NOT EXISTS idx_channel_follows_channel ON channel_follows(channel_id);
CREATE INDEX IF NOT EXISTS idx_channel_follows_user ON channel_follows(user_id);

-- ========== ShopCatalog (Catalog riêng cho từng shop) ==========
-- Tạo bảng shop_catalogs
CREATE TABLE IF NOT EXISTS shop_catalogs (
    id BIGSERIAL PRIMARY KEY,
    store_id BIGINT NOT NULL,
    cooperative_id BIGINT, -- Reference đến cooperative (nếu store thuộc cooperative)
    
    -- Thông tin catalog
    catalog_name VARCHAR(255) NOT NULL,
    catalog_code VARCHAR(100) UNIQUE,
    description TEXT,
    short_description VARCHAR(500),
    
    -- Display settings
    display_order INTEGER NOT NULL DEFAULT 0,
    is_featured BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    
    -- Metadata
    image_url VARCHAR(500),
    banner_url VARCHAR(500),
    
    -- Settings (JSON để lưu các settings như filter, sort, etc.)
    catalog_settings TEXT, -- JSON: {"defaultSort": "price_asc", "showFilters": true}
    
    -- Statistics
    product_count INTEGER NOT NULL DEFAULT 0,
    view_count INTEGER NOT NULL DEFAULT 0,
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_catalog_store FOREIGN KEY (store_id) REFERENCES stores(id) ON DELETE CASCADE,
    CONSTRAINT fk_catalog_cooperative FOREIGN KEY (cooperative_id) REFERENCES cooperatives(id) ON DELETE SET NULL
);

-- Tạo indexes cho shop_catalogs
CREATE INDEX IF NOT EXISTS idx_shop_catalogs_store ON shop_catalogs(store_id);
CREATE INDEX IF NOT EXISTS idx_shop_catalogs_cooperative ON shop_catalogs(cooperative_id);
CREATE INDEX IF NOT EXISTS idx_shop_catalogs_code ON shop_catalogs(catalog_code);
CREATE INDEX IF NOT EXISTS idx_shop_catalogs_active ON shop_catalogs(is_active);
CREATE INDEX IF NOT EXISTS idx_shop_catalogs_featured ON shop_catalogs(is_featured);

-- ========== Catalog Products (Mapping products vào catalog) ==========
-- Tạo bảng catalog_products (Many-to-Many relationship)
CREATE TABLE IF NOT EXISTS catalog_products (
    id BIGSERIAL PRIMARY KEY,
    catalog_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    
    -- Display settings trong catalog
    display_order INTEGER NOT NULL DEFAULT 0,
    is_featured BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    
    added_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_catalog_product_catalog FOREIGN KEY (catalog_id) REFERENCES shop_catalogs(id) ON DELETE CASCADE,
    CONSTRAINT fk_catalog_product_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT unique_catalog_product UNIQUE (catalog_id, product_id)
);

-- Tạo indexes cho catalog_products
CREATE INDEX IF NOT EXISTS idx_catalog_products_catalog ON catalog_products(catalog_id);
CREATE INDEX IF NOT EXISTS idx_catalog_products_product ON catalog_products(product_id);
CREATE INDEX IF NOT EXISTS idx_catalog_products_active ON catalog_products(is_active);
CREATE INDEX IF NOT EXISTS idx_catalog_products_featured ON catalog_products(is_featured);

-- ========== Trigger để update product_count trong shop_catalogs ==========
-- Function để update product_count
CREATE OR REPLACE FUNCTION update_catalog_product_count()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        UPDATE shop_catalogs 
        SET product_count = product_count + 1 
        WHERE id = NEW.catalog_id;
        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        UPDATE shop_catalogs 
        SET product_count = GREATEST(product_count - 1, 0) 
        WHERE id = OLD.catalog_id;
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- Trigger cho INSERT và DELETE
CREATE TRIGGER trigger_update_catalog_product_count_insert
AFTER INSERT ON catalog_products
FOR EACH ROW EXECUTE FUNCTION update_catalog_product_count();

CREATE TRIGGER trigger_update_catalog_product_count_delete
AFTER DELETE ON catalog_products
FOR EACH ROW EXECUTE FUNCTION update_catalog_product_count();

-- ========== Trigger để update follower_count trong channels ==========
-- Function để update follower_count
CREATE OR REPLACE FUNCTION update_channel_follower_count()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        UPDATE channels 
        SET follower_count = follower_count + 1 
        WHERE id = NEW.channel_id;
        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        UPDATE channels 
        SET follower_count = GREATEST(follower_count - 1, 0) 
        WHERE id = OLD.channel_id;
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- Trigger cho INSERT và DELETE
CREATE TRIGGER trigger_update_channel_follower_count_insert
AFTER INSERT ON channel_follows
FOR EACH ROW EXECUTE FUNCTION update_channel_follower_count();

CREATE TRIGGER trigger_update_channel_follower_count_delete
AFTER DELETE ON channel_follows
FOR EACH ROW EXECUTE FUNCTION update_channel_follower_count();

