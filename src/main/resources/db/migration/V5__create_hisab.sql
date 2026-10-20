CREATE TABLE IF NOT EXISTS monthly_hisab (
    id BIGINT NOT NULL AUTO_INCREMENT,
    owner_id BIGINT NOT NULL,
    employee_id BIGINT NOT NULL,
    hisab_month VARCHAR(7) NOT NULL,
    daily_rate DECIMAL(10, 2) NOT NULL,
    full_days INT NOT NULL DEFAULT 0,
    half_days INT NOT NULL DEFAULT 0,
    effective_days DECIMAL(5, 2) NOT NULL DEFAULT 0.00,
    total_earnings DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    total_advance DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    final_money DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    status VARCHAR(20) NOT NULL DEFAULT 'RUNNING',
    payment_mode VARCHAR(30) NULL,
    settled_at TIMESTAMP NULL,
    note VARCHAR(255) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_monthly_hisab PRIMARY KEY (id),
    CONSTRAINT fk_monthly_hisab_owner FOREIGN KEY (owner_id) REFERENCES owners (id) ON DELETE CASCADE,
    CONSTRAINT fk_monthly_hisab_employee FOREIGN KEY (employee_id) REFERENCES employees (id) ON DELETE CASCADE,
    CONSTRAINT uk_monthly_hisab_owner_emp_month UNIQUE (owner_id, employee_id, hisab_month)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_monthly_hisab_owner_month ON monthly_hisab (owner_id, hisab_month);
CREATE INDEX idx_monthly_hisab_owner_emp ON monthly_hisab (owner_id, employee_id);
CREATE INDEX idx_monthly_hisab_owner_status ON monthly_hisab (owner_id, status);
