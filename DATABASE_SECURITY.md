# Cấu hình Bảo mật Database cho Keycloak và Gocgac

## Vấn đề

Khi Keycloak và Gocgac dùng chung username và password cho database, sẽ có các vấn đề:

1. **Bảo mật kém**: Nếu một service bị xâm nhập, attacker có thể truy cập cả hai database
2. **Khó quản lý**: Không thể kiểm soát quyền truy cập riêng biệt
3. **Vi phạm nguyên tắc least privilege**: Mỗi service nên chỉ có quyền tối thiểu cần thiết

## Giải pháp đã triển khai

### 1. Tách User riêng cho Keycloak

- **Gocgac database**: Dùng user `postgres` với password `gocgac123`
- **Keycloak database**: Dùng user `keycloak_user` với password `keycloak_password_123`

### 2. Phân quyền

- User `keycloak_user` chỉ có quyền trên database `keycloak`
- User `postgres` chỉ có quyền trên database `gocgac`
- Mỗi user không thể truy cập database của service khác

## Cấu trúc Database

```
PostgreSQL Instance
├── Database: gocgac
│   └── User: postgres (password: gocgac123)
│       └── Quyền: Full access trên database gocgac
│
└── Database: keycloak
    └── User: keycloak_user (password: keycloak_password_123)
        └── Quyền: Full access trên database keycloak
```

## Lợi ích

1. **Bảo mật tốt hơn**: Tách biệt quyền truy cập
2. **Dễ quản lý**: Có thể thay đổi password từng service độc lập
3. **Tuân thủ best practices**: Mỗi service có user riêng
4. **Audit tốt hơn**: Có thể theo dõi hoạt động của từng service

## Cấu hình trong docker-compose.yml

### Gocgac (Application)
```yaml
postgres:
  environment:
    POSTGRES_DB: gocgac
    POSTGRES_USER: postgres
    POSTGRES_PASSWORD: gocgac123
```

### Keycloak
```yaml
keycloak:
  environment:
    KC_DB_URL: jdbc:postgresql://postgres:5432/keycloak
    KC_DB_USERNAME: keycloak_user
    KC_DB_PASSWORD: keycloak_password_123
```

## Lưu ý cho Production

Trong môi trường production, nên:

1. **Sử dụng secrets management**:
   - Dùng Docker secrets hoặc environment variables từ file .env
   - Không hardcode password trong docker-compose.yml

2. **Tạo file .env**:
   ```env
   POSTGRES_PASSWORD=gocgac_secure_password_here
   KEYCLOAK_DB_PASSWORD=keycloak_secure_password_here
   ```

3. **Cập nhật docker-compose.yml** để dùng .env:
   ```yaml
   environment:
     POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
     KC_DB_PASSWORD: ${KEYCLOAK_DB_PASSWORD}
   ```

4. **Thêm .env vào .gitignore**:
   ```
   .env
   ```

5. **Sử dụng password mạnh**:
   - Ít nhất 16 ký tự
   - Bao gồm chữ hoa, chữ thường, số và ký tự đặc biệt
   - Không dùng từ điển hoặc thông tin cá nhân

## Khởi tạo lại Database

Nếu cần khởi tạo lại với cấu hình mới:

```bash
# Dừng containers
docker-compose down

# Xóa volumes (CẢNH BÁO: Mất dữ liệu)
docker volume rm gocgac_postgres_data

# Khởi động lại
docker-compose up -d
```

Script `01-create-keycloak-db.sh` sẽ tự động:
- Tạo database `keycloak`
- Tạo user `keycloak_user`
- Cấp quyền phù hợp

