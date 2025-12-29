# Hướng Dẫn Deploy GocGac Email Theme

## 🐳 Deploy với Docker

### Option 1: Volume Mount (Development)

```yaml
# docker-compose.yml
version: '3.8'

services:
  keycloak:
    image: quay.io/keycloak/keycloak:latest
    container_name: keycloak
    command: start-dev
    environment:
      - KEYCLOAK_ADMIN=admin
      - KEYCLOAK_ADMIN_PASSWORD=admin123
      - KC_DB=postgres
      - KC_DB_URL=jdbc:postgresql://postgres:5432/keycloak
      - KC_DB_USERNAME=keycloak
      - KC_DB_PASSWORD=keycloak
      # Disable cache for development
      - KC_SPI_THEME_STATIC_MAX_AGE=-1
      - KC_SPI_THEME_CACHE_THEMES=false
      - KC_SPI_THEME_CACHE_TEMPLATES=false
    ports:
      - "8080:8080"
    volumes:
      # Mount theme directory
      - ./gocgac-email:/opt/keycloak/themes/gocgac-email
    depends_on:
      - postgres

  postgres:
    image: postgres:15
    environment:
      - POSTGRES_DB=keycloak
      - POSTGRES_USER=keycloak
      - POSTGRES_PASSWORD=keycloak
    volumes:
      - postgres_data:/var/lib/postgresql/data

volumes:
  postgres_data:
```

### Option 2: Build Custom Image (Production)

```dockerfile
# Dockerfile
FROM quay.io/keycloak/keycloak:latest

# Copy theme
COPY gocgac-email /opt/keycloak/themes/gocgac-email

# Build image
# docker build -t keycloak-gocgac:latest .
```

## 🖥️ Deploy với Standalone Keycloak

### Bước 1: Copy Theme

```bash
# Copy theme vào thư mục themes
cp -r keycloak-themes/gocgac-email /opt/keycloak/themes/

# Đảm bảo quyền đúng
chown -R keycloak:keycloak /opt/keycloak/themes/gocgac-email
chmod -R 755 /opt/keycloak/themes/gocgac-email
```

### Bước 2: Restart Keycloak

```bash
# Systemd
sudo systemctl restart keycloak

# Hoặc manual
/opt/keycloak/bin/kc.sh restart
```

## 🔧 Cấu Hình trong Keycloak

### 1. Set Email Theme

1. Đăng nhập Keycloak Admin Console
2. Chọn Realm `gocgac-htx`
3. Vào **Realm Settings** → **Themes**
4. **Email Theme**: Chọn `gocgac-email`
5. Click **Save**

### 2. Enable Vietnamese Locale

1. Vào **Realm Settings** → **Localization**
2. **Supported locales**: Thêm `vi`
3. **Default locale**: Có thể set `vi` (optional)
4. Click **Save**

### 3. Cấu Hình SMTP (nếu chưa có)

1. Vào **Realm Settings** → **Email**
2. Cấu hình SMTP settings
3. Test connection
4. Click **Save**

## ✅ Verify Installation

1. **Kiểm tra theme có trong list:**
   - Vào **Realm Settings** → **Themes**
   - Xem `gocgac-email` có trong dropdown **Email Theme**

2. **Test email:**
   - Tạo user mới hoặc gửi email verification
   - Kiểm tra email nhận được có đúng template không

3. **Kiểm tra log:**
   ```bash
   # Docker
   docker logs keycloak | grep -i theme
   
   # Standalone
   tail -f /opt/keycloak/data/log/keycloak.log | grep -i theme
   ```

## 🚨 Troubleshooting

### Theme không xuất hiện trong dropdown:

1. **Kiểm tra file theme.properties:**
   ```bash
   cat /opt/keycloak/themes/gocgac-email/theme.properties
   ```

2. **Kiểm tra cấu trúc thư mục:**
   ```bash
   ls -la /opt/keycloak/themes/gocgac-email/
   ```

3. **Restart Keycloak:**
   ```bash
   docker-compose restart keycloak
   # hoặc
   systemctl restart keycloak
   ```

### Email vẫn dùng template mặc định:

1. **Clear cache:**
   - Restart Keycloak
   - Hoặc dùng dev mode với cache disabled

2. **Kiểm tra realm settings:**
   - Đảm bảo Email Theme đã được set đúng

3. **Kiểm tra template files:**
   - Đảm bảo các file `.ftl` không có lỗi syntax

### Lỗi biến không hiển thị:

1. **Kiểm tra messages files:**
   - Đảm bảo key tồn tại trong `messages.properties`
   - Kiểm tra format: `${msg("key")}`

2. **Kiểm tra locale:**
   - Đảm bảo locale được set đúng cho user/realm

## 📝 Notes

- **Development:** Dùng volume mount để edit trực tiếp
- **Production:** Build custom image hoặc copy vào container
- **Cache:** Disable cache trong development để test nhanh
- **Logo:** Thêm logo vào `resources/img/logo.png` hoặc dùng URL tuyệt đối

