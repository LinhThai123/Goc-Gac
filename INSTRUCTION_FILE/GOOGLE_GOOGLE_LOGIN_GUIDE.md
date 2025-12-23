# Hướng dẫn đăng nhập bằng Google (API)

Tài liệu này giúp bạn test đăng nhập Google qua Swagger (không cần frontend).

## 1. Chuẩn bị cấu hình
- Lấy `GOOGLE_CLIENT_ID` từ Google Cloud Console → APIs & Services → Credentials → OAuth 2.0 Client ID (Web).
- Tạo chuỗi bí mật mạnh cho `SOCIAL_PASSWORD_SECRET` (tối thiểu 32 ký tự ngẫu nhiên).
- Thiết lập biến môi trường trước khi chạy ứng dụng:
  - PowerShell:
    - `$env:GOOGLE_CLIENT_ID="your-client-id.apps.googleusercontent.com"`
    - `$env:SOCIAL_PASSWORD_SECRET="your-strong-secret"`
  - Hoặc đặt vào cấu hình triển khai (Docker/K8s/CI). Không commit secret vào git.

## 2. Lấy Google ID token (dùng OAuth Playground)
1) Mở https://developers.google.com/oauthplayground  
2) Gốc phải, icon bánh răng → chọn “Use your own OAuth credentials”, điền `GOOGLE_CLIENT_ID` và client secret.  
3) Chọn scope: `openid email profile`.  
4) Bấm **Authorize APIs**, đăng nhập Google.  
5) Bấm **Exchange authorization code for tokens**.  
6) Trong kết quả bên phải sẽ có `id_token`. Copy giá trị này để gọi API.

Lưu ý: ID token có hạn; cần lấy mới khi hết hạn.

## 3. Gọi API qua Swagger
- Mở Swagger UI (ví dụ: `http://localhost:8086/swagger-ui.html`).
- Chọn endpoint `POST /api/auth/login/google`.
- Body JSON:
  ```json
  {
    "idToken": "<id_token_vua_lay>"
  }
  ```
- Execute → nhận `accessToken`, `refreshToken`, `expiresIn`, thông tin user.

## 4. Một số lưu ý
- Backend sẽ gọi `https://oauth2.googleapis.com/tokeninfo` để kiểm tra `id_token`; không thể dùng token giả.
- Nếu email đã tồn tại dưới dạng tài khoản mật khẩu, API sẽ trả lỗi để tránh ghi đè; cần quy trình liên kết nếu muốn hợp nhất tài khoản.
- Role mặc định: CUSTOMER. Nếu cần role khác, chỉnh ở `AuthService.resolveDefaultRole`.

## 5. Khi chuyển môi trường
- Prod: đặt biến môi trường thay vì chỉnh file YAML; đảm bảo `GOOGLE_CLIENT_ID` khớp client tạo cho domain prod.
- Đổi `SOCIAL_PASSWORD_SECRET` khác giữa các môi trường để tránh reuse mật khẩu nội bộ.

