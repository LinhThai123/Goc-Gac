# Hướng Dẫn Phòng Chống XSS (Cross-Site Scripting)

## Tổng Quan

XSS (Cross-Site Scripting) là một lỗ hổng bảo mật phổ biến cho phép kẻ tấn công chèn các đoạn script độc hại vào trang web. Hệ thống đã được tích hợp các biện pháp phòng chống XSS toàn diện ở nhiều lớp khác nhau.

## Kiến Trúc Bảo Mật XSS

Hệ thống sử dụng **defense in depth** (bảo vệ nhiều lớp) với các thành phần sau:

1. **XssSanitizer** - Utility class để sanitize input
2. **XssFilter** - Filter tự động sanitize request parameters
3. **XssRequestWrapper** - Wrapper để override request parameters
4. **SecurityHeadersFilter** - Thêm security headers vào response
5. **@Sanitize Annotation** - Annotation để đánh dấu fields cần sanitize

---

## 1. XssSanitizer - Utility Class

### Vị Trí
`src/main/java/com/ecommerce/gocgac/common/util/XssSanitizer.java`

### Chức Năng
Class utility cung cấp các phương thức để sanitize và kiểm tra XSS patterns.

### Các Phương Thức Chính

#### `sanitize(String input)`
Sanitize string input - loại bỏ tất cả HTML tags và script. Sử dụng cho các trường text thông thường.

```java
String userInput = "<script>alert('XSS')</script>Hello";
String safe = XssSanitizer.sanitize(userInput);
// Kết quả: "Hello" (đã loại bỏ script tag)
```

#### `sanitizeHtml(String input)`
Sanitize HTML input - cho phép một số HTML tags an toàn. Sử dụng cho các trường rich text editor.

```java
String htmlInput = "<p>Hello <script>alert('XSS')</script></p>";
String safe = XssSanitizer.sanitizeHtml(htmlInput);
// Kết quả: "<p>Hello </p>" (giữ lại p tag, loại bỏ script)
```

#### `containsXss(String input)`
Kiểm tra xem input có chứa XSS patterns không.

```java
boolean hasXss = XssSanitizer.containsXss("<script>alert('XSS')</script>");
// Kết quả: true
```

### Các Pattern Được Phát Hiện

- `<script>` tags
- `javascript:` protocol
- Event handlers (`onclick`, `onerror`, etc.)
- HTML tags (tùy theo phương thức sử dụng)

---

## 2. XssFilter - Request Filter

### Vị Trí
`src/main/java/com/ecommerce/gocgac/security/filter/XssFilter.java`

### Chức Năng
Filter tự động sanitize tất cả request parameters trước khi chúng đến controller.

### Cách Hoạt Động

1. **Tự động kích hoạt**: Filter được đăng ký với `@Component` và `@Order(1)`, tự động chạy cho mọi request
2. **Sanitize parameters**: Tất cả request parameters được sanitize tự động
3. **Logging**: Ghi log cảnh báo khi phát hiện XSS attempts

### Ví Dụ

**Request gửi đến:**
```
GET /api/products?name=<script>alert('XSS')</script>Product
```

**Sau khi qua XssFilter:**
```
GET /api/products?name=Product
```

### Các Endpoint Bị Bỏ Qua

Filter tự động bỏ qua các static resources:
- `/swagger-ui/**`
- `/v3/api-docs/**`
- `/webjars/**`
- `/actuator/**`
- Các file tĩnh (`.css`, `.js`, `.png`, etc.)

---

## 3. XssRequestWrapper

### Vị Trí
`src/main/java/com/ecommerce/gocgac/security/filter/XssRequestWrapper.java`

### Chức Năng
Wrapper class để override các phương thức của `HttpServletRequest`, tự động sanitize parameters khi được truy cập.

### Cách Hoạt Động

```java
// Trong XssFilter
XssRequestWrapper wrappedRequest = new XssRequestWrapper(request);
filterChain.doFilter(wrappedRequest, response);

// Trong Controller
String name = request.getParameter("name"); // Đã được sanitize tự động
```

---

## 4. SecurityHeadersFilter

### Vị Trí
`src/main/java/com/ecommerce/gocgac/security/filter/SecurityHeadersFilter.java`

### Chức Năng
Thêm các security headers vào HTTP response để bảo vệ ở phía client.

### Các Headers Được Thêm

#### X-XSS-Protection
```
X-XSS-Protection: 1; mode=block
```
Bật XSS filter của browser và chặn trang nếu phát hiện XSS.

#### X-Content-Type-Options
```
X-Content-Type-Options: nosniff
```
Ngăn browser tự động đoán MIME type, tránh MIME type confusion attacks.

#### X-Frame-Options
```
X-Frame-Options: DENY
```
Ngăn trang web được embed trong iframe, chống clickjacking.

