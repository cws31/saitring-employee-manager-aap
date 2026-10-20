CREATE TABLE IF NOT EXISTS attendance (
    id BIGINT NOT NULL AUTO_INCREMENT,
    owner_id BIGINT NOT NULL,
    employee_id BIGINT NOT NULL,
    attendance_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    note VARCHAR(255) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_attendance PRIMARY KEY (id),
    CONSTRAINT fk_attendance_owner FOREIGN KEY (owner_id) REFERENCES owners (id) ON DELETE CASCADE,
    CONSTRAINT fk_attendance_employee FOREIGN KEY (employee_id) REFERENCES employees (id) ON DELETE CASCADE,
    CONSTRAINT uk_attendance_owner_emp_date UNIQUE (owner_id, employee_id, attendance_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_attendance_owner_date ON attendance (owner_id, attendance_date);
CREATE INDEX idx_attendance_owner_emp_date ON attendance (owner_id, employee_id, attendance_date);
CREATE INDEX idx_attendance_owner_status_date ON attendance (owner_id, status, attendance_date);
