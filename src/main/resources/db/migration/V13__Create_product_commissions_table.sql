-- Sprint 10 (M17 - Affiliate): mức hoa hồng tiếp thị liên kết theo từng sản phẩm.
-- Người bán đặt commission_rate (phần trăm) cho sản phẩm của gian hàng mình.
-- Nếu một sản phẩm không có bản ghi ở đây, hệ thống dùng commission_rate mặc định của đối tác.

CREATE TABLE IF NOT EXISTS product_commissions (
    id              BIGSERIAL PRIMARY KEY,
    product_id      BIGINT       NOT NULL UNIQUE,
    store_id        BIGINT       NOT NULL,
    commission_rate NUMERIC(5,2) NOT NULL DEFAULT 0,  -- phần trăm, ví dụ 5.00 = 5%
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_product_commissions_product ON product_commissions(product_id);
CREATE INDEX IF NOT EXISTS idx_product_commissions_store ON product_commissions(store_id);