#### Content-Security-Policy (CSP)
```
Content-Security-Policy: default-src 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval'; ...
```
Chính sách bảo mật nội dung, kiểm soát các resource được phép load.

#### Strict-Transport-Security (HSTS)
```
Strict-Transport-Security: max-age=31536000; includeSubDomains; preload
```
Chỉ áp dụng cho HTTPS, buộc browser chỉ kết nối qua HTTPS.

#### Referrer-Policy
```
Referrer-Policy: strict-origin-when-cross-origin
```
Kiểm soát thông tin referrer được gửi đi.

#### Permissions-Policy
```
Permissions-Policy: geolocation=(), microphone=(), camera=(), ...
```
Giới hạn các tính năng browser có thể được sử dụng.

---

## 5. @Sanitize Annotation

### Vị Trí
`src/main/java/com/ecommerce/gocgac/common/annotation/Sanitize.java`

### Chức Năng
Annotation để đánh dấu các field trong DTO cần được sanitize.

### Cách Sử Dụng

```java
public class CreateProductRequest {
    
    @Sanitize(SanitizeType.STRICT)
    private String name;  // Sẽ được sanitize strict
    
    @Sanitize(SanitizeType.HTML)
    private String description;  // Cho phép một số HTML tags an toàn
    
    private String code;  // Không cần sanitize (nếu đã được validate)
}
```

### Các Loại Sanitize

- **STRICT**: Loại bỏ tất cả HTML tags (mặc định)
- **HTML**: Cho phép một số HTML tags an toàn (p, br, strong, em, etc.)

---

## Cách Sử Dụng Trong Code

### 1. Sử Dụng Trong Controller

#### Tự Động (Khuyến Nghị)
Filter tự động sanitize parameters, không cần code thêm:

```java
@PostMapping("/products")
public ResponseEntity<Product> createProduct(
    @RequestParam String name,  // Đã được sanitize tự động
    @RequestParam String description) {
    // Code xử lý...
}
```

#### Thủ Công (Khi Cần)
Nếu cần sanitize thủ công trong controller:

```java
@PostMapping("/products")
public ResponseEntity<Product> createProduct(@RequestBody CreateProductRequest request) {
    // Sanitize thủ công nếu cần
    String sanitizedName = XssSanitizer.sanitize(request.getName());
    String sanitizedDescription = XssSanitizer.sanitizeHtml(request.getDescription());
    
    // Sử dụng giá trị đã sanitize
    Product product = productService.create(sanitizedName, sanitizedDescription);
    return ResponseEntity.ok(product);
}
```

### 2. Sử Dụng Trong Service Layer

```java
@Service
public class ProductService {
    
    public Product createProduct(String name, String description) {
        // Sanitize input trước khi lưu vào database
        String safeName = XssSanitizer.sanitize(name);
        String safeDescription = XssSanitizer.sanitizeHtml(description);
        
        Product product = new Product();
        product.setName(safeName);
        product.setDescription(safeDescription);
        
        return productRepository.save(product);
    }
}
```

### 3. Sử Dụng Trong DTO

```java
@Data
public class CreateUserRequest {
    
    @NotBlank
    @Sanitize  // Mặc định là STRICT
    private String fullName;
    
    @Email
    private String email;  // Email không cần sanitize (đã có validation)
    
    @Sanitize(SanitizeType.HTML)
    private String bio;  // Cho phép HTML formatting
}
```

### 4. Kiểm Tra XSS Trước Khi Xử Lý

```java
public void processUserInput(String input) {
    if (XssSanitizer.containsXss(input)) {
        log.warn("XSS attempt detected: {}", input);
        throw new IllegalArgumentException("Input contains potentially dangerous content");
    }
    
    // Xử lý input an toàn
    String safeInput = XssSanitizer.sanitize(input);
    // ...
}
```

---

## Best Practices

### 1. Luôn Sanitize User Input

✅ **Đúng:**
```java
String userInput = request.getParameter("name");
String safe = XssSanitizer.sanitize(userInput);
```

❌ **Sai:**
```java
String userInput = request.getParameter("name");
// Lưu trực tiếp mà không sanitize
product.setName(userInput);
```

### 2. Sử Dụng Đúng Loại Sanitize

✅ **Cho text thông thường:**
```java
String name = XssSanitizer.sanitize(userInput);  // STRICT
```

✅ **Cho rich text content:**
```java
String description = XssSanitizer.sanitizeHtml(userInput);  // HTML
```

### 3. Validate Trước, Sanitize Sau

✅ **Đúng:**
```java
@NotBlank
@Size(max = 100)
@Sanitize
private String name;
```

❌ **Sai:**
```java
@Sanitize
private String name;  // Thiếu validation
```

### 4. Log XSS Attempts

