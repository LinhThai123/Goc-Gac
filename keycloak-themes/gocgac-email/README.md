# GocGac Email Theme cho Keycloak

Custom email theme cho Keycloak với branding và nội dung tiếng Việt cho GocGac.

## 📁 Cấu Trúc Thư Mục

```
gocgac-email/
├── theme.properties
├── email/
│   ├── html/
│   │   ├── template.ftl              # Layout chính (header + footer)
│   │   ├── email-verification.ftl    # Email xác thực
│   │   ├── executeActions.ftl       # Email hành động (VERIFY_EMAIL, etc.)
│   │   ├── password-reset.ftl       # Email đặt lại mật khẩu
│   │   └── update-password.ftl      # Email cập nhật mật khẩu
│   ├── text/
│   │   ├── email-verification.txt   # Plain text version
│   │   ├── executeActions.ftl       # Plain text execute actions
│   │   ├── password-reset.ftl       # Plain text password reset
│   │   └── update-password.txt     # Plain text update password
│   └── messages/
│       ├── messages.properties       # Tiếng Anh
│       └── messages_vi.properties    # Tiếng Việt
└── resources/
    └── img/
        └── Logo.png                  # Logo GocGac
```

## 🚀 Cài Đặt

### Cách 1: Docker (Khuyến nghị)

1. **Mount theme vào container:**

```yaml
# docker-compose.yml
services:
  keycloak:
    image: quay.io/keycloak/keycloak:latest
    volumes:
      - ./keycloak-themes/gocgac-email:/opt/keycloak/themes/gocgac-email
    environment:
      - KC_SPI_THEME_STATIC_MAX_AGE=-1
      - KC_SPI_THEME_CACHE_THEMES=false
      - KC_SPI_THEME_CACHE_TEMPLATES=false
```

2. **Restart Keycloak:**
```bash
docker-compose restart keycloak
```

### Cách 2: Standalone Keycloak

1. **Copy theme vào thư mục themes:**
```bash
cp -r keycloak-themes/gocgac-email /opt/keycloak/themes/
```

2. **Restart Keycloak:**
```bash
/opt/keycloak/bin/kc.sh restart
```

### Cách 3: Development Mode (Không cần restart)

```bash
./kc.sh start-dev \
  --spi-theme-static-max-age=-1 \
  --spi-theme-cache-themes=false \
  --spi-theme-cache-templates=false
```

## ⚙️ Cấu Hình trong Keycloak Admin Console

1. **Đăng nhập Keycloak Admin Console:**
   - URL: `http://localhost:8080`
   - Username: `admin`
   - Password: `admin123`

2. **Chọn Realm:**
   - Dropdown góc trên bên trái → Chọn `gocgac-htx`

3. **Cấu hình Email Theme:**
   - Vào **Realm Settings** → **Themes**
   - **Email Theme**: Chọn `gocgac-email`
   - Click **Save**

4. **Cấu hình Locale (Tiếng Việt):**
   - Vào **Realm Settings** → **Localization**
   - **Supported locales**: Thêm `vi` (Vietnamese)
   - **Default locale**: Có thể set `vi` nếu muốn
   - Click **Save**

## 🧪 Testing

1. **Tạo user test:**
   - Vào **Users** → **Add user**
   - Điền thông tin và tạo user

2. **Gửi email verification:**
   - Vào user detail → **Send email verification**
   - Hoặc dùng API: `POST /admin/realms/{realm}/users/{userId}/execute-actions-email`

3. **Kiểm tra email:**
   - Kiểm tra inbox của email đã đăng ký
   - Xác nhận template đã được áp dụng đúng

## 📝 Customization

### Thay đổi nội dung email:

1. **Tiếng Việt:** Sửa file `email/messages/messages_vi.properties`
2. **Tiếng Anh:** Sửa file `email/messages/messages.properties`
3. **HTML Template:** Sửa file `email/html/*.ftl`
4. **Plain Text:** Sửa file `email/text/*.txt`

### Thêm logo:

1. Thêm file logo vào `resources/img/logo.png`
2. Logo sẽ tự động hiển thị trong email (nếu dùng `${url.resourcesPath}`)
3. Hoặc dùng URL tuyệt đối: `https://gocgac.vn/assets/logo.png`

### Thêm template mới:

1. Tạo file `.ftl` trong `email/html/`
2. Tạo file `.txt` trong `email/text/` (nếu cần)
3. Thêm messages vào `messages/messages.properties` và `messages_vi.properties`

## 🔧 Troubleshooting

### Theme không hiển thị:

1. **Kiểm tra thư mục:**
   - Đảm bảo theme nằm đúng vị trí: `/opt/keycloak/themes/gocgac-email`
   - Kiểm tra file `theme.properties` có tồn tại

2. **Clear cache:**
   - Restart Keycloak
   - Hoặc dùng dev mode với cache disabled

3. **Kiểm tra log:**
   - Xem log Keycloak để tìm lỗi
   - `docker logs keycloak` (nếu dùng Docker)

### Email không gửi được:

1. **Kiểm tra SMTP config:**
   - Vào **Realm Settings** → **Email**
   - Test connection
   - Kiểm tra credentials

2. **Kiểm tra email template:**
   - Đảm bảo template không có lỗi syntax
   - Kiểm tra các biến `${variable}` có đúng không

## 📚 Tài Liệu Tham Khảo

- [Keycloak Themes Documentation](https://www.keycloak.org/docs/latest/server_development/#_themes)
- [FreeMarker Template Language](https://freemarker.apache.org/docs/)
- [Keycloak Email Templates](https://www.keycloak.org/docs/latest/server_admin/#_email)

## 📄 License

Copyright © 2024 GocGac. All rights reserved.

