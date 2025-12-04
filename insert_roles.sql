-- Insert default roles với created_at
INSERT INTO roles (name, code, description, created_at) VALUES
    ('Super Admin', 'SUPER_ADMIN', 'Ban chủ nhiệm HTX - quyền cao nhất', CURRENT_TIMESTAMP),
    ('Hợp tác xã', 'COOPERATIVE_MANAGER', 'Chủ nhiệm hoặc Văn phòng HTX', CURRENT_TIMESTAMP),
    ('Người bán', 'SELLER', 'Xã viên / Thành viên HTX bán sản phẩm', CURRENT_TIMESTAMP),
    ('Khách mua hàng', 'CUSTOMER', 'Khách hàng mua sản phẩm', CURRENT_TIMESTAMP),
    ('Kế toán HTX', 'ACCOUNTANT', 'Kế toán HTX', CURRENT_TIMESTAMP),
    ('Nhân viên kho', 'WAREHOUSE_STAFF', 'Nhân viên quản lý kho HTX', CURRENT_TIMESTAMP),
    ('Giao hàng', 'SHIPPER', 'Nhân viên giao hàng', CURRENT_TIMESTAMP),
    ('Duyệt nội dung', 'MODERATOR', 'Nhân viên duyệt nội dung', CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;

