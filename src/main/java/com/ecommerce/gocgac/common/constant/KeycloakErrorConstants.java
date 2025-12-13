package com.ecommerce.gocgac.common.constant;

/**
 * Constants cho các error messages của Keycloak
 */
public class KeycloakErrorConstants {
    
    // Login errors
    public static final String LOGIN_INVALID_CREDENTIALS = "Email hoặc mật khẩu không đúng";
    public static final String LOGIN_FAILED = "Đăng nhập thất bại";
    public static final String LOGIN_NO_TOKEN = "Không nhận được token từ Keycloak";
    
    // Registration errors
    public static final String REGISTER_EMAIL_EXISTS = "Email đã được sử dụng trong Keycloak";
    public static final String REGISTER_NO_PERMISSION = "Không có quyền tạo user trong Keycloak.";
    public static final String REGISTER_FAILED = "Đăng ký thất bại";
    public static final String REGISTER_CANNOT_CREATE_USER = "Không thể tạo user trong Keycloak. Status: %s";
    public static final String REGISTER_CANNOT_ASSIGN_ROLE = "Không thể gán role cho user. Vui lòng thử lại hoặc liên hệ quản trị viên.";
    
    // Admin token errors
    public static final String ADMIN_TOKEN_FAILED = "Không thể lấy admin token";
    public static final String ADMIN_TOKEN_NO_CREDENTIALS = "Không thể lấy admin token.";
    public static final String ADMIN_TOKEN_MASTER_LOGIN_FAILED = "Không thể đăng nhập vào Keycloak Admin với username='%s'. ";
    public static final String ADMIN_TOKEN_NO_ACCESS_TOKEN = "Không nhận được access token từ %s";
    public static final String ADMIN_TOKEN_SERVICE_ACCOUNT_FAILED = "Không thể lấy admin token từ service account";
    
    // User management errors
    public static final String USER_CANNOT_SET_PASSWORD = "Không thể set password cho user";
    public static final String USER_CANNOT_DELETE = "Không thể xóa user trong Keycloak";
    public static final String USER_NOT_FOUND = "User không tồn tại";
    
    // Role assignment errors
    public static final String ROLE_CANNOT_ASSIGN = "Không thể gán role %s cho user";
    public static final String ROLE_CLIENT_NOT_EXISTS = "Client role %s không tồn tại trong client %s";
    public static final String ROLE_REALM_NOT_EXISTS = "Realm role %s không tồn tại";
    public static final String ROLE_CANNOT_CREATE = "Không thể tạo role %s";
    public static final String ROLE_ALREADY_EXISTS = "Role %s đã tồn tại trong Keycloak realm";
    
    // Client errors
    public static final String CLIENT_NOT_FOUND = "Không tìm thấy client %s";
    public static final String CLIENT_CANNOT_GET_ID = "Không thể lấy client ID";
    
    // Token refresh errors
    public static final String REFRESH_TOKEN_FAILED = "Không thể refresh token";
    public static final String REFRESH_TOKEN_INVALID = "Refresh token không hợp lệ hoặc đã hết hạn";
    
    // Private constructor để prevent instantiation
    private KeycloakErrorConstants() {
        throw new UnsupportedOperationException("Constants class cannot be instantiated");
    }
}

