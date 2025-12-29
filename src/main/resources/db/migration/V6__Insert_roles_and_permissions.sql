-- ==========================================
-- V6: Insert Roles và Permissions phù hợp với UserType
-- ==========================================

-- ========== INSERT PERMISSIONS ==========

-- Product Permissions
INSERT INTO permissions (name, code, description, resource, action, created_at, updated_at) VALUES
('Tạo sản phẩm', 'product:create', 'Quyền tạo sản phẩm mới trong hệ thống', 'product', 'create', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Xem sản phẩm', 'product:view', 'Quyền xem danh sách và chi tiết sản phẩm', 'product', 'view', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Sửa sản phẩm', 'product:update', 'Quyền sửa thông tin sản phẩm', 'product', 'update', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Xóa sản phẩm', 'product:delete', 'Quyền xóa sản phẩm', 'product', 'delete', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Phê duyệt sản phẩm', 'product:approve', 'Quyền phê duyệt sản phẩm', 'product', 'approve', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;

-- Order Permissions
INSERT INTO permissions (name, code, description, resource, action, created_at, updated_at) VALUES
('Tạo đơn hàng', 'order:create', 'Quyền tạo đơn hàng mới', 'order', 'create', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Xem đơn hàng', 'order:view', 'Quyền xem danh sách và chi tiết đơn hàng', 'order', 'view', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Cập nhật đơn hàng', 'order:update', 'Quyền cập nhật trạng thái đơn hàng', 'order', 'update', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Hủy đơn hàng', 'order:cancel', 'Quyền hủy đơn hàng', 'order', 'cancel', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Phê duyệt đơn hàng', 'order:approve', 'Quyền phê duyệt đơn hàng', 'order', 'approve', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Quản lý đơn hàng', 'order:manage', 'Quyền quản lý toàn bộ đơn hàng', 'order', 'manage', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;

-- User Permissions
INSERT INTO permissions (name, code, description, resource, action, created_at, updated_at) VALUES
('Xem người dùng', 'user:view', 'Quyền xem danh sách và thông tin người dùng', 'user', 'view', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Tạo người dùng', 'user:create', 'Quyền tạo tài khoản người dùng mới', 'user', 'create', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Sửa người dùng', 'user:update', 'Quyền sửa thông tin người dùng', 'user', 'update', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Xóa người dùng', 'user:delete', 'Quyền xóa tài khoản người dùng', 'user', 'delete', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Quản lý người dùng', 'user:manage', 'Quyền quản lý toàn bộ người dùng', 'user', 'manage', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;

-- Cooperative Permissions
INSERT INTO permissions (name, code, description, resource, action, created_at, updated_at) VALUES
('Xem HTX', 'cooperative:view', 'Quyền xem danh sách và thông tin HTX', 'cooperative', 'view', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Tạo HTX', 'cooperative:create', 'Quyền tạo HTX mới', 'cooperative', 'create', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Sửa HTX', 'cooperative:update', 'Quyền sửa thông tin HTX', 'cooperative', 'update', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Phê duyệt đăng ký HTX', 'cooperative:approve', 'Quyền phê duyệt đơn đăng ký HTX', 'cooperative', 'approve', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Từ chối đăng ký HTX', 'cooperative:reject', 'Quyền từ chối đơn đăng ký HTX', 'cooperative', 'reject', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Quản lý thành viên HTX', 'cooperative:manage_members', 'Quyền quản lý thành viên HTX', 'cooperative', 'manage_members', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;

-- Store Permissions
INSERT INTO permissions (name, code, description, resource, action, created_at, updated_at) VALUES
('Xem cửa hàng', 'store:view', 'Quyền xem danh sách và thông tin cửa hàng', 'store', 'view', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Tạo cửa hàng', 'store:create', 'Quyền tạo cửa hàng mới', 'store', 'create', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Sửa cửa hàng', 'store:update', 'Quyền sửa thông tin cửa hàng', 'store', 'update', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Xóa cửa hàng', 'store:delete', 'Quyền xóa cửa hàng', 'store', 'delete', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Quản lý cửa hàng', 'store:manage', 'Quyền quản lý toàn bộ cửa hàng', 'store', 'manage', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;

-- Payment Permissions
INSERT INTO permissions (name, code, description, resource, action, created_at, updated_at) VALUES
('Xem thanh toán', 'payment:view', 'Quyền xem thông tin thanh toán', 'payment', 'view', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Xử lý thanh toán', 'payment:process', 'Quyền xử lý thanh toán', 'payment', 'process', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Hoàn tiền', 'payment:refund', 'Quyền hoàn tiền', 'payment', 'refund', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;

-- Category Permissions
INSERT INTO permissions (name, code, description, resource, action, created_at, updated_at) VALUES
('Xem danh mục', 'category:view', 'Quyền xem danh sách và thông tin danh mục', 'category', 'view', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Tạo danh mục', 'category:create', 'Quyền tạo danh mục mới', 'category', 'create', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Sửa danh mục', 'category:update', 'Quyền sửa thông tin danh mục', 'category', 'update', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Xóa danh mục', 'category:delete', 'Quyền xóa danh mục', 'category', 'delete', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;

-- Role & Permission Permissions
INSERT INTO permissions (name, code, description, resource, action, created_at, updated_at) VALUES
('Quản lý Role', 'role:manage', 'Quyền tạo, sửa, xóa role', 'role', 'manage', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Quản lý Permission', 'permission:manage', 'Quyền tạo, sửa, xóa permission', 'permission', 'manage', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;

-- Warehouse Permissions
INSERT INTO permissions (name, code, description, resource, action, created_at, updated_at) VALUES
('Xem kho', 'warehouse:view', 'Quyền xem thông tin kho', 'warehouse', 'view', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Quản lý tồn kho', 'warehouse:manage_inventory', 'Quyền quản lý tồn kho', 'warehouse', 'manage_inventory', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Quản lý nhập xuất kho', 'warehouse:manage_stock', 'Quyền quản lý nhập xuất kho', 'warehouse', 'manage_stock', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;

-- Shipping Permissions
INSERT INTO permissions (name, code, description, resource, action, created_at, updated_at) VALUES
('Xem vận chuyển', 'shipping:view', 'Quyền xem thông tin vận chuyển', 'shipping', 'view', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Cập nhật trạng thái vận chuyển', 'shipping:update_status', 'Quyền cập nhật trạng thái vận chuyển', 'shipping', 'update_status', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Quản lý vận chuyển', 'shipping:manage', 'Quyền quản lý toàn bộ vận chuyển', 'shipping', 'manage', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;

-- Review Permissions
INSERT INTO permissions (name, code, description, resource, action, created_at, updated_at) VALUES
('Xem đánh giá', 'review:view', 'Quyền xem đánh giá sản phẩm', 'review', 'view', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Tạo đánh giá', 'review:create', 'Quyền tạo đánh giá sản phẩm', 'review', 'create', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Sửa đánh giá', 'review:update', 'Quyền sửa đánh giá', 'review', 'update', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Xóa đánh giá', 'review:delete', 'Quyền xóa đánh giá', 'review', 'delete', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Phê duyệt đánh giá', 'review:approve', 'Quyền phê duyệt đánh giá', 'review', 'approve', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;

-- Promotion Permissions
INSERT INTO permissions (name, code, description, resource, action, created_at, updated_at) VALUES
('Xem khuyến mãi', 'promotion:view', 'Quyền xem thông tin khuyến mãi', 'promotion', 'view', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Tạo khuyến mãi', 'promotion:create', 'Quyền tạo khuyến mãi mới', 'promotion', 'create', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Sửa khuyến mãi', 'promotion:update', 'Quyền sửa thông tin khuyến mãi', 'promotion', 'update', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Xóa khuyến mãi', 'promotion:delete', 'Quyền xóa khuyến mãi', 'promotion', 'delete', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;

-- ========== INSERT ROLES ==========

INSERT INTO roles (name, code, description, created_at, updated_at) VALUES
('Super Admin', 'SUPER_ADMIN', 'Ban chủ nhiệm HTX - quyền cao nhất, có tất cả quyền trong hệ thống', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Quản lý HTX', 'COOPERATIVE_MANAGER', 'Hợp tác xã (Chủ nhiệm hoặc Văn phòng HTX) - quản lý HTX và thành viên', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Người bán', 'SELLER', 'Người bán (Xã viên / Thành viên HTX) - quản lý cửa hàng và sản phẩm', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Khách hàng', 'CUSTOMER', 'Khách mua hàng - xem và đặt hàng', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Thành viên HTX', 'MEMBER', 'Thành viên HTX (đã được approve) - xem thông tin HTX', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Kế toán HTX', 'ACCOUNTANT', 'Kế toán HTX - quản lý tài chính và thanh toán', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Nhân viên kho', 'WAREHOUSE_STAFF', 'Nhân viên kho HTX - quản lý tồn kho', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Giao hàng', 'SHIPPER', 'Giao hàng - quản lý vận chuyển', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Duyệt nội dung', 'MODERATOR', 'Duyệt nội dung - phê duyệt sản phẩm và đánh giá', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Đối tác liên kết', 'AFFILIATE', 'Đối tác liên kết - xem và quản lý đơn hàng liên quan', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;

-- ========== ASSIGN PERMISSIONS TO ROLES ==========

-- SUPER_ADMIN: Tất cả permissions
INSERT INTO role_permissions (role_id, permission_id, created_at)
SELECT r.id, p.id, CURRENT_TIMESTAMP
FROM roles r, permissions p
WHERE r.code = 'SUPER_ADMIN'
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- COOPERATIVE_MANAGER: Quản lý HTX, thành viên, cửa hàng, sản phẩm, đơn hàng
INSERT INTO role_permissions (role_id, permission_id, created_at)
SELECT r.id, p.id, CURRENT_TIMESTAMP
FROM roles r, permissions p
WHERE r.code = 'COOPERATIVE_MANAGER'
  AND p.code IN (
    'cooperative:view', 'cooperative:update', 'cooperative:manage_members',
    'store:view', 'store:create', 'store:update', 'store:manage',
    'product:view', 'product:create', 'product:update', 'product:delete',
    'order:view', 'order:update', 'order:manage',
    'user:view',
    'payment:view', 'payment:process',
    'category:view',
    'warehouse:view', 'warehouse:manage_inventory', 'warehouse:manage_stock',
    'shipping:view', 'shipping:update_status',
    'review:view', 'review:approve',
    'promotion:view', 'promotion:create', 'promotion:update'
  )
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- SELLER: Quản lý cửa hàng, sản phẩm, đơn hàng của mình
INSERT INTO role_permissions (role_id, permission_id, created_at)
SELECT r.id, p.id, CURRENT_TIMESTAMP
FROM roles r, permissions p
WHERE r.code = 'SELLER'
  AND p.code IN (
    'store:view', 'store:update',
    'product:view', 'product:create', 'product:update', 'product:delete',
    'order:view', 'order:update',
    'category:view',
    'warehouse:view', 'warehouse:manage_inventory',
    'review:view',
    'promotion:view', 'promotion:create', 'promotion:update'
  )
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- CUSTOMER: Xem và đặt hàng
INSERT INTO role_permissions (role_id, permission_id, created_at)
SELECT r.id, p.id, CURRENT_TIMESTAMP
FROM roles r, permissions p
WHERE r.code = 'CUSTOMER'
  AND p.code IN (
    'product:view',
    'order:create', 'order:view', 'order:cancel',
    'category:view',
    'cooperative:view',
    'store:view',
    'review:view', 'review:create', 'review:update',
    'payment:view',
    'promotion:view'
  )
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- MEMBER: Thành viên HTX - xem thông tin HTX
INSERT INTO role_permissions (role_id, permission_id, created_at)
SELECT r.id, p.id, CURRENT_TIMESTAMP
FROM roles r, permissions p
WHERE r.code = 'MEMBER'
  AND p.code IN (
    'cooperative:view',
    'product:view',
    'category:view',
    'store:view',
    'order:view',
    'promotion:view'
  )
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- ACCOUNTANT: Kế toán - quản lý tài chính và thanh toán
INSERT INTO role_permissions (role_id, permission_id, created_at)
SELECT r.id, p.id, CURRENT_TIMESTAMP
FROM roles r, permissions p
WHERE r.code = 'ACCOUNTANT'
  AND p.code IN (
    'payment:view', 'payment:process', 'payment:refund',
    'order:view',
    'user:view',
    'cooperative:view',
    'store:view'
  )
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- WAREHOUSE_STAFF: Nhân viên kho - quản lý tồn kho
INSERT INTO role_permissions (role_id, permission_id, created_at)
SELECT r.id, p.id, CURRENT_TIMESTAMP
FROM roles r, permissions p
WHERE r.code = 'WAREHOUSE_STAFF'
  AND p.code IN (
    'warehouse:view', 'warehouse:manage_inventory', 'warehouse:manage_stock',
    'product:view', 'product:update',
    'order:view',
    'store:view'
  )
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- SHIPPER: Giao hàng - quản lý vận chuyển
INSERT INTO role_permissions (role_id, permission_id, created_at)
SELECT r.id, p.id, CURRENT_TIMESTAMP
FROM roles r, permissions p
WHERE r.code = 'SHIPPER'
  AND p.code IN (
    'shipping:view', 'shipping:update_status', 'shipping:manage',
    'order:view', 'order:update',
    'user:view'
  )
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- MODERATOR: Duyệt nội dung - phê duyệt sản phẩm và đánh giá
INSERT INTO role_permissions (role_id, permission_id, created_at)
SELECT r.id, p.id, CURRENT_TIMESTAMP
FROM roles r, permissions p
WHERE r.code = 'MODERATOR'
  AND p.code IN (
    'product:view', 'product:approve',
    'review:view', 'review:approve', 'review:delete',
    'store:view',
    'category:view'
  )
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- AFFILIATE: Đối tác liên kết - xem và quản lý đơn hàng liên quan
INSERT INTO role_permissions (role_id, permission_id, created_at)
SELECT r.id, p.id, CURRENT_TIMESTAMP
FROM roles r, permissions p
WHERE r.code = 'AFFILIATE'
  AND p.code IN (
    'order:view',
    'product:view',
    'store:view',
    'category:view',
    'promotion:view'
  )
ON CONFLICT (role_id, permission_id) DO NOTHING;

