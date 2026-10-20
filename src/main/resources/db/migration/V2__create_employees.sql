CREATE TABLE IF NOT EXISTS employees (
    id BIGINT NOT NULL AUTO_INCREMENT,
    owner_id BIGINT NOT NULL,
    employee_name VARCHAR(100) NOT NULL,
    mobile_number VARCHAR(20) NOT NULL,
    daily_rate DECIMAL(10, 2) NOT NULL,
    joining_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_employees PRIMARY KEY (id),
    CONSTRAINT fk_employees_owner FOREIGN KEY (owner_id) REFERENCES owners (id) ON DELETE CASCADE,
    CONSTRAINT uk_employees_owner_mobile UNIQUE (owner_id, mobile_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_employees_owner_created_id ON employees (owner_id, created_at, id);
CREATE INDEX idx_employees_owner_status ON employees (owner_id, status);
CREATE INDEX idx_employees_owner_name ON employees (owner_id, employee_name);
