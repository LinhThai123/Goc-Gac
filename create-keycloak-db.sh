#!/bin/bash
# Script tạo database Keycloak thủ công
# Chạy script này nếu database chưa được tạo tự động

echo "Creating Keycloak database and user..."

docker-compose exec -T postgres psql -U postgres <<EOF
-- Tạo database keycloak (nếu chưa tồn tại)
SELECT 'CREATE DATABASE keycloak'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'keycloak')\gexec

-- Tạo user keycloak_user (nếu chưa tồn tại)
DO \$\$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_user WHERE usename = 'keycloak_user') THEN
        CREATE USER keycloak_user WITH PASSWORD 'keycloak_password_123';
    ELSE
        ALTER USER keycloak_user WITH PASSWORD 'keycloak_password_123';
    END IF;
END
\$\$;

-- Cấp quyền cho user keycloak_user trên database keycloak
GRANT ALL PRIVILEGES ON DATABASE keycloak TO keycloak_user;
EOF

echo "Connecting to keycloak database to grant schema permissions..."

docker-compose exec -T postgres psql -U postgres -d keycloak <<EOF
GRANT ALL ON SCHEMA public TO keycloak_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO keycloak_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO keycloak_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON FUNCTIONS TO keycloak_user;
EOF

echo "Keycloak database and user created successfully!"
echo "Database: keycloak"
echo "User: keycloak_user"
echo "Password: keycloak_password_123"

