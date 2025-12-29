-- ==========================================
-- V5: Các bảng còn lại (Review, Promotion, Voucher, User Behavior, System, etc.)
-- ==========================================

-- ========== Review & Rating ==========
-- Tạo bảng product_reviews
CREATE TABLE IF NOT EXISTS product_reviews (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    order_id BIGINT,
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    review_title VARCHAR(255),
    review_content TEXT,
    images TEXT,
    helpful_count INTEGER NOT NULL DEFAULT 0,
    is_verified_purchase BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_product_review_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT fk_product_review_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_product_review_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE SET NULL
);

-- Tạo indexes cho product_reviews
CREATE INDEX IF NOT EXISTS idx_product_reviews_product ON product_reviews(product_id);
CREATE INDEX IF NOT EXISTS idx_product_reviews_user ON product_reviews(user_id);
CREATE INDEX IF NOT EXISTS idx_rating ON product_reviews(rating);
CREATE INDEX IF NOT EXISTS idx_product_reviews_status ON product_reviews(status);

-- Tạo bảng review_replies
CREATE TABLE IF NOT EXISTS review_replies (
    id BIGSERIAL PRIMARY KEY,
    review_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    reply_content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_review_reply_review FOREIGN KEY (review_id) REFERENCES product_reviews(id) ON DELETE CASCADE,
    CONSTRAINT fk_review_reply_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ========== Promotion & Voucher ==========
-- Tạo bảng promotions
CREATE TABLE IF NOT EXISTS promotions (
    id BIGSERIAL PRIMARY KEY,
    store_id BIGINT,
    promotion_name VARCHAR(255) NOT NULL,
    description TEXT,
    promotion_type VARCHAR(50) NOT NULL,
    discount_value NUMERIC(15, 2),
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'SCHEDULED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_promotion_store FOREIGN KEY (store_id) REFERENCES stores(id) ON DELETE CASCADE
);

-- Tạo indexes cho promotions
CREATE INDEX IF NOT EXISTS idx_promotions_store ON promotions(store_id);
CREATE INDEX IF NOT EXISTS idx_promotions_dates ON promotions(start_date, end_date);
CREATE INDEX IF NOT EXISTS idx_promotions_status ON promotions(status);

-- Tạo bảng promotion_products
CREATE TABLE IF NOT EXISTS promotion_products (
    id BIGSERIAL PRIMARY KEY,
    promotion_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_promotion_product_promotion FOREIGN KEY (promotion_id) REFERENCES promotions(id) ON DELETE CASCADE,
    CONSTRAINT fk_promotion_product_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- Tạo bảng vouchers
CREATE TABLE IF NOT EXISTS vouchers (
    id BIGSERIAL PRIMARY KEY,
    store_id BIGINT,
    voucher_code VARCHAR(50) NOT NULL UNIQUE,
    voucher_name VARCHAR(255) NOT NULL,
    voucher_type VARCHAR(50) NOT NULL,
    discount_type VARCHAR(50) NOT NULL,
    discount_value NUMERIC(15, 2) NOT NULL,
    max_discount_amount NUMERIC(15, 2),
    min_order_value NUMERIC(15, 2),
    usage_limit INTEGER,
    usage_count INTEGER NOT NULL DEFAULT 0,
    user_usage_limit INTEGER NOT NULL DEFAULT 1,
    member_rank_id BIGINT,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_voucher_store FOREIGN KEY (store_id) REFERENCES stores(id) ON DELETE CASCADE,
    CONSTRAINT fk_voucher_member_rank FOREIGN KEY (member_rank_id) REFERENCES member_ranks(id) ON DELETE SET NULL
);

-- Tạo indexes cho vouchers
CREATE INDEX IF NOT EXISTS idx_vouchers_code ON vouchers(voucher_code);
CREATE INDEX IF NOT EXISTS idx_vouchers_store ON vouchers(store_id);
CREATE INDEX IF NOT EXISTS idx_vouchers_dates ON vouchers(start_date, end_date);

-- Tạo bảng user_vouchers
CREATE TABLE IF NOT EXISTS user_vouchers (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    voucher_id BIGINT NOT NULL,
    usage_count INTEGER NOT NULL DEFAULT 0,
    saved_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_voucher_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_voucher_voucher FOREIGN KEY (voucher_id) REFERENCES vouchers(id) ON DELETE CASCADE,
    CONSTRAINT unique_user_voucher UNIQUE (user_id, voucher_id)
);

-- Tạo bảng order_vouchers
CREATE TABLE IF NOT EXISTS order_vouchers (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    voucher_id BIGINT NOT NULL,
    voucher_code VARCHAR(50) NOT NULL,
    discount_amount NUMERIC(15, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_order_voucher_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_order_voucher_voucher FOREIGN KEY (voucher_id) REFERENCES vouchers(id) ON DELETE CASCADE
);

-- Tạo bảng voucher_usage
CREATE TABLE IF NOT EXISTS voucher_usage (
    id BIGSERIAL PRIMARY KEY,
    voucher_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    discount_amount NUMERIC(15, 2) NOT NULL,
    used_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_voucher_usage_voucher FOREIGN KEY (voucher_id) REFERENCES vouchers(id) ON DELETE CASCADE,
    CONSTRAINT fk_voucher_usage_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_voucher_usage_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

-- Tạo indexes cho voucher_usage
CREATE INDEX IF NOT EXISTS idx_voucher ON voucher_usage(voucher_id);
CREATE INDEX IF NOT EXISTS idx_voucher_usage_user ON voucher_usage(user_id);

-- ========== Stock Management ==========
-- Tạo bảng stock_transactions
CREATE TABLE IF NOT EXISTS stock_transactions (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    variant_id BIGINT,
    transaction_type VARCHAR(50) NOT NULL,
    quantity INTEGER NOT NULL,
    reference_type VARCHAR(50),
    reference_id BIGINT,
    note TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    CONSTRAINT fk_stock_transaction_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT fk_stock_transaction_variant FOREIGN KEY (variant_id) REFERENCES product_variants(id) ON DELETE SET NULL
);

-- Tạo indexes cho stock_transactions
CREATE INDEX IF NOT EXISTS idx_stock_transactions_product ON stock_transactions(product_id);
CREATE INDEX IF NOT EXISTS idx_stock_transactions_created ON stock_transactions(created_at);

-- ========== Loyalty Program ==========
-- Tạo bảng loyalty_config
CREATE TABLE IF NOT EXISTS loyalty_config (
    id BIGSERIAL PRIMARY KEY,
    event_type VARCHAR(100) NOT NULL UNIQUE,
    event_name VARCHAR(255) NOT NULL,
    points_awarded INTEGER NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_event_type UNIQUE (event_type)
);

-- Tạo bảng loyalty_transactions
CREATE TABLE IF NOT EXISTS loyalty_transactions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    transaction_type VARCHAR(50) NOT NULL,
    points INTEGER NOT NULL,
    event_type VARCHAR(100),
    reference_id BIGINT,
    reference_type VARCHAR(50),
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_loyalty_transaction_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Tạo index cho loyalty_transactions
CREATE INDEX IF NOT EXISTS idx_user_transaction ON loyalty_transactions(user_id, created_at);

-- Tạo bảng user_rank_history
CREATE TABLE IF NOT EXISTS user_rank_history (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    rank_id BIGINT NOT NULL,
    total_spending NUMERIC(15, 2) NOT NULL,
    achieved_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at DATE,
    CONSTRAINT fk_user_rank_history_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_rank_history_rank FOREIGN KEY (rank_id) REFERENCES member_ranks(id) ON DELETE CASCADE
);

-- ========== User Behavior Tracking ==========
-- Tạo bảng user_preferences
CREATE TABLE IF NOT EXISTS user_preferences (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    preference_key VARCHAR(100) NOT NULL,
    preference_value TEXT,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_preference_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT unique_user_preference UNIQUE (user_id, preference_key)
);

-- Tạo bảng user_product_views
CREATE TABLE IF NOT EXISTS user_product_views (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    product_id BIGINT NOT NULL,
    view_duration INTEGER,
    viewed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_product_view_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_product_view_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- Tạo indexes cho user_product_views
CREATE INDEX IF NOT EXISTS idx_user_product_views_user ON user_product_views(user_id);
CREATE INDEX IF NOT EXISTS idx_user_product_views_product ON user_product_views(product_id);
CREATE INDEX IF NOT EXISTS idx_viewed ON user_product_views(viewed_at);

-- Tạo bảng user_search_history
CREATE TABLE IF NOT EXISTS user_search_history (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    search_keyword VARCHAR(500) NOT NULL,
    result_count INTEGER NOT NULL DEFAULT 0,
    searched_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_search_history_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Tạo indexes cho user_search_history
CREATE INDEX IF NOT EXISTS idx_user_search_history_user ON user_search_history(user_id);
CREATE INDEX IF NOT EXISTS idx_keyword ON user_search_history(search_keyword);
CREATE INDEX IF NOT EXISTS idx_searched ON user_search_history(searched_at);

-- Tạo bảng user_sessions
CREATE TABLE IF NOT EXISTS user_sessions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    session_id VARCHAR(255) NOT NULL UNIQUE,
    ip_address VARCHAR(45),
    user_agent TEXT,
    device_type VARCHAR(50),
    started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ended_at TIMESTAMP,
    page_views INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT fk_user_session_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Tạo indexes cho user_sessions
CREATE INDEX IF NOT EXISTS idx_user_sessions_user ON user_sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_session ON user_sessions(session_id);
CREATE INDEX IF NOT EXISTS idx_user_sessions_started ON user_sessions(started_at);

-- Tạo bảng product_recommendations
CREATE TABLE IF NOT EXISTS product_recommendations (
    id BIGSERIAL PRIMARY KEY,
    source_product_id BIGINT NOT NULL,
    recommended_product_id BIGINT NOT NULL,
    recommendation_type VARCHAR(50) NOT NULL,
    score NUMERIC(5, 4) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_recommendation_source FOREIGN KEY (source_product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT fk_recommendation_recommended FOREIGN KEY (recommended_product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- Tạo indexes cho product_recommendations
CREATE INDEX IF NOT EXISTS idx_source ON product_recommendations(source_product_id);
CREATE INDEX IF NOT EXISTS idx_product_recommendations_type ON product_recommendations(recommendation_type);

-- ========== Notifications ==========
-- Tạo bảng notifications
CREATE TABLE IF NOT EXISTS notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    notification_type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    reference_type VARCHAR(50),
    reference_id BIGINT,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    read_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Tạo indexes cho notifications
CREATE INDEX IF NOT EXISTS idx_notifications_user ON notifications(user_id);
CREATE INDEX IF NOT EXISTS idx_read ON notifications(is_read);
CREATE INDEX IF NOT EXISTS idx_notifications_type ON notifications(notification_type);

-- ========== Messaging ==========
-- Tạo bảng conversations
CREATE TABLE IF NOT EXISTS conversations (
    id BIGSERIAL PRIMARY KEY,
    buyer_id BIGINT NOT NULL,
    seller_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,
    last_message TEXT,
    last_message_at TIMESTAMP,
    unread_count_buyer INTEGER NOT NULL DEFAULT 0,
    unread_count_seller INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_conversation_buyer FOREIGN KEY (buyer_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_conversation_seller FOREIGN KEY (seller_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_conversation_store FOREIGN KEY (store_id) REFERENCES stores(id) ON DELETE CASCADE,
    CONSTRAINT unique_conversation UNIQUE (buyer_id, store_id)
);

-- Tạo indexes cho conversations
CREATE INDEX IF NOT EXISTS idx_buyer ON conversations(buyer_id);
CREATE INDEX IF NOT EXISTS idx_conversations_seller ON conversations(seller_id);
CREATE INDEX IF NOT EXISTS idx_conversations_store ON conversations(store_id);

-- Tạo bảng messages
CREATE TABLE IF NOT EXISTS messages (
    id BIGSERIAL PRIMARY KEY,
    conversation_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    message_text TEXT NOT NULL,
    attachment_url VARCHAR(500),
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    read_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_message_conversation FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE,
    CONSTRAINT fk_message_sender FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Tạo indexes cho messages
CREATE INDEX IF NOT EXISTS idx_conversation ON messages(conversation_id);
CREATE INDEX IF NOT EXISTS idx_messages_created ON messages(created_at);

-- Tạo bảng auto_replies
CREATE TABLE IF NOT EXISTS auto_replies (
    id BIGSERIAL PRIMARY KEY,
    store_id BIGINT NOT NULL,
    trigger_keyword VARCHAR(255),
    reply_message TEXT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_auto_reply_store FOREIGN KEY (store_id) REFERENCES stores(id) ON DELETE CASCADE
);

-- ========== Affiliate ==========
-- Tạo bảng affiliate_registrations
CREATE TABLE IF NOT EXISTS affiliate_registrations (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    application_reason TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    rejection_reason TEXT,
    submitted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_at TIMESTAMP,
    reviewed_by BIGINT,
    CONSTRAINT fk_affiliate_registration_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Tạo bảng affiliate_partners
CREATE TABLE IF NOT EXISTS affiliate_partners (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    affiliate_code VARCHAR(50) NOT NULL UNIQUE,
    commission_rate NUMERIC(5, 2) NOT NULL,
    total_clicks INTEGER NOT NULL DEFAULT 0,
    total_orders INTEGER NOT NULL DEFAULT 0,
    total_revenue NUMERIC(15, 2) NOT NULL DEFAULT 0,
    total_commission NUMERIC(15, 2) NOT NULL DEFAULT 0,
    status VARCHAR(50) NOT NULL DEFAULT 'active',
    joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_affiliate_partner_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT unique_user_affiliate UNIQUE (user_id)
);

-- Tạo index cho affiliate_partners
CREATE INDEX IF NOT EXISTS idx_affiliate_partners_code ON affiliate_partners(affiliate_code);

-- Tạo bảng affiliate_links
CREATE TABLE IF NOT EXISTS affiliate_links (
    id BIGSERIAL PRIMARY KEY,
    partner_id BIGINT NOT NULL,
    product_id BIGINT,
    store_id BIGINT,
    affiliate_code VARCHAR(50) NOT NULL,
    link_url TEXT NOT NULL,
    click_count INTEGER NOT NULL DEFAULT 0,
    conversion_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_affiliate_link_partner FOREIGN KEY (partner_id) REFERENCES affiliate_partners(id) ON DELETE CASCADE,
    CONSTRAINT fk_affiliate_link_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE SET NULL,
    CONSTRAINT fk_affiliate_link_store FOREIGN KEY (store_id) REFERENCES stores(id) ON DELETE SET NULL
);

-- Tạo indexes cho affiliate_links
CREATE INDEX IF NOT EXISTS idx_affiliate_links_partner ON affiliate_links(partner_id);
CREATE INDEX IF NOT EXISTS idx_affiliate_links_code ON affiliate_links(affiliate_code);

-- Tạo bảng affiliate_clicks
CREATE TABLE IF NOT EXISTS affiliate_clicks (
    id BIGSERIAL PRIMARY KEY,
    link_id BIGINT NOT NULL,
    user_id BIGINT,
    ip_address VARCHAR(45),
    clicked_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_affiliate_click_link FOREIGN KEY (link_id) REFERENCES affiliate_links(id) ON DELETE CASCADE,
    CONSTRAINT fk_affiliate_click_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- Tạo indexes cho affiliate_clicks
CREATE INDEX IF NOT EXISTS idx_link ON affiliate_clicks(link_id);
CREATE INDEX IF NOT EXISTS idx_affiliate_clicks_clicked ON affiliate_clicks(clicked_at);

-- Tạo bảng affiliate_commissions
CREATE TABLE IF NOT EXISTS affiliate_commissions (
    id BIGSERIAL PRIMARY KEY,
    partner_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    order_amount NUMERIC(15, 2) NOT NULL,
    commission_rate NUMERIC(5, 2) NOT NULL,
    commission_amount NUMERIC(15, 2) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    paid_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_affiliate_commission_partner FOREIGN KEY (partner_id) REFERENCES affiliate_partners(id) ON DELETE CASCADE,
    CONSTRAINT fk_affiliate_commission_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

-- Tạo indexes cho affiliate_commissions
CREATE INDEX IF NOT EXISTS idx_affiliate_commissions_partner ON affiliate_commissions(partner_id);
CREATE INDEX IF NOT EXISTS idx_affiliate_commissions_order ON affiliate_commissions(order_id);
CREATE INDEX IF NOT EXISTS idx_affiliate_commissions_status ON affiliate_commissions(status);

-- ========== Advertising ==========
-- Tạo bảng ad_campaigns
CREATE TABLE IF NOT EXISTS ad_campaigns (
    id BIGSERIAL PRIMARY KEY,
    store_id BIGINT NOT NULL,
    campaign_name VARCHAR(255) NOT NULL,
    product_id BIGINT NOT NULL,
    ad_image_url VARCHAR(500),
    ad_position VARCHAR(100),
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    budget NUMERIC(15, 2),
    cost_per_click NUMERIC(10, 2),
    total_clicks INTEGER NOT NULL DEFAULT 0,
    total_conversions INTEGER NOT NULL DEFAULT 0,
    total_spent NUMERIC(15, 2) NOT NULL DEFAULT 0,
    approval_status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    rejection_reason TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'scheduled',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    approved_at TIMESTAMP,
    approved_by BIGINT,
    CONSTRAINT fk_ad_campaign_store FOREIGN KEY (store_id) REFERENCES stores(id) ON DELETE CASCADE,
    CONSTRAINT fk_ad_campaign_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- Tạo indexes cho ad_campaigns
CREATE INDEX IF NOT EXISTS idx_ad_campaigns_store ON ad_campaigns(store_id);
CREATE INDEX IF NOT EXISTS idx_ad_campaigns_status ON ad_campaigns(status);
CREATE INDEX IF NOT EXISTS idx_ad_campaigns_dates ON ad_campaigns(start_date, end_date);

-- Tạo bảng ad_clicks
CREATE TABLE IF NOT EXISTS ad_clicks (
    id BIGSERIAL PRIMARY KEY,
    campaign_id BIGINT NOT NULL,
    user_id BIGINT,
    ip_address VARCHAR(45),
    user_agent TEXT,
    clicked_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    converted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_ad_click_campaign FOREIGN KEY (campaign_id) REFERENCES ad_campaigns(id) ON DELETE CASCADE,
    CONSTRAINT fk_ad_click_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- Tạo indexes cho ad_clicks
CREATE INDEX IF NOT EXISTS idx_campaign ON ad_clicks(campaign_id);
CREATE INDEX IF NOT EXISTS idx_ad_clicks_clicked ON ad_clicks(clicked_at);

-- Tạo bảng banners
CREATE TABLE IF NOT EXISTS banners (
    id BIGSERIAL PRIMARY KEY,
    banner_name VARCHAR(255) NOT NULL,
    banner_position VARCHAR(100) NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    mobile_image_url VARCHAR(500),
    link_url VARCHAR(500),
    target_type VARCHAR(20) NOT NULL DEFAULT '_self',
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    display_order INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Tạo indexes cho banners
CREATE INDEX IF NOT EXISTS idx_banners_position ON banners(banner_position);
CREATE INDEX IF NOT EXISTS idx_banners_active ON banners(is_active);
CREATE INDEX IF NOT EXISTS idx_banners_dates ON banners(start_date, end_date);

-- ========== News & Content ==========
-- Tạo bảng news_categories
CREATE TABLE IF NOT EXISTS news_categories (
    id BIGSERIAL PRIMARY KEY,
    category_name VARCHAR(255) NOT NULL,
    category_slug VARCHAR(255) NOT NULL,
    description TEXT,
    display_order INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Tạo index cho news_categories
CREATE INDEX IF NOT EXISTS idx_news_categories_slug ON news_categories(category_slug);

-- Tạo bảng news_articles
CREATE TABLE IF NOT EXISTS news_articles (
    id BIGSERIAL PRIMARY KEY,
    category_id BIGINT,
    title VARCHAR(500) NOT NULL,
    slug VARCHAR(500) NOT NULL,
    summary TEXT,
    content TEXT NOT NULL,
    featured_image VARCHAR(500),
    author_id BIGINT,
    view_count INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    published_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_news_article_category FOREIGN KEY (category_id) REFERENCES news_categories(id) ON DELETE SET NULL,
    CONSTRAINT fk_news_article_author FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE SET NULL
);

-- Tạo indexes cho news_articles
CREATE INDEX IF NOT EXISTS idx_news_articles_category ON news_articles(category_id);
CREATE INDEX IF NOT EXISTS idx_news_articles_slug ON news_articles(slug);
CREATE INDEX IF NOT EXISTS idx_news_articles_status ON news_articles(status);
CREATE INDEX IF NOT EXISTS idx_news_articles_published ON news_articles(published_at);

-- ========== System Management ==========
-- Tạo bảng menu_items
CREATE TABLE IF NOT EXISTS menu_items (
    id BIGSERIAL PRIMARY KEY,
    parent_id BIGINT,
    menu_type VARCHAR(50) NOT NULL,
    menu_name VARCHAR(100) NOT NULL,
    menu_url VARCHAR(255),
    icon VARCHAR(100),
    display_order INTEGER NOT NULL DEFAULT 0,
    permission_code VARCHAR(50),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_menu_item_parent FOREIGN KEY (parent_id) REFERENCES menu_items(id) ON DELETE SET NULL
);

-- Tạo indexes cho menu_items
CREATE INDEX IF NOT EXISTS idx_menu_items_parent ON menu_items(parent_id);
CREATE INDEX IF NOT EXISTS idx_menu_items_type ON menu_items(menu_type);

-- Tạo bảng store_permissions
CREATE TABLE IF NOT EXISTS store_permissions (
    id BIGSERIAL PRIMARY KEY,
    store_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    granted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    granted_by BIGINT,
    CONSTRAINT fk_store_permission_store FOREIGN KEY (store_id) REFERENCES stores(id) ON DELETE CASCADE,
    CONSTRAINT fk_store_permission_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_store_permission_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    CONSTRAINT unique_store_user UNIQUE (store_id, user_id)
);

-- Tạo bảng store_subscriptions
CREATE TABLE IF NOT EXISTS store_subscriptions (
    id BIGSERIAL PRIMARY KEY,
    store_id BIGINT NOT NULL,
    package_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    auto_renew BOOLEAN NOT NULL DEFAULT FALSE,
    payment_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    payment_amount NUMERIC(15, 2),
    payment_date TIMESTAMP,
    status VARCHAR(50) NOT NULL DEFAULT 'active',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_store_subscription_store FOREIGN KEY (store_id) REFERENCES stores(id) ON DELETE CASCADE
);

-- Tạo index cho store_subscriptions
CREATE INDEX IF NOT EXISTS idx_store_status ON store_subscriptions(store_id, status);

-- Tạo bảng system_configs
CREATE TABLE IF NOT EXISTS system_configs (
    id BIGSERIAL PRIMARY KEY,
    config_key VARCHAR(100) NOT NULL UNIQUE,
    config_value TEXT,
    config_type VARCHAR(50),
    description TEXT,
    is_public BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Tạo index cho system_configs
CREATE INDEX IF NOT EXISTS idx_key ON system_configs(config_key);

-- Tạo bảng system_logs
CREATE TABLE IF NOT EXISTS system_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    action VARCHAR(100) NOT NULL,
    module VARCHAR(50),
    reference_type VARCHAR(50),
    reference_id BIGINT,
    ip_address VARCHAR(45),
    user_agent TEXT,
    details TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_system_log_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- Tạo indexes cho system_logs
CREATE INDEX IF NOT EXISTS idx_system_logs_user ON system_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_action ON system_logs(action);
CREATE INDEX IF NOT EXISTS idx_system_logs_created ON system_logs(created_at);

-- Tạo bảng backup_logs
CREATE TABLE IF NOT EXISTS backup_logs (
    id BIGSERIAL PRIMARY KEY,
    backup_name VARCHAR(255) NOT NULL,
    backup_type VARCHAR(50) NOT NULL,
    file_path VARCHAR(500),
    file_size BIGINT,
    status VARCHAR(50) NOT NULL DEFAULT 'IN_PROGRESS',
    error_message TEXT,
    started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    created_by BIGINT,
    CONSTRAINT fk_backup_log_user FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
);

-- Tạo indexes cho backup_logs
CREATE INDEX IF NOT EXISTS idx_backup_logs_status ON backup_logs(status);
CREATE INDEX IF NOT EXISTS idx_backup_logs_started ON backup_logs(started_at);

