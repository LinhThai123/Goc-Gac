# Danh Sách Template Email Bắt Buộc cho Keycloak

## 📋 Template Names trong Keycloak

Keycloak sử dụng các tên template sau (phải đúng chính xác):

### ✅ Đã Tạo:

1. **email-verification** (Email xác thực)
   - `html/email-verification.ftl` ✅
   - `text/email-verification.txt` hoặc `.ftl` ✅

2. **executeActions** (Execute Actions - dùng cho VERIFY_EMAIL, UPDATE_PASSWORD, etc.)
   - `html/executeActions.ftl` ✅ (vừa tạo)
   - `text/executeActions.ftl` ✅ (vừa tạo)

3. **password-reset** (Đặt lại mật khẩu)
   - `html/password-reset.ftl` ✅
   - `text/password-reset.ftl` ✅

4. **update-password** (Cập nhật mật khẩu)
   - `html/update-password.ftl` ✅
   - `text/update-password.txt` ✅

### ⚠️ Lưu Ý Quan Trọng:

- **executeActions** (camelCase, có "s") - KHÔNG phải `execute-action` (kebab-case)
- Keycloak tìm template theo tên chính xác
- Có thể dùng extension `.ftl` hoặc `.txt` cho text templates
- Nhưng `.ftl` được khuyến nghị vì hỗ trợ FreeMarker syntax

## 🔍 Cách Kiểm Tra Template Name

Nếu gặp lỗi `Template not found for name "xxx"`, đó chính là tên template cần tạo.

## 📝 Template Mapping

| Keycloak Action | Template Name | File Path |
|----------------|---------------|-----------|
| VERIFY_EMAIL | executeActions | `html/executeActions.ftl`<br>`text/executeActions.ftl` |
| UPDATE_PASSWORD | executeActions | `html/executeActions.ftl`<br>`text/executeActions.ftl` |
| Password Reset | password-reset | `html/password-reset.ftl`<br>`text/password-reset.ftl` |
| Update Password (notification) | update-password | `html/update-password.ftl`<br>`text/update-password.txt` |

## 🎯 Giải Pháp Cho Lỗi Hiện Tại

Lỗi: `Template not found for name "text/executeActions.ftl"`

**Đã fix:** ✅ Đã tạo file `text/executeActions.ftl`

**Cần làm:**
1. Restart Keycloak để load template mới
2. Test lại gửi email verification

