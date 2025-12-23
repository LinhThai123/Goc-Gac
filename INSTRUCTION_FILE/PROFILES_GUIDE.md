# Hướng dẫn sử dụng Spring Profiles

## Tổng quan

Dự án đã được cấu hình với 3 profiles:
- **dev**: Môi trường phát triển (mặc định)
- **prod**: Môi trường production
- **test**: Môi trường testing

## Cấu trúc file

```
src/main/resources/
├── application.yml          # Cấu hình chung (base)
├── application-dev.yml      # Cấu hình development
├── application-prod.yml    # Cấu hình production
└── application-test.yml     # Cấu hình testing
```

## Cách sử dụng

### 1. Development (Mặc định)

Chạy ứng dụng với profile dev (mặc định):

```bash
# Cách 1: Không cần chỉ định (mặc định là dev)
mvn spring-boot:run

# Cách 2: Chỉ định rõ ràng
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Cách 3: Sử dụng environment variable
export SPRING_PROFILES_ACTIVE=dev
mvn spring-boot:run
```

**Đặc điểm của dev profile:**
- ✅ Hiển thị SQL queries
- ✅ Tự động tạo/cập nhật database schema
- ✅ Swagger UI enabled
- ✅ Debug logging enabled
- ✅ Actuator endpoints exposed
- ✅ Email debug mode

### 2. Production

Chạy ứng dụng với profile prod:

```bash
# Cách 1: Sử dụng Maven
mvn spring-boot:run -Dspring-boot.run.profiles=prod

# Cách 2: Sử dụng JAR file
java -jar target/gocgac-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod

# Cách 3: Sử dụng environment variable
export SPRING_PROFILES_ACTIVE=prod
java -jar target/gocgac-0.0.1-SNAPSHOT.jar

# Cách 4: Trong docker-compose.yml
environment:
  SPRING_PROFILES_ACTIVE: prod
```

**Đặc điểm của prod profile:**
- ❌ Không hiển thị SQL queries
- ❌ Không tự động tạo schema (validate only)
- ❌ Swagger UI disabled
- ⚠️ WARN/ERROR logging only
- ✅ Flyway enabled (migration)
- ✅ HTTPS required cho Keycloak
- ✅ Connection pooling tối ưu
- ✅ Compression enabled
- ✅ HTTP/2 enabled

### 3. Test

Chạy tests với profile test:

```bash
# Chạy tests
mvn test -Dspring.profiles.active=test

# Hoặc trong test class
@ActiveProfiles("test")
```

**Đặc điểm của test profile:**
- ✅ Sử dụng H2 in-memory database
- ✅ Auto create-drop schema
- ✅ Debug logging

## Cấu hình Environment Variables

### Development

Tạo file `.env` hoặc export các biến:

```bash
export SPRING_PROFILES_ACTIVE=dev
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/gocgac
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=gocgac123
export KEYCLOAK_CLIENT_SECRET=your-dev-secret
```

### Production

**QUAN TRỌNG**: Trong production, tất cả sensitive data phải được set qua environment variables:

```bash
export SPRING_PROFILES_ACTIVE=prod
export SPRING_DATASOURCE_URL=jdbc:postgresql://prod-db-host:5432/gocgac
export SPRING_DATASOURCE_USERNAME=prod_user
export SPRING_DATASOURCE_PASSWORD=secure_prod_password
export KEYCLOAK_CLIENT_SECRET=prod_client_secret
export KEYCLOAK_AUTH_SERVER_URL=https://keycloak.yourdomain.com
export SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI=https://keycloak.yourdomain.com/realms/gocgac-realm
export SPRING_MAIL_USERNAME=prod-email@yourdomain.com
export SPRING_MAIL_PASSWORD=prod_email_password
```

## Docker Compose với Profiles

Cập nhật `docker-compose.yml`:

```yaml
services:
  app:
    image: gocgac:latest
    environment:
      SPRING_PROFILES_ACTIVE: ${SPRING_PROFILES_ACTIVE:-dev}
      SPRING_DATASOURCE_URL: ${SPRING_DATASOURCE_URL}
      SPRING_DATASOURCE_USERNAME: ${SPRING_DATASOURCE_USERNAME}
      SPRING_DATASOURCE_PASSWORD: ${SPRING_DATASOURCE_PASSWORD}
      KEYCLOAK_CLIENT_SECRET: ${KEYCLOAK_CLIENT_SECRET}
```

## So sánh các Profiles

| Tính năng | Dev | Prod | Test |
|-----------|-----|------|------|
| Database | PostgreSQL | PostgreSQL | H2 (in-memory) |
| DDL Auto | update | validate | create-drop |
| Show SQL | ✅ | ❌ | ✅ |
| Swagger | ✅ | ❌ | ❌ |
| Logging Level | DEBUG | WARN | DEBUG |
| Flyway | ❌ | ✅ | ❌ |
| HTTPS Required | ❌ | ✅ | ❌ |
| Compression | ❌ | ✅ | ❌ |
| HTTP/2 | ❌ | ✅ | ❌ |

## Lưu ý quan trọng

### Production Checklist

- [ ] Tất cả passwords phải được set qua environment variables
- [ ] Keycloak phải sử dụng HTTPS
- [ ] Database connection pool đã được tối ưu
- [ ] Logging level đã được set về WARN/ERROR
- [ ] Swagger UI đã được tắt
- [ ] Flyway migration đã được test
- [ ] Health checks đã được cấu hình
- [ ] Monitoring/metrics đã được setup

### Security Best Practices

1. **Không commit sensitive data**: Tất cả passwords, secrets phải ở environment variables
2. **Sử dụng secrets management**: AWS Secrets Manager, HashiCorp Vault, etc.
3. **Rotate credentials**: Định kỳ thay đổi passwords và secrets
4. **HTTPS only**: Production phải sử dụng HTTPS cho tất cả connections
5. **Least privilege**: Database user chỉ có quyền cần thiết

## Troubleshooting

### Profile không được áp dụng

```bash
# Kiểm tra profile hiện tại
curl http://localhost:8086/actuator/env | grep spring.profiles.active

# Hoặc trong logs
grep "The following profiles are active" logs/application.log
```

### Database connection failed

```bash
# Kiểm tra environment variables
echo $SPRING_DATASOURCE_URL
echo $SPRING_DATASOURCE_USERNAME

# Test connection
psql -h localhost -p 5433 -U postgres -d gocgac
```

### Keycloak connection failed

```bash
# Kiểm tra Keycloak đang chạy
curl http://localhost:8080/health/ready

# Kiểm tra realm
curl http://localhost:8080/realms/gocgac-realm/.well-known/openid-configuration
```

## Migration từ application.properties

File `application.properties` vẫn có thể được sử dụng, nhưng khuyến nghị chuyển sang YAML:

1. **Ưu điểm của YAML**:
   - Dễ đọc hơn
   - Hỗ trợ profiles tốt hơn
   - Cấu trúc rõ ràng hơn
   - Hỗ trợ multi-document

2. **Nếu muốn giữ properties**:
   - File `application.properties` vẫn hoạt động
   - Spring Boot sẽ ưu tiên YAML nếu cả hai tồn tại
   - Có thể xóa `application.properties` sau khi chuyển sang YAML

