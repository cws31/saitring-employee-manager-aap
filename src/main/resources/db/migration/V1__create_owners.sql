CREATE TABLE IF NOT EXISTS owners (
    id BIGINT NOT NULL AUTO_INCREMENT,
    owner_name VARCHAR(100) NOT NULL,
    organization_name VARCHAR(150) NOT NULL,
    email VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    address TEXT NOT NULL,
    logo_url VARCHAR(500) NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    role VARCHAR(30) NOT NULL DEFAULT 'ROLE_OWNER',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_owners PRIMARY KEY (id),
    CONSTRAINT uk_owners_email UNIQUE (email),
    CONSTRAINT uk_owners_phone_number UNIQUE (phone_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_owners_created_at_id ON owners (created_at, id);
CREATE INDEX idx_owners_status_created_at_id ON owners (status, created_at, id);