Filter tự động log các XSS attempts. Luôn kiểm tra logs để phát hiện các cuộc tấn công:

```
WARN  XssFilter - XSS attempt detected in parameter 'name': <script>alert('XSS')</script>
```

### 5. Không Sanitize Output

⚠️ **Lưu ý**: Sanitize input, không sanitize output. Output nên được encode bởi template engine (nếu có) hoặc sử dụng output encoding.

---

## Testing XSS Protection

### Test Cases

#### 1. Test Script Tag
```bash
curl -X POST "http://localhost:8086/api/products?name=<script>alert('XSS')</script>Product"
```
**Kết quả mong đợi**: Parameter `name` sẽ là `"Product"` (đã loại bỏ script tag)

#### 2. Test JavaScript Protocol
```bash
curl -X POST "http://localhost:8086/api/products?name=javascript:alert('XSS')"
```
**Kết quả mong đợi**: `javascript:` sẽ bị loại bỏ

#### 3. Test Event Handlers
```bash
curl -X POST "http://localhost:8086/api/products?name=<img onclick='alert(1)'>"
```
**Kết quả mong đợi**: Event handler sẽ bị loại bỏ

#### 4. Test Security Headers
```bash
curl -I http://localhost:8086/api/products
```
**Kết quả mong đợi**: Response headers chứa:
- `X-XSS-Protection: 1; mode=block`
- `X-Content-Type-Options: nosniff`
- `X-Frame-Options: DENY`
- `Content-Security-Policy: ...`

---

## Cấu Hình và Tùy Chỉnh

### Thay Đổi CSP Policy

Nếu cần thay đổi Content-Security-Policy, chỉnh sửa trong `SecurityHeadersFilter`:

```java
String csp = "default-src 'self'; " +
             "script-src 'self'; " +  // Bỏ 'unsafe-inline' và 'unsafe-eval' nếu không cần
             "style-src 'self' 'unsafe-inline'; " +
             // ...
response.setHeader("Content-Security-Policy", csp);
```

### Thêm HTML Tags An Toàn

Nếu cần cho phép thêm HTML tags trong `sanitizeHtml()`, chỉnh sửa `XssSanitizer`:

```java
private static final PolicyFactory RELAXED_POLICY = new HtmlPolicyBuilder()
    .allowElements("p", "br", "strong", "em", "u", "a")  // Thêm "a" tag
    .allowAttributes("href").onElements("a")  // Cho phép href attribute
    .toFactory();
```

### Bỏ Qua Một Số Endpoints

Nếu cần bỏ qua filter cho một số endpoints, chỉnh sửa `shouldNotFilter()`:

```java
@Override
protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    return path.startsWith("/api/webhook") ||  // Thêm endpoint mới
           path.startsWith("/swagger-ui") ||
           // ...
}
```

---

## Monitoring và Logging

### XSS Attempt Logs

Filter tự động log các XSS attempts với level WARN:

```
2024-01-01 10:00:00 [http-nio-8086-exec-1] WARN  XssFilter - XSS attempt detected in parameter 'name': <script>alert('XSS')</script>
2024-01-01 10:00:01 [http-nio-8086-exec-2] WARN  XssFilter - XSS attempt detected in query string: ?search=<script>alert(1)</script>
```

### Giám Sát

- Theo dõi số lượng XSS attempts trong logs
- Thiết lập alert nếu có quá nhiều XSS attempts từ cùng một IP
- Phân tích patterns để cải thiện bảo mật

---

## Tóm Tắt

Hệ thống đã được tích hợp đầy đủ các biện pháp phòng chống XSS:

✅ **Input Sanitization**: Tự động sanitize tất cả request parameters  
✅ **Security Headers**: Thêm các headers bảo mật vào response  
✅ **Utility Methods**: Cung cấp các phương thức sanitize linh hoạt  
✅ **Logging**: Tự động log các XSS attempts  
✅ **Defense in Depth**: Bảo vệ ở nhiều lớp khác nhau  

### Lưu Ý Quan Trọng

1. **Luôn sanitize user input** trước khi lưu vào database
2. **Validate trước, sanitize sau** - kết hợp validation và sanitization
3. **Sử dụng đúng loại sanitize** - STRICT cho text, HTML cho rich text
4. **Theo dõi logs** để phát hiện các cuộc tấn công
5. **Cập nhật thường xuyên** - bảo mật là quá trình liên tục

---

## Tài Liệu Tham Khảo

- [OWASP XSS Prevention Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Cross_Site_Scripting_Prevention_Cheat_Sheet.html)
- [OWASP Java HTML Sanitizer](https://github.com/OWASP/java-html-sanitizer)
- [Content Security Policy (CSP)](https://developer.mozilla.org/en-US/docs/Web/HTTP/CSP)
- [Spring Security Documentation](https://docs.spring.io/spring-security/reference/index.html)

