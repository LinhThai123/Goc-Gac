# Hướng Dẫn Cấu Hình Email Template trong Keycloak

## 📋 Tổng Quan

Keycloak cho phép customize email templates để thay đổi nội dung email verification, password reset, và các email khác. Có 2 cách để cấu hình:

1. **Qua Keycloak Admin Console (UI)** - Dễ dàng, trực quan
2. **Qua Keycloak Admin REST API** - Tự động hóa, có thể version control

---

## 🎨 Cách 1: Cấu Hình Qua Keycloak Admin Console (UI)

### Bước 1: Truy cập Email Templates

1. **Đăng nhập Keycloak Admin Console**
   - URL: `http://localhost:8080`
   - Username: `admin`
   - Password: `admin123`

2. **Chọn Realm**
   - Dropdown góc trên bên trái → Chọn `gocgac-htx`

3. **Vào Email Templates**
   - Menu bên trái: **Realm Settings** → **Email** → Tab **Email Templates**
   - Hoặc: **Realm Settings** → **Themes** → Tab **Email**

### Bước 2: Chọn Template Cần Customize

**Các template có sẵn:**
- **Verify Email** - Email xác thực tài khoản
- **Password Reset** - Email đặt lại mật khẩu
- **Update Password** - Email cập nhật mật khẩu
- **Update Email** - Email cập nhật email
- **Test Email** - Email test
- **Welcome Email** - Email chào mừng
- **Account Disabled** - Email tài khoản bị vô hiệu hóa
- **Account Enabled** - Email tài khoản được kích hoạt

### Bước 3: Customize Template "Verify Email"

1. **Chọn "Verify Email"** từ dropdown
2. **Chọn Locale** (ví dụ: `vi` cho tiếng Việt, `en` cho tiếng Anh)
3. **Click "Create"** để tạo template mới hoặc **"Edit"** để sửa template hiện có

### Bước 4: Chỉnh Sửa Nội Dung

**Subject (Tiêu đề email):**
```
Xác thực email tài khoản GocGac của bạn
```

**Body Text (Nội dung email - Plain Text):**
```
Chào mừng bạn đến với GocGac!

Vui lòng click vào link sau để xác thực email của bạn:

${verificationLink}

Link này sẽ hết hạn sau 24 giờ.

Nếu bạn không yêu cầu tạo tài khoản này, vui lòng bỏ qua email này.

Trân trọng,
Đội ngũ GocGac
```

**Body HTML (Nội dung email - HTML):**
```html
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <style>
        body {
            font-family: Arial, sans-serif;
            line-height: 1.6;
            color: #333;
            max-width: 600px;
            margin: 0 auto;
            padding: 20px;
        }
        .header {
            background-color: #4CAF50;
            color: white;
            padding: 20px;
            text-align: center;
            border-radius: 5px 5px 0 0;
        }
        .content {
            background-color: #f9f9f9;
            padding: 30px;
            border-radius: 0 0 5px 5px;
        }
        .button {
            display: inline-block;
            padding: 12px 30px;
            background-color: #4CAF50;
            color: white;
            text-decoration: none;
            border-radius: 5px;
            margin: 20px 0;
        }
        .footer {
            margin-top: 30px;
            padding-top: 20px;
            border-top: 1px solid #ddd;
            font-size: 12px;
            color: #666;
        }
    </style>
</head>
<body>
    <div class="header">
        <h1>Chào mừng đến với GocGac!</h1>
    </div>
    <div class="content">
        <p>Xin chào,</p>
        <p>Cảm ơn bạn đã đăng ký tài khoản tại GocGac. Để hoàn tất đăng ký, vui lòng xác thực email của bạn bằng cách click vào nút bên dưới:</p>
        
        <div style="text-align: center;">
            <a href="${verificationLink}" class="button">Xác thực Email</a>
        </div>
        
        <p>Hoặc copy và paste link sau vào trình duyệt:</p>
        <p style="word-break: break-all; color: #4CAF50;">${verificationLink}</p>
        
        <p><strong>Lưu ý:</strong> Link này sẽ hết hạn sau 24 giờ.</p>
        
        <p>Nếu bạn không yêu cầu tạo tài khoản này, vui lòng bỏ qua email này.</p>
        
        <div class="footer">
            <p>Trân trọng,<br>Đội ngũ GocGac</p>
            <p>Email này được gửi tự động, vui lòng không trả lời email này.</p>
        </div>
    </div>
</body>
</html>
```

### Bước 5: Lưu Template

1. Click **"Save"** để lưu template
2. Test bằng cách gửi email verification cho một user test

### Bước 6: Test Template

1. Vào **Realm Settings** → **Email** → Click **"Test connection"**
2. Hoặc đăng ký user mới và kiểm tra email nhận được

---

