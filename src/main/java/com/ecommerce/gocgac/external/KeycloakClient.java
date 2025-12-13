package com.ecommerce.gocgac.external;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ecommerce.gocgac.common.constant.KeycloakErrorConstants.*;

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
            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = (ResponseEntity<Map<String, Object>>) 
                (ResponseEntity<?>) restTemplate.postForEntity(tokenUrl, request, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
            
            throw new RuntimeException(LOGIN_NO_TOKEN);
        } catch (HttpClientErrorException e) {
            log.error("Keycloak login error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            if (e.getStatusCode().value() == 401) {
                throw new RuntimeException(LOGIN_INVALID_CREDENTIALS);
            }
            throw new RuntimeException(LOGIN_FAILED + ": " + e.getMessage());
        } catch (Exception e) {
            log.error("Keycloak login error: {}", e.getMessage(), e);
            throw new RuntimeException(LOGIN_FAILED + ": " + e.getMessage());
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
        
        // Tạo user object
        Map<String, Object> user = new HashMap<>();
        user.put("username", email);
        user.put("email", email);
        user.put("firstName", fullName);
        user.put("enabled", true);
        user.put("emailVerified", false);
        
        // Set password trong credentials
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
                
                // Set password riêng để đảm bảo password được set đúng
                if (userId != null && !userId.isEmpty()) {
                    try {
                        setUserPassword(userId, password, adminToken);
                        log.debug("Đã set password cho user {}", userId);
                    } catch (Exception e) {
                        log.warn("Không thể set password riêng, nhưng user đã được tạo: {}", e.getMessage());
                    }
                }
                
                Map<String, Object> result = new HashMap<>();
                result.put("userId", userId != null ? userId : "created");
                return result;
            } else {
                throw new RuntimeException(String.format(REGISTER_CANNOT_CREATE_USER, response.getStatusCode()));
            }
        } catch (HttpClientErrorException e) {
            String errorBody = e.getResponseBodyAsString();
            int statusCode = e.getStatusCode().value();
            
            if (statusCode == 409) {
                throw new RuntimeException(REGISTER_EMAIL_EXISTS);
            }
            if (statusCode == 403) {
                throw new RuntimeException(String.format(REGISTER_NO_PERMISSION, realm, clientId));
            }
            throw new RuntimeException(REGISTER_FAILED + ": " + statusCode + " - " + errorBody);
        } catch (Exception e) {
            log.error("Keycloak register error: {}", e.getMessage(), e);
            throw new RuntimeException(REGISTER_FAILED + ": " + e.getMessage());
        }
    }
    
    /**
     * Lấy admin token để thực hiện các thao tác quản trị
     * Ưu tiên: Master realm admin → Service account (fallback)
     */
    private String getAdminToken() {
        // Ưu tiên: Master realm admin (đơn giản cho development)
        try {
            return getMasterAdminToken();
        } catch (Exception e) {
            log.warn("Failed to get master admin token: {}", e.getMessage());
            
            // Fallback: Service account
            try {
                String token = getServiceAccountToken();
                log.warn("Using service account token as fallback. " +
                    "Nên cấu hình master admin credentials hoặc service account roles cho client {} trong realm {}", 
                    clientId, realm);
                return token;
            } catch (Exception serviceAccountException) {
                log.error("Both master admin and service account token failed. " +
                    "Master error: {}, Service account error: {}", 
                    e.getMessage(), serviceAccountException.getMessage());
                throw new RuntimeException(String.format(ADMIN_TOKEN_NO_CREDENTIALS, realm, clientId));
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
            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = (ResponseEntity<Map<String, Object>>) 
                (ResponseEntity<?>) restTemplate.postForEntity(tokenUrl, request, Map.class);
            Map<String, Object> responseBody = response.getBody();
            
            if (responseBody != null && responseBody.containsKey("access_token")) {
                return (String) responseBody.get("access_token");
            }
            throw new RuntimeException(String.format(ADMIN_TOKEN_NO_ACCESS_TOKEN, "service account"));
        } catch (Exception e) {
            log.error("Failed to get service account token: {}", e.getMessage());
            throw new RuntimeException(ADMIN_TOKEN_SERVICE_ACCOUNT_FAILED + ": " + e.getMessage());
        }
    }
    
    /**
     * Lấy admin token từ master realm
     * Dùng 'admin-cli' client (public client) với username/password từ config
     * Lưu ý: Trong production, nên sử dụng service account với proper roles
     */
    private String getMasterAdminToken() {
        String tokenUrl = String.format("%s/realms/master/protocol/openid-connect/token", keycloakServerUrl);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("client_id", "admin-cli");
        body.add("username", adminUsername);
        body.add("password", adminPassword);
        
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        
        try {
            log.debug("Requesting master admin token with username={}", adminUsername);
            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = (ResponseEntity<Map<String, Object>>) 
                (ResponseEntity<?>) restTemplate.postForEntity(tokenUrl, request, Map.class);
            Map<String, Object> responseBody = response.getBody();
            
            if (response.getStatusCode().is2xxSuccessful() && 
                responseBody != null && 
                responseBody.containsKey("access_token")) {
                String token = (String) responseBody.get("access_token");
                log.info("Successfully obtained master admin token");
                
                // Test token có quyền trong realm không
                if (!testAdminTokenInRealm(token)) {
                    log.warn("Master admin token không có quyền trong realm {}", realm);
                }
                
                return token;
            }
            
            throw new RuntimeException(String.format(ADMIN_TOKEN_NO_ACCESS_TOKEN, "master realm"));
        } catch (HttpClientErrorException e) {
            String errorBody = e.getResponseBodyAsString();
            log.error("Failed to get master admin token: {} - {}", e.getStatusCode(), errorBody);
            throw new RuntimeException(String.format(ADMIN_TOKEN_MASTER_LOGIN_FAILED, adminUsername, errorBody));
        } catch (Exception e) {
            log.error("Failed to get master admin token: {}", e.getMessage(), e);
            throw new RuntimeException(ADMIN_TOKEN_FAILED + ": " + e.getMessage());
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
            
            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = (ResponseEntity<Map<String, Object>>) 
                (ResponseEntity<?>) restTemplate.exchange(testUrl, HttpMethod.GET, request, Map.class);
            
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.warn("Failed to test admin token in realm {}: {}", realm, e.getMessage());
            return false;
        }
    }
    
    /**
     * Set password cho user
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
        
        try {
            ResponseEntity<Void> response = restTemplate.exchange(passwordUrl, HttpMethod.PUT, request, Void.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.warn("Không thể set password cho user {}. Status: {}", userId, response.getStatusCode());
                throw new RuntimeException(USER_CANNOT_SET_PASSWORD);
            }
        } catch (Exception e) {
            log.warn("Failed to set password for user {}: {}", userId, e.getMessage());
            throw new RuntimeException(USER_CANNOT_SET_PASSWORD + ": " + e.getMessage());
        }
    }
    
    /**
     * Gán role cho user
     * Ưu tiên gán client role từ gocgac_app, nếu không có thì gán realm role
     */
    public void assignRoleToUser(String userId, String roleName) {
        String adminToken = getAdminToken();
        
        // Thử gán client role trước (từ client gocgac_app)
        try {
            assignClientRoleToUser(userId, roleName, adminToken);
            log.info("Đã gán client role {} cho user {}", roleName, userId);
            return;
        } catch (Exception e) {
            log.warn("Không thể gán client role {}, thử gán realm role: {}", roleName, e.getMessage());
        }
        
        // Fallback: Gán realm role
        try {
            // Kiểm tra và tạo realm role nếu chưa tồn tại
            if (!roleExistsInRealm(roleName, adminToken)) {
                log.info("Role {} chưa tồn tại trong Keycloak realm, đang tạo...", roleName);
                createRealmRole(roleName, adminToken);
            }
            
            assignRealmRoleToUser(userId, roleName, adminToken);
            log.info("Đã gán realm role {} cho user {}", roleName, userId);
        } catch (Exception e) {
            log.error("Không thể gán realm role {} cho user {}: {}", roleName, userId, e.getMessage(), e);
            throw new RuntimeException(String.format(ROLE_CANNOT_ASSIGN, roleName) + ": " + e.getMessage());
        }
    }
    
    /**
     * Gán client role cho user (từ client gocgac_app)
     */
    private void assignClientRoleToUser(String userId, String roleName, String adminToken) {
        String roleUrl = String.format("%s/admin/realms/%s/users/%s/role-mappings/clients/%s", 
            keycloakServerUrl, realm, userId, getClientId(adminToken));
        
        // Lấy client role
        String clientRolesUrl = String.format("%s/admin/realms/%s/clients/%s/roles/%s", 
            keycloakServerUrl, realm, getClientId(adminToken), roleName);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(adminToken);
        
        HttpEntity<String> getRequest = new HttpEntity<>(headers);
        
        try {
            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> roleResponse = (ResponseEntity<Map<String, Object>>) 
                (ResponseEntity<?>) restTemplate.exchange(clientRolesUrl, HttpMethod.GET, getRequest, Map.class);
            
            Map<String, Object> role = roleResponse.getBody();
            if (role == null) {
                throw new RuntimeException(String.format(ROLE_CLIENT_NOT_EXISTS, roleName, clientId));
            }
            
            @SuppressWarnings("unchecked")
            HttpEntity<Map<String, Object>[]> assignRequest = new HttpEntity<>(
                (Map<String, Object>[]) new Map[]{role}, headers);
            
            ResponseEntity<Void> assignResponse = restTemplate.postForEntity(roleUrl, assignRequest, Void.class);
            if (!assignResponse.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException(String.format(ROLE_CANNOT_ASSIGN, roleName) + ". Status: " + assignResponse.getStatusCode());
            }
        } catch (HttpClientErrorException e) {
            String errorBody = e.getResponseBodyAsString();
            if (e.getStatusCode().value() == 404) {
                throw new RuntimeException(String.format(ROLE_CLIENT_NOT_EXISTS, roleName, clientId));
            }
            throw new RuntimeException(String.format(ROLE_CANNOT_ASSIGN, roleName) + ": " + errorBody);
        }
    }
    
    /**
     * Gán realm role cho user
     */
    private void assignRealmRoleToUser(String userId, String roleName, String adminToken) {
        String roleUrl = String.format("%s/admin/realms/%s/users/%s/role-mappings/realm", 
            keycloakServerUrl, realm, userId);
        
        // Lấy realm role
        String realmRolesUrl = String.format("%s/admin/realms/%s/roles/%s", 
            keycloakServerUrl, realm, roleName);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(adminToken);
        
        HttpEntity<String> getRequest = new HttpEntity<>(headers);
        
        try {
            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> roleResponse = (ResponseEntity<Map<String, Object>>) 
                (ResponseEntity<?>) restTemplate.exchange(realmRolesUrl, HttpMethod.GET, getRequest, Map.class);
            
            Map<String, Object> role = roleResponse.getBody();
            if (role == null) {
                throw new RuntimeException(String.format(ROLE_REALM_NOT_EXISTS, roleName));
            }
            
            @SuppressWarnings("unchecked")
            HttpEntity<Map<String, Object>[]> assignRequest = new HttpEntity<>(
                (Map<String, Object>[]) new Map[]{role}, headers);
            
            ResponseEntity<Void> assignResponse = restTemplate.postForEntity(roleUrl, assignRequest, Void.class);
            if (!assignResponse.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException(String.format(ROLE_CANNOT_ASSIGN, roleName) + ". Status: " + assignResponse.getStatusCode());
            }
        } catch (HttpClientErrorException e) {
            String errorBody = e.getResponseBodyAsString();
            throw new RuntimeException(String.format(ROLE_CANNOT_ASSIGN, roleName) + ": " + errorBody);
        }
    }
    
    /**
     * Lấy client ID (UUID) của client gocgac_app
     */
    private String getClientId(String adminToken) {
        String clientsUrl = String.format("%s/admin/realms/%s/clients?clientId=%s", 
            keycloakServerUrl, realm, clientId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        try {
            @SuppressWarnings("unchecked")
            ResponseEntity<List<Map<String, Object>>> response = (ResponseEntity<List<Map<String, Object>>>) 
                (ResponseEntity<?>) restTemplate.exchange(clientsUrl, HttpMethod.GET, request, List.class);
            
            List<Map<String, Object>> clients = response.getBody();
            if (clients != null && !clients.isEmpty()) {
                Map<String, Object> client = clients.get(0);
                Object id = client.get("id");
                if (id != null) {
                    return id.toString();
                }
            }
            throw new RuntimeException(String.format(CLIENT_NOT_FOUND, clientId));
        } catch (Exception e) {
            log.error("Failed to get client ID for {}: {}", clientId, e.getMessage());
            throw new RuntimeException(CLIENT_CANNOT_GET_ID + ": " + e.getMessage());
        }
    }
    
    /**
     * Kiểm tra role có tồn tại trong realm không
     */
    private boolean roleExistsInRealm(String roleName, String adminToken) {
        String realmRolesUrl = String.format("%s/admin/realms/%s/roles/%s", 
            keycloakServerUrl, realm, roleName);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        try {
            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = (ResponseEntity<Map<String, Object>>) 
                (ResponseEntity<?>) restTemplate.exchange(realmRolesUrl, HttpMethod.GET, request, Map.class);
            return response.getStatusCode().is2xxSuccessful() && response.getBody() != null;
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 404) {
                return false;
            }
            log.warn("Error checking role existence: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.warn("Error checking role existence: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Tạo realm role trong Keycloak
     */
    private void createRealmRole(String roleName, String adminToken) {
        String createRoleUrl = String.format("%s/admin/realms/%s/roles", 
            keycloakServerUrl, realm);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(adminToken);
        
        Map<String, Object> role = new HashMap<>();
        role.put("name", roleName);
        role.put("description", "Role " + roleName + " for " + realm + " realm");
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(role, headers);
        
        try {
            ResponseEntity<Void> response = restTemplate.postForEntity(createRoleUrl, request, Void.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Đã tạo role {} trong Keycloak realm", roleName);
            } else {
                log.error("Không thể tạo role {}. Status: {}", roleName, response.getStatusCode());
                throw new RuntimeException(String.format(ROLE_CANNOT_CREATE, roleName));
            }
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 409) {
                log.info(String.format(ROLE_ALREADY_EXISTS, roleName));
                return; // Role đã tồn tại, không cần tạo lại
            }
            String errorBody = e.getResponseBodyAsString();
            log.error("Không thể tạo role {}: {} - {}", roleName, e.getStatusCode(), errorBody);
            throw new RuntimeException(String.format(ROLE_CANNOT_CREATE, roleName) + ": " + errorBody);
        } catch (Exception e) {
            log.error("Failed to create role {}: {}", roleName, e.getMessage(), e);
            throw new RuntimeException(String.format(ROLE_CANNOT_CREATE, roleName) + ": " + e.getMessage());
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
            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = (ResponseEntity<Map<String, Object>>) 
                (ResponseEntity<?>) restTemplate.postForEntity(tokenUrl, request, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
            
            throw new RuntimeException(REFRESH_TOKEN_FAILED);
        } catch (HttpClientErrorException e) {
            log.error("Keycloak refresh token error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            if (e.getStatusCode().value() == 400) {
                throw new RuntimeException(REFRESH_TOKEN_INVALID);
            }
            throw new RuntimeException(REFRESH_TOKEN_FAILED + ": " + e.getMessage());
        } catch (Exception e) {
            log.error("Keycloak refresh token error: {}", e.getMessage(), e);
            throw new RuntimeException(REFRESH_TOKEN_FAILED + ": " + e.getMessage());
        }
    }
    
    /**
     * Xóa user trong Keycloak
     */
    public void deleteUser(String userId) {
        String adminToken = getAdminToken();
        String deleteUserUrl = String.format("%s/admin/realms/%s/users/%s", 
            keycloakServerUrl, realm, userId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        try {
            restTemplate.exchange(deleteUserUrl, HttpMethod.DELETE, request, Void.class);
            log.info("Đã xóa user {} trong Keycloak", userId);
        } catch (Exception e) {
            log.error("Không thể xóa user {} trong Keycloak: {}", userId, e.getMessage());
            throw new RuntimeException(USER_CANNOT_DELETE + ": " + e.getMessage());
        }
    }
    
    /**
     * Gửi email verification từ Keycloak
     * Keycloak sẽ tự động gửi email verification với link của Keycloak
     * 
     * @param userId Keycloak user ID
     * @param clientId Optional: Client ID để customize redirect URI
     * @param redirectUri Optional: Redirect URI sau khi verify
     */
    public void sendVerificationEmail(String userId, String clientId, String redirectUri) {
        String adminToken = getAdminToken();
        // Sử dụng endpoint execute-actions-email với action VERIFY_EMAIL
        String sendEmailUrl = String.format("%s/admin/realms/%s/users/%s/execute-actions-email", 
            keycloakServerUrl, realm, userId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        // Body phải là array chứa action "VERIFY_EMAIL"
        List<String> actions = List.of("VERIFY_EMAIL");
        
        // Nếu có clientId, thêm vào query params
        // Lưu ý: redirectUri chỉ truyền nếu đã được cấu hình trong Keycloak client
        // Nếu không, Keycloak sẽ tự xử lý redirect
        StringBuilder urlBuilder = new StringBuilder(sendEmailUrl);
        if (clientId != null && !clientId.isEmpty()) {
            urlBuilder.append("?client_id=").append(clientId);
            // Chỉ thêm redirect_uri nếu được cung cấp và đã được cấu hình trong Keycloak
            if (redirectUri != null && !redirectUri.isEmpty()) {
                // URL encode redirect URI để tránh lỗi
                try {
                    String encodedRedirectUri = java.net.URLEncoder.encode(redirectUri, "UTF-8");
                    urlBuilder.append("&redirect_uri=").append(encodedRedirectUri);
                } catch (java.io.UnsupportedEncodingException e) {
                    log.warn("Không thể encode redirect URI: {}", redirectUri);
                    // Bỏ qua redirect_uri nếu không encode được
                }
            }
        }
        sendEmailUrl = urlBuilder.toString();
        
        HttpEntity<List<String>> request = new HttpEntity<>(actions, headers);
        
        try {
            ResponseEntity<Void> response = restTemplate.exchange(
                sendEmailUrl, HttpMethod.PUT, request, Void.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Đã gửi email verification từ Keycloak cho user {}", userId);
            } else {
                log.warn("Không thể gửi email verification từ Keycloak. Status: {}", response.getStatusCode());
                throw new RuntimeException("Không thể gửi email verification từ Keycloak. Status: " + response.getStatusCode());
            }
        } catch (HttpClientErrorException e) {
            String errorBody = e.getResponseBodyAsString();
            log.error("Lỗi khi gửi email verification từ Keycloak cho user {}: {} - {}", userId, e.getStatusCode(), errorBody);
            throw new RuntimeException("Không thể gửi email verification từ Keycloak: " + errorBody);
        } catch (Exception e) {
            log.error("Lỗi khi gửi email verification từ Keycloak cho user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Không thể gửi email verification từ Keycloak: " + e.getMessage());
        }
    }
    
    /**
     * Gửi email verification từ Keycloak (simplified - dùng default client)
     */
    public void sendVerificationEmail(String userId) {
        sendVerificationEmail(userId, clientId, null);
    }
    
    /**
     * Lấy client ID (dùng cho các service khác)
     */
    public String getClientId() {
        return clientId;
    }
    
    /**
     * Đánh dấu email đã được verify trong Keycloak (manual verification)
     * 
     * @param userId Keycloak user ID
     */
    public void verifyEmail(String userId) {
        String adminToken = getAdminToken();
        String userUrl = String.format("%s/admin/realms/%s/users/%s", 
            keycloakServerUrl, realm, userId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(adminToken);
        
        // Lấy thông tin user hiện tại
        try {
            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> getResponse = (ResponseEntity<Map<String, Object>>) 
                (ResponseEntity<?>) restTemplate.exchange(userUrl, HttpMethod.GET, 
                    new HttpEntity<>(headers), Map.class);
            
            if (getResponse.getBody() != null) {
                Map<String, Object> user = new HashMap<>(getResponse.getBody());
                user.put("emailVerified", true);
                
                HttpEntity<Map<String, Object>> updateRequest = new HttpEntity<>(user, headers);
                ResponseEntity<Void> updateResponse = restTemplate.exchange(
                    userUrl, HttpMethod.PUT, updateRequest, Void.class);
                
                if (updateResponse.getStatusCode().is2xxSuccessful()) {
                    log.info("Đã verify email cho user {} trong Keycloak", userId);
                } else {
                    log.warn("Không thể verify email trong Keycloak. Status: {}", updateResponse.getStatusCode());
                    throw new RuntimeException("Không thể verify email trong Keycloak. Status: " + updateResponse.getStatusCode());
                }
            }
        } catch (HttpClientErrorException e) {
            String errorBody = e.getResponseBodyAsString();
            log.error("Lỗi khi verify email trong Keycloak cho user {}: {} - {}", userId, e.getStatusCode(), errorBody);
            throw new RuntimeException("Không thể verify email trong Keycloak: " + errorBody);
        } catch (Exception e) {
            log.error("Lỗi khi verify email trong Keycloak cho user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Không thể verify email trong Keycloak: " + e.getMessage());
        }
    }
    
    /**
     * Lấy trạng thái email verified của user trong Keycloak
     * 
     * @param userId Keycloak user ID
     * @return true nếu email đã verified, false nếu chưa
     */
    public boolean isEmailVerified(String userId) {
        String adminToken = getAdminToken();
        String userUrl = String.format("%s/admin/realms/%s/users/%s", 
            keycloakServerUrl, realm, userId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        try {
            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = (ResponseEntity<Map<String, Object>>) 
                (ResponseEntity<?>) restTemplate.exchange(userUrl, HttpMethod.GET, request, Map.class);
            
            if (response.getBody() != null) {
                Object emailVerified = response.getBody().get("emailVerified");
                return emailVerified != null && Boolean.TRUE.equals(emailVerified);
            }
            return false;
        } catch (Exception e) {
            log.error("Lỗi khi lấy email verified status cho user {}: {}", userId, e.getMessage());
            return false;
        }
    }
    
    /**
     * Lấy Keycloak user ID từ email
     * 
     * @param email Email của user
     * @return Keycloak user ID hoặc null nếu không tìm thấy
     */
    public String getUserIdByEmail(String email) {
        String adminToken = getAdminToken();
        String usersUrl = String.format("%s/admin/realms/%s/users?email=%s&exact=true", 
            keycloakServerUrl, realm, email);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        try {
            @SuppressWarnings("unchecked")
            ResponseEntity<List<Map<String, Object>>> response = (ResponseEntity<List<Map<String, Object>>>) 
                (ResponseEntity<?>) restTemplate.exchange(usersUrl, HttpMethod.GET, request, List.class);
            
            if (response.getBody() != null && !response.getBody().isEmpty()) {
                Map<String, Object> user = response.getBody().get(0);
                return (String) user.get("id");
            }
            return null;
        } catch (Exception e) {
            log.error("Lỗi khi lấy user ID từ email {}: {}", email, e.getMessage());
            return null;
        }
    }
    
}

