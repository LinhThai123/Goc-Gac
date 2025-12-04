# PowerShell script để tạo database Keycloak
# Chạy script này nếu database chưa được tạo tự động

Write-Host "Creating Keycloak database and user..." -ForegroundColor Green

# Tạo database keycloak
docker-compose exec -T postgres psql -U postgres -c "SELECT 'CREATE DATABASE keycloak' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'keycloak')\gexec"

# Tạo user keycloak_user
docker-compose exec -T postgres psql -U postgres -c @"
DO `$`$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_user WHERE usename = 'keycloak_user') THEN
        CREATE USER keycloak_user WITH PASSWORD 'keycloak_password_123';
    ELSE
        ALTER USER keycloak_user WITH PASSWORD 'keycloak_password_123';
    END IF;
END
`$`$;
"@

# Cấp quyền trên database
docker-compose exec -T postgres psql -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE keycloak TO keycloak_user;"

# Cấp quyền trên schema
docker-compose exec -T postgres psql -U postgres -d keycloak -c "GRANT ALL ON SCHEMA public TO keycloak_user;"
docker-compose exec -T postgres psql -U postgres -d keycloak -c "ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO keycloak_user;"
docker-compose exec -T postgres psql -U postgres -d keycloak -c "ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO keycloak_user;"
docker-compose exec -T postgres psql -U postgres -d keycloak -c "ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON FUNCTIONS TO keycloak_user;"

Write-Host "Keycloak database and user created successfully!" -ForegroundColor Green
Write-Host "Database: keycloak" -ForegroundColor Yellow
Write-Host "User: keycloak_user" -ForegroundColor Yellow
Write-Host "Password: keycloak_password_123" -ForegroundColor Yellow

