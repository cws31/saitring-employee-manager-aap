CREATE TABLE IF NOT EXISTS employee_rate_history (
    id BIGINT NOT NULL AUTO_INCREMENT,
    owner_id BIGINT NOT NULL,
    employee_id BIGINT NOT NULL,
    daily_rate DECIMAL(10, 2) NOT NULL,
    effective_from DATE NOT NULL,
    note VARCHAR(255) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_employee_rate_history PRIMARY KEY (id),
    CONSTRAINT fk_employee_rate_history_owner FOREIGN KEY (owner_id) REFERENCES owners (id) ON DELETE CASCADE,
    CONSTRAINT fk_employee_rate_history_employee FOREIGN KEY (employee_id) REFERENCES employees (id) ON DELETE CASCADE,
    CONSTRAINT chk_employee_rate_positive CHECK (daily_rate > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_employee_rate_history_lookup ON employee_rate_history (owner_id, employee_id, effective_from DESC);
CREATE INDEX idx_employee_rate_history_owner ON employee_rate_history (owner_id);

-- Backfill initial rate history for any existing employees
INSERT INTO employee_rate_history (owner_id, employee_id, daily_rate, effective_from, note, created_at)
SELECT owner_id, id, daily_rate, joining_date, 'Initial wage rate upon joining', created_at
FROM employees;
