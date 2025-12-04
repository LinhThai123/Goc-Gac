package com.ecommerce.gocgac.external;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class KeycloakClient {
    
    private final RestTemplate restTemplate;
    
    @Value("${keycloak.auth-server-url}")
    private String keycloakServerUrl;
    
    @Value("${keycloak.realm}")
    private String realm;
    
    @Value("${keycloak.resource}")
    private String clientId;
    
    @Value("${keycloak.credentials.secret}")
    private String clientSecret;
    
    @Value("${keycloak.admin.username:admin}")
    private String adminUsername;
    
    @Value("${keycloak.admin.password:admin123}")
    private String adminPassword;
    
    public KeycloakClient() {
        this.restTemplate = new RestTemplate();
    }
    
    /**
     * Đăng nhập và lấy access token từ Keycloak
     */
    public Map<String, Object> login(String email, String password) {
        String tokenUrl = String.format("%s/realms/%s/protocol/openid-connect/token", 
            keycloakServerUrl, realm);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("username", email);
        body.add("password", password);
        
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
            
            throw new RuntimeException("Không nhận được token từ Keycloak");
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("Keycloak login error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            if (e.getStatusCode().value() == 401) {
                throw new RuntimeException("Email hoặc mật khẩu không đúng");
            }
            throw new RuntimeException("Đăng nhập thất bại: " + e.getMessage());
        } catch (Exception e) {
            log.error("Keycloak login error: {}", e.getMessage(), e);
            throw new RuntimeException("Đăng nhập thất bại: " + e.getMessage());
        }
    }
    
    /**
     * Đăng ký user mới trong Keycloak (đơn giản như dự án tham khảo)
     */
    public Map<String, Object> registerUser(String email, String password, String fullName) {
        String adminToken = getAdminToken();
        String usersUrl = String.format("%s/admin/realms/%s/users", 
            keycloakServerUrl, realm);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(adminToken);
        
        // Tạo user với password trong credentials (đơn giản như dự án tham khảo)
        Map<String, Object> user = new HashMap<>();
        user.put("username", email);
        user.put("email", email);
        user.put("firstName", fullName);
        user.put("enabled", true);
        user.put("emailVerified", false);
        
        Map<String, Object> credentials = new HashMap<>();
        credentials.put("type", "password");
        credentials.put("value", password);
        credentials.put("temporary", false);
        user.put("credentials", List.of(credentials));
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(user, headers);
        
        try {
            ResponseEntity<Void> response = restTemplate.postForEntity(usersUrl, request, Void.class);
            
            if (response.getStatusCode() == HttpStatus.CREATED) {
                // Lấy user ID từ Location header
                String userId = null;
                String location = response.getHeaders().getFirst("Location");
                if (location != null && location.contains("/users/")) {
                    userId = location.substring(location.lastIndexOf("/users/") + 7);
                    if (userId.contains("?")) {
                        userId = userId.substring(0, userId.indexOf("?"));
                    }
                }
                
                // Nếu có user ID, thử set password riêng để đảm bảo password được set đúng
                if (userId != null && !userId.isEmpty()) {
                    try {
                        setUserPassword(userId, password, adminToken);
                    } catch (Exception e) {
                        log.warn("Không thể set password riêng, nhưng user đã được tạo: {}", e.getMessage());
                    }
                }
                
                Map<String, Object> result = new HashMap<>();
                result.put("userId", userId != null ? userId : "created");
                return result;
            } else {
                throw new RuntimeException("Không thể tạo user trong Keycloak. Status: " + response.getStatusCode());
            }
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            String errorBody = e.getResponseBodyAsString();
            if (e.getStatusCode().value() == 409) {
                throw new RuntimeException("Email đã được sử dụng trong Keycloak");
            }
            if (e.getStatusCode().value() == 403) {
                throw new RuntimeException("Không có quyền tạo user trong Keycloak. " +
                    "Vui lòng cấu hình service account roles: " +
                    "Keycloak Admin Console > Realm '" + realm + "' > Clients > '" + clientId + 
                    "' > Service accounts roles > Assign 'manage-users' từ 'realm-management'");
            }
            throw new RuntimeException("Đăng ký thất bại: " + e.getStatusCode() + " - " + errorBody);
        } catch (Exception e) {
            log.error("Keycloak register error: {}", e.getMessage(), e);
            throw new RuntimeException("Đăng ký thất bại: " + e.getMessage());
        }
    }
    
    /**
     * Lấy admin token để thực hiện các thao tác quản trị
     * Ưu tiên sử dụng master realm admin (đơn giản và đáng tin cậy hơn cho development)
     * Fallback sang service account nếu master admin không hoạt động
     */
    private String getAdminToken() {
        // Ưu tiên dùng master realm admin (như dự án tham khảo - đơn giản và đáng tin cậy hơn)
        try {
            String token = getMasterAdminToken();
            log.info("Successfully obtained master realm admin token");
            return token;
        } catch (Exception e) {
            log.warn("Failed to get master realm admin token: {}", e.getMessage());
            // Fallback: thử với service account từ realm gocgac-htx
            try {
                String token = getServiceAccountToken();
                log.warn("Using service account token as fallback. " +
                    "Nên cấu hình master admin credentials hoặc service account roles cho client {} trong realm {} để bảo mật hơn.", 
                    clientId, realm);
                return token;
            } catch (Exception serviceAccountException) {
                log.error("Both master realm admin and service account token failed. " +
                    "Master realm error: {}, Service account error: {}", 
                    e.getMessage(), serviceAccountException.getMessage());
                throw new RuntimeException("Không thể lấy admin token. " +
                    "Vui lòng kiểm tra:\n" +
                    "1. Master admin credentials: keycloak.admin.username và keycloak.admin.password trong application-dev.yml (mặc định: admin/admin123)\n" +
                    "2. Hoặc Service account: Keycloak Admin Console > Realm '" + realm + "' > Clients > '" + clientId + "' > Service accounts roles > Assign 'manage-users', 'view-users', 'query-users' từ 'realm-management'");
            }
        }
    }
    
    /**
     * Lấy token từ service account (client credentials)
     */
    private String getServiceAccountToken() {
        String tokenUrl = String.format("%s/realms/%s/protocol/openid-connect/token", 
            keycloakServerUrl, realm);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);
            if (response.getBody() != null && response.getBody().containsKey("access_token")) {
                return (String) response.getBody().get("access_token");
            }
            throw new RuntimeException("Không nhận được access token từ service account");
        } catch (Exception e) {
            log.error("Failed to get service account token: {}", e.getMessage());
            throw new RuntimeException("Không thể lấy admin token: " + e.getMessage());
        }
    }
    
    /**
     * Lấy admin token từ master realm (như dự án tham khảo)
     * Dùng 'admin-cli' client (public client trong master realm) với username/password từ config
     * Lưu ý: Trong production, nên sử dụng service account với proper roles
     */
    private String getMasterAdminToken() {
        String tokenUrl = String.format("%s/realms/master/protocol/openid-connect/token", 
            keycloakServerUrl);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("client_id", "admin-cli"); // admin-cli là public client trong master realm, không cần secret
        body.add("username", adminUsername);
        body.add("password", adminPassword);
        
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        
        try {
            log.debug("Requesting master admin token with client_id=admin-cli, username={}", adminUsername);
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && 
                response.getBody() != null && 
                response.getBody().containsKey("access_token")) {
                String token = (String) response.getBody().get("access_token");
                log.info("Successfully obtained master admin token from master realm");
                
                // Test token có quyền trong realm gocgac-htx không
                if (!testAdminTokenInRealm(token)) {
                    log.warn("Master admin token không có quyền trong realm {}. " +
                        "Cần đảm bảo admin user có quyền quản lý realm này.", realm);
                }
                
                return token;
            }
            
            throw new RuntimeException("Không nhận được access token từ master realm");
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            String errorBody = e.getResponseBodyAsString();
            log.error("Failed to get master admin token: {} - {}", e.getStatusCode(), errorBody);
            throw new RuntimeException("Không thể đăng nhập vào Keycloak Admin với username='" + adminUsername + "'. " +
                "Vui lòng kiểm tra:\n" +
                "1. Keycloak admin credentials (keycloak.admin.username và keycloak.admin.password) - mặc định: admin/admin123\n" +
                "2. Keycloak đang chạy: curl http://localhost:8080/health/ready\n" +
                "3. Error: " + errorBody);
        } catch (Exception e) {
            log.error("Failed to get master admin token: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể lấy admin token: " + e.getMessage());
        }
    }
    
    /**
     * Test xem admin token có quyền trong realm không
     */
    private boolean testAdminTokenInRealm(String token) {
        try {
            String testUrl = String.format("%s/admin/realms/%s", keycloakServerUrl, realm);
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            HttpEntity<String> request = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                testUrl, HttpMethod.GET, request, Map.class);
            
            boolean hasAccess = response.getStatusCode().is2xxSuccessful();
            if (!hasAccess) {
                log.warn("Admin token không có quyền truy cập realm {}. Status: {}", realm, response.getStatusCode());
            }
            return hasAccess;
        } catch (Exception e) {
            log.warn("Failed to test admin token in realm {}: {}", realm, e.getMessage());
            return false;
        }
    }
    
    /**
     * Set password cho user (đơn giản)
     */
    private void setUserPassword(String userId, String password, String adminToken) {
        String passwordUrl = String.format("%s/admin/realms/%s/users/%s/reset-password", 
            keycloakServerUrl, realm, userId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(adminToken);
        
        Map<String, Object> passwordData = new HashMap<>();
        passwordData.put("type", "password");
        passwordData.put("value", password);
        passwordData.put("temporary", false);
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(passwordData, headers);
        restTemplate.exchange(passwordUrl, HttpMethod.PUT, request, Void.class);
    }
    
    /**
     * Gán role cho user
     */
    public void assignRoleToUser(String userId, String roleName) {
        String adminToken = getAdminToken();
        String roleUrl = String.format("%s/admin/realms/%s/users/%s/role-mappings/realm", 
            keycloakServerUrl, realm, userId);
        
        // Lấy role từ realm
        String realmRolesUrl = String.format("%s/admin/realms/%s/roles/%s", 
            keycloakServerUrl, realm, roleName);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(adminToken);
        
        HttpEntity<String> getRequest = new HttpEntity<>(headers);
        
        try {
            ResponseEntity<Map> roleResponse = restTemplate.exchange(
                realmRolesUrl, HttpMethod.GET, getRequest, Map.class);
            
            if (roleResponse.getBody() == null) {
                log.warn("Role {} không tồn tại trong Keycloak", roleName);
                return;
            }
            
            Map<String, Object> role = roleResponse.getBody();
            
            HttpEntity<Map[]> assignRequest = new HttpEntity<>(
                new Map[]{role}, headers);
            
            restTemplate.postForEntity(roleUrl, assignRequest, String.class);
            log.info("Đã gán role {} cho user {}", roleName, userId);
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.warn("Không thể gán role {} cho user {}: {}", roleName, userId, e.getMessage());
            // Không throw exception để không làm gián đoạn quá trình đăng ký
        } catch (Exception e) {
            log.warn("Failed to assign role {} to user {}: {}", roleName, userId, e.getMessage());
            // Không throw exception để không làm gián đoạn quá trình đăng ký
        }
    }
    
    /**
     * Refresh token
     */
    public Map<String, Object> refreshToken(String refreshToken) {
        String tokenUrl = String.format("%s/realms/%s/protocol/openid-connect/token", 
            keycloakServerUrl, realm);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "refresh_token");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("refresh_token", refreshToken);
        
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
            
            throw new RuntimeException("Không thể refresh token");
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("Keycloak refresh token error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            if (e.getStatusCode().value() == 400) {
                throw new RuntimeException("Refresh token không hợp lệ hoặc đã hết hạn");
            }
            throw new RuntimeException("Refresh token thất bại: " + e.getMessage());
        } catch (Exception e) {
            log.error("Keycloak refresh token error: {}", e.getMessage(), e);
            throw new RuntimeException("Refresh token thất bại: " + e.getMessage());
        }
    }
}

