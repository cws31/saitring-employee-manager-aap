CREATE TABLE IF NOT EXISTS advances (
    id BIGINT NOT NULL AUTO_INCREMENT,
    owner_id BIGINT NOT NULL,
    employee_id BIGINT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    advance_date DATE NOT NULL,
    note VARCHAR(255) NULL,
    is_settled BOOLEAN NOT NULL DEFAULT FALSE,
    settled_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_advances PRIMARY KEY (id),
    CONSTRAINT fk_advances_owner FOREIGN KEY (owner_id) REFERENCES owners (id) ON DELETE CASCADE,
    CONSTRAINT fk_advances_employee FOREIGN KEY (employee_id) REFERENCES employees (id) ON DELETE CASCADE,
    CONSTRAINT chk_advance_amount_positive CHECK (amount > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_advances_owner_date ON advances (owner_id, advance_date);
CREATE INDEX idx_advances_owner_emp ON advances (owner_id, employee_id);
CREATE INDEX idx_advances_owner_emp_date ON advances (owner_id, employee_id, advance_date);
CREATE INDEX idx_advances_owner_settled ON advances (owner_id, is_settled);
