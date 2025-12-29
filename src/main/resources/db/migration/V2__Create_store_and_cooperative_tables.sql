-- ==========================================
-- V2: Store và Cooperative Management
-- ==========================================

-- Tạo bảng stores
CREATE TABLE IF NOT EXISTS stores (
    id BIGSERIAL PRIMARY KEY,
    seller_id BIGINT,
    cooperative_id BIGINT,
    store_name VARCHAR(255) NOT NULL,
    store_code VARCHAR(50) UNIQUE,
    description TEXT,
    logo_url VARCHAR(500),
    banner_url VARCHAR(500),
    video_url VARCHAR(500),
    contact_phone VARCHAR(20),
    contact_email VARCHAR(255),
    address TEXT,
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    is_branded BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_store_owner CHECK (
        (seller_id IS NULL AND cooperative_id IS NOT NULL) OR
        (seller_id IS NOT NULL AND cooperative_id IS NULL) OR
        (seller_id IS NULL AND cooperative_id IS NULL)
    )
);

-- Tạo indexes cho stores
CREATE INDEX IF NOT EXISTS idx_stores_seller ON stores(seller_id);
CREATE INDEX IF NOT EXISTS idx_stores_cooperative ON stores(cooperative_id);
CREATE INDEX IF NOT EXISTS idx_status_stores ON stores(status);

-- Tạo bảng cooperative_registrations
CREATE TABLE IF NOT EXISTS cooperative_registrations (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    
    -- STEP 1: Thông tin HTX cơ bản
    cooperative_name VARCHAR(200) NOT NULL,
    slug VARCHAR(100) NOT NULL UNIQUE,
    cooperative_code VARCHAR(50) NOT NULL UNIQUE,
    establishment_date DATE NOT NULL,
    cooperative_type VARCHAR(50) NOT NULL,
    scale VARCHAR(50) NOT NULL,
    short_description TEXT NOT NULL,
    
    -- STEP 2: Thông tin liên hệ
    contact_email VARCHAR(255),
    contact_phone VARCHAR(20),
    contact_phone_alt VARCHAR(20),
    website VARCHAR(500),
    facebook_page VARCHAR(500),
    
    -- STEP 3: Địa chỉ kinh doanh
    full_address TEXT,
    province VARCHAR(100),
    district VARCHAR(100),
    ward VARCHAR(100),
    postal_code VARCHAR(20),
    show_map BOOLEAN DEFAULT FALSE,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    
    -- STEP 4: Thông tin người đại diện
    representative_name VARCHAR(200),
    representative_position VARCHAR(100),
    representative_id_number VARCHAR(50),
    representative_id_issue_date DATE,
    representative_id_issue_place VARCHAR(200),
    representative_email VARCHAR(255),
    representative_phone VARCHAR(20),
    representative_id_front_image VARCHAR(500),
    representative_id_back_image VARCHAR(500),
    
    -- STEP 5: Thông tin pháp lý
    tax_code VARCHAR(50) UNIQUE,
    registration_certificate_number VARCHAR(100),
    registration_certificate_issue_date DATE,
    registration_certificate_issue_place VARCHAR(200),
    registration_certificate_image VARCHAR(500),
    tax_code_certificate_image VARCHAR(500),
    
    -- STEP 6: Thông tin kinh doanh
    main_product_description TEXT,
    business_scale VARCHAR(50),
    has_special_certification BOOLEAN,
    special_certification_details TEXT,
    
    -- Status và tracking
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    current_step INTEGER NOT NULL DEFAULT 0,
    submitted_at TIMESTAMP,
    reviewed_at TIMESTAMP,
    reviewed_by BIGINT,
    rejection_reason TEXT,
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_cooperative_registration_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Tạo indexes cho cooperative_registrations
CREATE INDEX IF NOT EXISTS idx_cooperative_registrations_user_id ON cooperative_registrations(user_id);
CREATE INDEX IF NOT EXISTS idx_cooperative_registrations_status ON cooperative_registrations(status);
CREATE INDEX IF NOT EXISTS idx_cooperative_registrations_slug ON cooperative_registrations(slug);
CREATE INDEX IF NOT EXISTS idx_cooperative_registrations_code ON cooperative_registrations(cooperative_code);

-- Tạo bảng cooperative_registration_product_types (ElementCollection)
CREATE TABLE IF NOT EXISTS cooperative_registration_product_types (
    registration_id BIGINT NOT NULL,
    product_type VARCHAR(255),
    CONSTRAINT fk_registration_product_types FOREIGN KEY (registration_id) REFERENCES cooperative_registrations(id) ON DELETE CASCADE
);

-- Tạo bảng cooperatives
CREATE TABLE IF NOT EXISTS cooperatives (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    registration_id BIGINT NOT NULL,
    
    -- Thông tin cơ bản
    cooperative_name VARCHAR(200) NOT NULL,
    slug VARCHAR(100) NOT NULL UNIQUE,
    cooperative_code VARCHAR(50) NOT NULL UNIQUE,
    establishment_date DATE NOT NULL,
    cooperative_type VARCHAR(50) NOT NULL,
    scale VARCHAR(50) NOT NULL,
    short_description TEXT NOT NULL,
    
    -- Thông tin liên hệ
    contact_email VARCHAR(255) NOT NULL,
    contact_phone VARCHAR(20) NOT NULL,
    contact_phone_alt VARCHAR(20),
    website VARCHAR(500),
    facebook_page VARCHAR(500),
    
    -- Địa chỉ
    full_address TEXT NOT NULL,
    province VARCHAR(100) NOT NULL,
    district VARCHAR(100) NOT NULL,
    ward VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20),
    show_map BOOLEAN NOT NULL DEFAULT FALSE,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    
    -- Thông tin người đại diện
    representative_name VARCHAR(200) NOT NULL,
    representative_position VARCHAR(100) NOT NULL,
    representative_id_number VARCHAR(50) NOT NULL,
    representative_id_issue_date DATE NOT NULL,
    representative_id_issue_place VARCHAR(200) NOT NULL,
    representative_email VARCHAR(255) NOT NULL,
    representative_phone VARCHAR(20) NOT NULL,
    
    -- Thông tin pháp lý
    tax_code VARCHAR(50) UNIQUE,
    registration_certificate_number VARCHAR(100) NOT NULL,
    registration_certificate_issue_date DATE NOT NULL,
    registration_certificate_issue_place VARCHAR(200) NOT NULL,
    
    -- Thông tin kinh doanh
    main_product_description TEXT NOT NULL,
    
    -- Thông tin bổ sung
    logo_url VARCHAR(500),
    banner_url VARCHAR(500),
    video_url VARCHAR(500),
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_cooperative_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_cooperative_registration FOREIGN KEY (registration_id) REFERENCES cooperative_registrations(id)
);

