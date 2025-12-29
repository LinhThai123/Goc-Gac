package com.ecommerce.gocgac.entity.enums;

/**
 * Enum cho vai trò của thành viên trong HTX
 */
public enum CooperativeMemberRole {
    MEMBER,              // Thành viên thường (mặc định)
    MANAGER,             // Quản lý (phó chủ nhiệm, trưởng phòng, etc.)
    ACCOUNTANT,          // Kế toán HTX
    WAREHOUSE_STAFF,     // Nhân viên kho HTX
    SELLER,              // Nhân viên bán hàng (seller) - nhất quán với UserType.SELLER
    SHIPPER,             // Nhân viên giao hàng
    MODERATOR,           // Nhân viên duyệt nội dung
    SECRETARY,           // Thư ký HTX
    OTHER                // Vai trò khác
}

