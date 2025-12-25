package com.ecommerce.gocgac.entity.enums;

public enum UserType {
    SUPER_ADMIN,           // Ban chủ nhiệm HTX - quyền cao nhất
    COOPERATIVE_MANAGER,   // Hợp tác xã (Chủ nhiệm hoặc Văn phòng HTX)
    SELLER,                // Người bán (Xã viên / Thành viên HTX)
    CUSTOMER,              // Khách mua hàng
    MEMBER,                // Thành viên HTX (đã được approve)
    ACCOUNTANT,            // Kế toán HTX
    WAREHOUSE_STAFF,       // Nhân viên kho HTX
    SHIPPER,               // Giao hàng
    MODERATOR,             // Duyệt nội dung
    AFFILIATE              // Đối tác liên kết (giữ lại cho tương thích)
}

