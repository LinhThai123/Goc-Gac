package com.ecommerce.gocgac.common.util;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

/**
 * Utility class để sanitize input và chống XSS attacks
 */
public class XssSanitizer {
    
    // Pattern để detect các script tags và javascript: protocols
    private static final Pattern SCRIPT_PATTERN = Pattern.compile(
        "<script[^>]*>.*?</script>",
        Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL
    );
    
    private static final Pattern JAVASCRIPT_PATTERN = Pattern.compile(
        "javascript:",
        Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern ON_EVENT_PATTERN = Pattern.compile(
        "on\\w+\\s*=",
        Pattern.CASE_INSENSITIVE
    );
    
    // OWASP HTML Sanitizer Policy - chỉ cho phép text, loại bỏ tất cả HTML tags
    private static final PolicyFactory STRICT_POLICY = new HtmlPolicyBuilder()
        .allowTextIn("body")
        .toFactory();
    
    // Policy cho phép một số HTML tags an toàn (nếu cần)
    private static final PolicyFactory RELAXED_POLICY = new HtmlPolicyBuilder()
        .allowElements("p", "br", "strong", "em", "u", "h1", "h2", "h3", "h4", "h5", "h6")
        .allowAttributes("class").onElements("p", "h1", "h2", "h3", "h4", "h5", "h6")
        .toFactory();
    
    /**
     * Sanitize string input - loại bỏ tất cả HTML tags và script
     * Sử dụng cho các trường text thông thường
     */
    public static String sanitize(String input) {
        if (!StringUtils.hasText(input)) {
            return input;
        }
        
        // Loại bỏ script tags
        String sanitized = SCRIPT_PATTERN.matcher(input).replaceAll("");
        
        // Loại bỏ javascript: protocol
        sanitized = JAVASCRIPT_PATTERN.matcher(sanitized).replaceAll("");
        
        // Loại bỏ on* event handlers
        sanitized = ON_EVENT_PATTERN.matcher(sanitized).replaceAll("");
        
        // Sử dụng OWASP sanitizer để loại bỏ HTML tags
        sanitized = STRICT_POLICY.sanitize(sanitized);
        
        // Escape các ký tự đặc biệt
        sanitized = escapeHtml(sanitized);
        
        return sanitized.trim();
    }
    
    /**
     * Sanitize HTML input - cho phép một số HTML tags an toàn
     * Sử dụng cho các trường rich text editor
     */
    public static String sanitizeHtml(String input) {
        if (!StringUtils.hasText(input)) {
            return input;
        }
        
        // Loại bỏ script tags
        String sanitized = SCRIPT_PATTERN.matcher(input).replaceAll("");
        
        // Loại bỏ javascript: protocol
        sanitized = JAVASCRIPT_PATTERN.matcher(sanitized).replaceAll("");
        
        // Loại bỏ on* event handlers
        sanitized = ON_EVENT_PATTERN.matcher(sanitized).replaceAll("");
        
        // Sử dụng OWASP sanitizer với policy relaxed
        sanitized = RELAXED_POLICY.sanitize(sanitized);
        
        return sanitized.trim();
    }
    
    /**
     * Sanitize cho email, phone, URL - chỉ loại bỏ script tags, không escape HTML
     * Sử dụng cho các trường đã có validation riêng (@Email, @Pattern, @URL)
     * Không escape HTML để giữ nguyên format (ví dụ: @ trong email)
     */
    public static String sanitizeStructuredData(String input) {
        if (!StringUtils.hasText(input)) {
            return input;
        }
        
        // Loại bỏ script tags
        String sanitized = SCRIPT_PATTERN.matcher(input).replaceAll("");
        
        // Loại bỏ javascript: protocol
        sanitized = JAVASCRIPT_PATTERN.matcher(sanitized).replaceAll("");
        
        // Loại bỏ on* event handlers
        sanitized = ON_EVENT_PATTERN.matcher(sanitized).replaceAll("");
        
        // KHÔNG escape HTML - giữ nguyên format cho email, phone, URL
        // Chỉ loại bỏ các script tags và dangerous patterns
        
        return sanitized.trim();
    }
    
    /**
     * Escape HTML special characters
     */
    private static String escapeHtml(String input) {
        if (input == null) {
            return null;
        }
        
        return input
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#x27;")
            .replace("/", "&#x2F;");
    }
    
    /**
     * Kiểm tra xem input có chứa XSS patterns không
     */
    public static boolean containsXss(String input) {
        if (!StringUtils.hasText(input)) {
            return false;
        }
        
        return SCRIPT_PATTERN.matcher(input).find() ||
               JAVASCRIPT_PATTERN.matcher(input).find() ||
               ON_EVENT_PATTERN.matcher(input).find();
    }
    
    /**
     * Sanitize object recursively (cho Map, List, etc.)
     */
    public static Object sanitizeObject(Object obj) {
        if (obj == null) {
            return null;
        }
        
        if (obj instanceof String) {
            return sanitize((String) obj);
        }
        
        // Có thể mở rộng để xử lý Map, List, etc. nếu cần
        return obj;
    }
    
    /**
     * Decode HTML entities (để fix dữ liệu đã bị encode trước đó)
     * Ví dụ: &amp;#64; → @, &amp; → &
     */
    public static String decodeHtmlEntities(String input) {
        if (!StringUtils.hasText(input)) {
            return input;
        }
        
        // Decode các HTML entities phổ biến
        return input
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#x27;", "'")
            .replace("&#x2F;", "/")
            .replace("&#64;", "@")  // @ symbol
            .replace("&#x40;", "@"); // @ symbol (hex)
    }
}