## 🔧 Cách 2: Cấu Hình Qua Keycloak Admin REST API

### Tạo Service Quản Lý Email Templates

Tôi sẽ tạo một service để quản lý email templates qua API, cho phép:
- Lấy template hiện tại
- Cập nhật template
- Tạo template mới
- Xóa template (restore về default)

### API Endpoints của Keycloak

**Lấy template:**
```
GET /admin/realms/{realm}/email-templates/{template-name}
```

**Cập nhật template:**
```
PUT /admin/realms/{realm}/email-templates/{template-name}
```

**Lấy tất cả templates:**
```
GET /admin/realms/{realm}/email-templates
```

---

## 📝 Các Biến Có Sẵn trong Template

Keycloak cung cấp các biến có thể dùng trong template:

### Verify Email Template:
- `${verificationLink}` - Link xác thực email
- `${user.firstName}` - Tên user
- `${user.lastName}` - Họ user
- `${user.email}` - Email user
- `${user.username}` - Username
- `${realmName}` - Tên realm
- `${linkExpiration}` - Thời gian hết hạn link

### Password Reset Template:
- `${link}` - Link đặt lại mật khẩu
- `${user.firstName}` - Tên user
- `${user.email}` - Email user
- `${realmName}` - Tên realm

### Các biến khác:
- `${clientId}` - Client ID
- `${clientName}` - Client name
- `${baseUrl}` - Base URL của Keycloak
- `${timestamp}` - Timestamp

---

## 🎯 Ví Dụ Template Tiếng Việt Đầy Đủ

### Verify Email - Subject:
```
Xác thực email tài khoản GocGac
```

### Verify Email - HTML Body:
```html
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background-color: #f4f4f4;
            padding: 20px;
        }
        .email-container {
            max-width: 600px;
            margin: 0 auto;
            background-color: #ffffff;
            border-radius: 10px;
            overflow: hidden;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }
        .header {
            background: linear-gradient(135deg, #4CAF50 0%, #45a049 100%);
            color: white;
            padding: 40px 30px;
            text-align: center;
        }
        .header h1 {
            font-size: 28px;
            margin-bottom: 10px;
        }
        .header p {
            font-size: 16px;
            opacity: 0.9;
        }
        .content {
            padding: 40px 30px;
            color: #333;
        }
        .content h2 {
            color: #4CAF50;
            margin-bottom: 20px;
            font-size: 22px;
        }
        .content p {
            margin-bottom: 15px;
            line-height: 1.6;
            font-size: 16px;
        }
        .button-container {
            text-align: center;
            margin: 30px 0;
        }
        .button {
            display: inline-block;
            padding: 15px 40px;
            background-color: #4CAF50;
            color: white;
            text-decoration: none;
            border-radius: 5px;
            font-weight: bold;
            font-size: 16px;
            transition: background-color 0.3s;
        }
        .button:hover {
            background-color: #45a049;
        }
        .link-text {
            margin-top: 20px;
            padding: 15px;
            background-color: #f9f9f9;
            border-left: 4px solid #4CAF50;
            word-break: break-all;
            font-size: 14px;
            color: #666;
        }
        .warning {
            margin-top: 20px;
            padding: 15px;
            background-color: #fff3cd;
            border-left: 4px solid #ffc107;
            color: #856404;
            font-size: 14px;
        }
        .footer {
            background-color: #f9f9f9;
            padding: 30px;
            text-align: center;
            color: #666;
            font-size: 14px;
            border-top: 1px solid #e0e0e0;
        }
        .footer p {
            margin-bottom: 10px;
        }
        .logo {
            font-size: 32px;
            font-weight: bold;
            margin-bottom: 10px;
        }
    </style>
</head>
<body>
    <div class="email-container">
        <div class="header">
            <div class="logo">🌾 GocGac</div>
            <h1>Chào mừng bạn đến với GocGac!</h1>
            <p>Nền tảng thương mại điện tử cho Hợp Tác Xã</p>
        </div>
        
        <div class="content">
            <h2>Xác thực email của bạn</h2>
            
            <p>Xin chào <strong>${user.firstName}</strong>,</p>
            
            <p>Cảm ơn bạn đã đăng ký tài khoản tại <strong>GocGac</strong>. Để bắt đầu sử dụng dịch vụ, vui lòng xác thực địa chỉ email của bạn.</p>
            
            <div class="button-container">
                <a href="${verificationLink}" class="button">Xác thực Email Ngay</a>
            </div>
            
            <p>Hoặc copy và paste link sau vào trình duyệt của bạn:</p>
            <div class="link-text">
                ${verificationLink}
            </div>
            
            <div class="warning">
                <strong>⚠️ Lưu ý quan trọng:</strong><br>
                • Link xác thực sẽ hết hạn sau <strong>24 giờ</strong><br>
                • Nếu bạn không yêu cầu tạo tài khoản này, vui lòng bỏ qua email này<br>
                • Không chia sẻ link xác thực với bất kỳ ai
            </div>
            
            <p style="margin-top: 30px;">Sau khi xác thực email, bạn sẽ có thể:</p>
            <ul style="margin-left: 20px; line-height: 2;">
                <li>Mua sắm các sản phẩm từ Hợp Tác Xã</li>
                <li>Đăng ký trở thành thành viên HTX</li>
                <li>Tham gia các chương trình khuyến mãi</li>
                <li>Nhận thông báo về đơn hàng và cập nhật</li>
            </ul>
        </div>
        
        <div class="footer">
            <p><strong>GocGac</strong> - Nền tảng thương mại điện tử cho Hợp Tác Xã</p>
            <p>Email này được gửi tự động, vui lòng không trả lời email này.</p>
            <p>Nếu bạn có thắc mắc, vui lòng liên hệ: <a href="mailto:support@gocgac.com" style="color: #4CAF50;">support@gocgac.com</a></p>
            <p style="margin-top: 20px; font-size: 12px; color: #999;">
                © 2024 GocGac. All rights reserved.
            </p>
        </div>
    </div>
</body>
</html>
```

