#!/bin/bash
set -e

# Sử dụng biến môi trường hoặc giá trị mặc định
KEYCLOAK_DB_NAME=${KEYCLOAK_DB_NAME:-keycloak}
KEYCLOAK_DB_USER=${KEYCLOAK_DB_USER:-keycloak_user}
KEYCLOAK_DB_PASSWORD=${KEYCLOAK_DB_PASSWORD:-keycloak_password_123}

echo "Creating Keycloak database: ${KEYCLOAK_DB_NAME}"
echo "Creating Keycloak user: ${KEYCLOAK_DB_USER}"

# Tạo database (nếu chưa tồn tại)
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    -- Tạo database cho Keycloak
    SELECT 'CREATE DATABASE ${KEYCLOAK_DB_NAME}'
    WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = '${KEYCLOAK_DB_NAME}')\gexec
EOSQL

# Tạo user và cấp quyền
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    -- Tạo user riêng cho Keycloak (nếu chưa tồn tại)
    DO \$\$
    BEGIN
        IF NOT EXISTS (SELECT FROM pg_user WHERE usename = '${KEYCLOAK_DB_USER}') THEN
            CREATE USER ${KEYCLOAK_DB_USER} WITH PASSWORD '${KEYCLOAK_DB_PASSWORD}';
        ELSE
            -- Cập nhật password nếu user đã tồn tại
            ALTER USER ${KEYCLOAK_DB_USER} WITH PASSWORD '${KEYCLOAK_DB_PASSWORD}';
        END IF;
    END
    \$\$;
    
    -- Cấp quyền cho user keycloak_user trên database keycloak
    GRANT ALL PRIVILEGES ON DATABASE ${KEYCLOAK_DB_NAME} TO ${KEYCLOAK_DB_USER};
EOSQL

# Cấp quyền trên schema
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "${KEYCLOAK_DB_NAME}" <<-EOSQL
    GRANT ALL ON SCHEMA public TO ${KEYCLOAK_DB_USER};
    ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO ${KEYCLOAK_DB_USER};
    ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO ${KEYCLOAK_DB_USER};
    ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON FUNCTIONS TO ${KEYCLOAK_DB_USER};
EOSQL

echo "Keycloak database and user created successfully!"