-- Tạo indexes cho cooperatives
CREATE INDEX IF NOT EXISTS idx_cooperatives_user_id ON cooperatives(user_id);
CREATE INDEX IF NOT EXISTS idx_cooperatives_slug ON cooperatives(slug);
CREATE INDEX IF NOT EXISTS idx_cooperatives_code ON cooperatives(cooperative_code);

-- Tạo bảng cooperative_product_types (ElementCollection)
CREATE TABLE IF NOT EXISTS cooperative_product_types (
    cooperative_id BIGINT NOT NULL,
    product_type VARCHAR(255),
    CONSTRAINT fk_cooperative_product_types FOREIGN KEY (cooperative_id) REFERENCES cooperatives(id) ON DELETE CASCADE
);

-- Tạo bảng cooperative_members
CREATE TABLE IF NOT EXISTS cooperative_members (
    id BIGSERIAL PRIMARY KEY,
    cooperative_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role VARCHAR(50) DEFAULT 'MEMBER',
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    rejection_reason TEXT,
    joined_at TIMESTAMP,
    applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_at TIMESTAMP,
    reviewed_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_cooperative_member_cooperative FOREIGN KEY (cooperative_id) REFERENCES cooperatives(id) ON DELETE CASCADE,
    CONSTRAINT fk_cooperative_member_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT unique_cooperative_member UNIQUE (cooperative_id, user_id)
);

-- Tạo indexes cho cooperative_members
CREATE INDEX IF NOT EXISTS idx_cooperative_members_cooperative ON cooperative_members(cooperative_id);
CREATE INDEX IF NOT EXISTS idx_cooperative_members_user ON cooperative_members(user_id);
CREATE INDEX IF NOT EXISTS idx_cooperative_members_status ON cooperative_members(status);

-- Thêm foreign key cho stores sau khi tạo cooperatives
ALTER TABLE stores 
    ADD CONSTRAINT fk_store_cooperative FOREIGN KEY (cooperative_id) REFERENCES cooperatives(id) ON DELETE SET NULL;

-- Thêm foreign key cho stores.seller_id (nếu cần)
-- ALTER TABLE stores 
--     ADD CONSTRAINT fk_store_seller FOREIGN KEY (seller_id) REFERENCES users(id) ON DELETE SET NULL;