### Verify Email - Plain Text Body:
```
Chào mừng bạn đến với GocGac!

Xin chào ${user.firstName},

Cảm ơn bạn đã đăng ký tài khoản tại GocGac. Để bắt đầu sử dụng dịch vụ, vui lòng xác thực địa chỉ email của bạn.

Link xác thực:
${verificationLink}

Lưu ý quan trọng:
- Link xác thực sẽ hết hạn sau 24 giờ
- Nếu bạn không yêu cầu tạo tài khoản này, vui lòng bỏ qua email này
- Không chia sẻ link xác thực với bất kỳ ai

Sau khi xác thực email, bạn sẽ có thể:
- Mua sắm các sản phẩm từ Hợp Tác Xã
- Đăng ký trở thành thành viên HTX
- Tham gia các chương trình khuyến mãi
- Nhận thông báo về đơn hàng và cập nhật

Trân trọng,
Đội ngũ GocGac

---
Email này được gửi tự động, vui lòng không trả lời email này.
Nếu bạn có thắc mắc, vui lòng liên hệ: support@gocgac.com

© 2024 GocGac. All rights reserved.
```

---

## 🔄 Cập Nhật Template Qua Code (Java)

Nếu bạn muốn tự động hóa việc cập nhật template, có thể tạo một service để quản lý templates qua Keycloak Admin REST API.

### Ví dụ Service:

```java
@Service
@RequiredArgsConstructor
public class KeycloakEmailTemplateService {
    
    private final KeycloakClient keycloakClient;
    
    public void updateVerifyEmailTemplate(String subject, String htmlBody, String textBody) {
        // Implementation để update template qua Keycloak Admin API
    }
}
```

---

## 📌 Lưu Ý Quan Trọng

1. **Locale Support:**
   - Có thể tạo template cho nhiều ngôn ngữ (vi, en, etc.)
   - Keycloak sẽ tự động chọn template phù hợp với locale của user

2. **Template Variables:**
   - Luôn sử dụng `${variableName}` format
   - Các biến phải match với template type

3. **HTML vs Plain Text:**
   - Nên cung cấp cả HTML và Plain Text version
   - Plain Text là fallback nếu email client không support HTML

4. **Testing:**
   - Luôn test template sau khi cập nhật
   - Kiểm tra trên nhiều email client khác nhau (Gmail, Outlook, etc.)

5. **Backup:**
   - Nên backup template mặc định trước khi chỉnh sửa
   - Có thể restore về default nếu cần

---

## 🎨 Best Practices

1. **Design:**
   - Sử dụng responsive design cho mobile
   - Màu sắc phù hợp với brand
   - Font size dễ đọc (tối thiểu 14px)

2. **Content:**
   - Rõ ràng, dễ hiểu
   - Call-to-action nổi bật
   - Thông tin liên hệ đầy đủ

3. **Security:**
   - Không bao gồm thông tin nhạy cảm
   - Cảnh báo về link hết hạn
   - Hướng dẫn bảo mật

4. **Accessibility:**
   - Alt text cho images
   - Contrast ratio đủ
   - Font size phù hợp

---

## 📚 Tài Liệu Tham Khảo

- [Keycloak Email Templates Documentation](https://www.keycloak.org/docs/latest/server_admin/#_email)
- [Keycloak Admin REST API - Email Templates](https://www.keycloak.org/docs-api/latest/rest-api/index.html#_email_templates_resource)

---

**Chúc bạn cấu hình thành công! 🎉**

