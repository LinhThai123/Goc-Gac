-- Script tạo database và user cho Keycloak
-- Script này sẽ chạy tự động khi PostgreSQL container khởi động lần đầu

-- Tạo database keycloak (nếu chưa tồn tại)
SELECT 'CREATE DATABASE keycloak'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'keycloak')\gexec

-- Tạo user keycloak_user (nếu chưa tồn tại)
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_user WHERE usename = 'keycloak_user') THEN
        CREATE USER keycloak_user WITH PASSWORD 'keycloak_password_123';
    END IF;
END
$$;

-- Cấp quyền cho user keycloak_user trên database keycloak
GRANT ALL PRIVILEGES ON DATABASE keycloak TO keycloak_user;

-- Kết nối vào database keycloak và cấp quyền schema
\c keycloak

GRANT ALL ON SCHEMA public TO keycloak_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO keycloak_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO keycloak_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON FUNCTIONS TO keycloak_user;

