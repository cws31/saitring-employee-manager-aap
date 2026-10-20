# Saitring Employee Manager (SEMA) - Modular Multi-Tenant Backend

## Overview
A production-grade, multi-tenant SaaS backend built with **Spring Boot 3.4**, **Java 21/25**, **MySQL 8**, **Redis**, and **Flyway**. Designed specifically for scaffolding, shuttering, and formwork (*saitring*) business owners to manage employees, advance balances, daily attendance, and monthly settlements.

---

## Architecture & Modular Design

The codebase strictly follows a clean modular layered architecture (`Presentation -> Service -> Repository -> Persistence`):

```
dev.sonu.saitring_employee_manager/
├── auth/                      # Authentication & Dynamic 2FA Module
│   ├── controller/            # AuthController (REST Endpoints)
│   ├── dto/                   # RegisterRequest, LoginRequest, VerifyOtpRequest, AuthResponse, etc.
│   └── service/               # AuthService, OtpService, RedisOtpStorageService
├── owner/                     # Tenant Domain Module
│   ├── controller/            # OwnerController (Profile Management)
│   ├── dto/                   # OwnerResponse, UpdateOwnerProfileRequest
│   ├── entity/                # Owner JPA Entity, OwnerStatus, OwnerRole
│   ├── repository/            # OwnerRepository
│   └── service/               # OwnerService & Implementation
├── employee/                  # Multi-Tenant Employee Management & Wage Rate History Module
│   ├── controller/            # EmployeeController (CRUD, List, Block/Unblock, Wage Rate History)
│   ├── dto/                   # CreateEmployeeRequest, UpdateEmployeeRequest, RateRevisionRequest, EmployeeResponse, EmployeeRateResponse
│   ├── entity/                # Employee, EmployeeRateHistory, EmployeeStatus
│   ├── repository/            # EmployeeRepository, EmployeeRateHistoryRepository (Tenant-scoped queries)
│   └── service/               # EmployeeService & Implementation
├── attendance/                # Multi-Tenant Daily & Bulk Attendance Module
│   ├── controller/            # AttendanceController (Mark, Bulk, Register, Summaries)
│   ├── dto/                   # MarkAttendanceRequest, BulkMarkAttendanceRequest, Register, Summaries
│   ├── entity/                # Attendance JPA Entity, AttendanceStatus (PRESENT, HALF_DAY, ABSENT)
│   ├── repository/            # AttendanceRepository (Tenant-scoped queries)
│   └── service/               # AttendanceService & Implementation
├── advance/                   # Multi-Tenant Advance Money Management Module
│   ├── controller/            # AdvanceController (Disburse, History, Register, Summaries)
│   ├── dto/                   # CreateAdvanceRequest, UpdateAdvanceRequest, Register, Summaries
│   ├── entity/                # Advance JPA Entity
│   ├── repository/            # AdvanceRepository (Tenant-scoped running advance queries)
│   └── service/               # AdvanceService & Implementation
├── hisab/                     # Multi-Tenant Monthly Running Hisab & Settlement Module
│   ├── controller/            # HisabController (Monthly Register, Settle, Employee History)
│   ├── dto/                   # HisabTableRow, MonthlyHisabTableResponse, SettleHisabRequest, HisabResponse
│   ├── entity/                # MonthlyHisab JPA Entity, HisabStatus, PaymentMode
│   ├── repository/            # HisabRepository (Tenant-scoped queries)
│   └── service/               # HisabService & Implementation
├── common/                    # Shared Cross-Cutting Infrastructure
│   ├── config/                # SecurityConfig, OpenApiConfig, RedisConfig
│   ├── exception/             # GlobalExceptionHandler, Custom AppException hierarchy
│   ├── notification/          # Brevo (Email OTP) & Twilio (SMS OTP)
│   └── storage/               # Supabase Storage (Logo upload & cleanup)
└── security/                  # Spring Security 6 & JJWT 0.12 Integration
    ├── JwtTokenProvider       # Token generation, claims, signing
    ├── JwtAuthenticationFilter# Stateless request filtering
    ├── OwnerPrincipal         # UserDetails implementation
    └── CustomUserDetailsService
```

---

## Multi-Tenancy Isolation Standards

- **Tenant Boundary**: Every operation requires authentication. The tenant identifier (`ownerId`) is derived securely from the authenticated JWT security context (`@AuthenticationPrincipal OwnerPrincipal principal`), **never** from request body parameters or path parameters.
- **Data Protection**: If an owner attempts to view, update, delete, block, or unblock an employee belonging to another owner, the system returns `404 Not Found` (rather than 403) to prevent tenant enumeration.
- **Tenant-Scoped Uniqueness**: Mobile numbers are unique **per owner tenant** (`UNIQUE(owner_id, mobile_number)`). Two different business owners can employ workers who share the same mobile number without conflict.

---

## Database Engineering & Schema (Flyway)

### Flyway Migrations
1. [`V1__create_owners.sql`](file:///d:/DOWNLOADS/freelance-projects/saitring-employee-manager/src/main/resources/db/migration/V1__create_owners.sql): Creates `owners` tenant table with authentication indexes.
2. [`V2__create_employees.sql`](file:///d:/DOWNLOADS/freelance-projects/saitring-employee-manager/src/main/resources/db/migration/V2__create_employees.sql): Creates `employees` table with foreign key to `owners(id) ON DELETE CASCADE` and tenant-scoped composite indexes.
3. [`V3__create_attendance.sql`](file:///d:/DOWNLOADS/freelance-projects/saitring-employee-manager/src/main/resources/db/migration/V3__create_attendance.sql): Creates `attendance` table with foreign keys, unique constraint on `(owner_id, employee_id, attendance_date)`, and high-performance reporting indexes.
4. [`V4__create_advances.sql`](file:///d:/DOWNLOADS/freelance-projects/saitring-employee-manager/src/main/resources/db/migration/V4__create_advances.sql): Creates `advances` table with foreign keys to owners and employees (`ON DELETE CASCADE`), positive amount constraint, and running advance tracking indexes.
5. [`V5__create_hisab.sql`](file:///d:/DOWNLOADS/freelance-projects/saitring-employee-manager/src/main/resources/db/migration/V5__create_hisab.sql): Creates `monthly_hisab` table with foreign keys, unique constraint on `(owner_id, employee_id, hisab_month)`, and settlement tracking indexes.
6. [`V6__create_employee_rate_history.sql`](file:///d:/DOWNLOADS/freelance-projects/saitring-employee-manager/src/main/resources/db/migration/V6__create_employee_rate_history.sql): Creates `employee_rate_history` table with foreign keys to owners and employees (`ON DELETE CASCADE`), effective date tracking, index optimizations, and data backfill from current employee rates.

### Table Schema: `employees`
| Column | Type | Constraints / Description |
|---|---|---|
| `id` | `BIGINT AUTO_INCREMENT` | Primary Key |
| `owner_id` | `BIGINT` | Foreign Key to `owners(id)` |
| `employee_name` | `VARCHAR(100)` | Full employee/worker name |
| `mobile_number` | `VARCHAR(20)` | Employee contact number |
| `daily_rate` | `DECIMAL(10, 2)` | Agreed daily wage/rate (rate/day) |
| `joining_date` | `DATE` | Date employee joined the business (entered by owner) |
| `status` | `VARCHAR(20)` | `ACTIVE`, `BLOCKED` (default: `ACTIVE`) |
| `created_at` | `TIMESTAMP` | Record creation timestamp |
| `updated_at` | `TIMESTAMP` | Last updated timestamp |

### Table Schema: `attendance`
| Column | Type | Constraints / Description |
|---|---|---|
| `id` | `BIGINT AUTO_INCREMENT` | Primary Key |
| `owner_id` | `BIGINT` | Foreign Key to `owners(id)` ON DELETE CASCADE |
| `employee_id` | `BIGINT` | Foreign Key to `employees(id)` ON DELETE CASCADE |
| `attendance_date` | `DATE` | Attendance date (must be on or after employee's joining date) |
| `status` | `VARCHAR(20)` | `PRESENT`, `HALF_DAY`, `ABSENT` |
| `note` | `VARCHAR(255)` | Optional note or remark |
| `created_at` | `TIMESTAMP` | Record creation timestamp |
| `updated_at` | `TIMESTAMP` | Last updated timestamp |

### Table Schema: `advances`
| Column | Type | Constraints / Description |
|---|---|---|
| `id` | `BIGINT AUTO_INCREMENT` | Primary Key |
| `owner_id` | `BIGINT` | Foreign Key to `owners(id)` ON DELETE CASCADE |
| `employee_id` | `BIGINT` | Foreign Key to `employees(id)` ON DELETE CASCADE |
| `amount` | `DECIMAL(10, 2)` | Advance money amount (positive value) |
| `advance_date` | `DATE` | Date advance disbursed (must be on or after joining date) |
| `note` | `VARCHAR(255)` | Optional note / reason for advance |
| `is_settled` | `BOOLEAN` | Whether settled in monthly hisab (default: `false`) |
| `settled_at` | `TIMESTAMP` | Timestamp when advance was settled |
| `created_at` | `TIMESTAMP` | Record creation timestamp |
| `updated_at` | `TIMESTAMP` | Last updated timestamp |

### Table Schema: `monthly_hisab`
| Column | Type | Constraints / Description |
|---|---|---|
| `id` | `BIGINT AUTO_INCREMENT` | Primary Key |
| `owner_id` | `BIGINT` | Foreign Key to `owners(id)` ON DELETE CASCADE |
| `employee_id` | `BIGINT` | Foreign Key to `employees(id)` ON DELETE CASCADE |
| `hisab_month` | `VARCHAR(7)` | Month in `YYYY-MM` format |
| `daily_rate` | `DECIMAL(10, 2)` | Snapshot daily wage rate |
| `full_days` | `INT` | Count of full days (`PRESENT`) |
| `half_days` | `INT` | Count of half days (`HALF_DAY`) |
| `effective_days` | `DOUBLE` | Total effective presenties (`full + 0.5 * half`) |
| `total_earnings` | `DECIMAL(10, 2)` | Total earnings (`daily_rate * effective_days`) |
| `total_advance` | `DECIMAL(10, 2)` | Total advance money deducted |
| `final_money` | `DECIMAL(10, 2)` | Net final payable money (`total_earnings - total_advance`) |
| `status` | `VARCHAR(20)` | `RUNNING`, `SETTLED` (default: `RUNNING`) |
| `payment_mode` | `VARCHAR(30)` | `CASH`, `UPI`, `BANK_TRANSFER`, `CHEQUE`, `OTHER` |
| `settled_at` | `TIMESTAMP` | Settlement timestamp |
| `note` | `VARCHAR(255)` | Optional settlement note / remarks |
| `created_at` | `TIMESTAMP` | Record creation timestamp |
| `updated_at` | `TIMESTAMP` | Last updated timestamp |

### Table Schema: `employee_rate_history`
| Column | Type | Constraints / Description |
|---|---|---|
| `id` | `BIGINT AUTO_INCREMENT` | Primary Key |
| `owner_id` | `BIGINT` | Foreign Key to `owners(id)` ON DELETE CASCADE |
| `employee_id` | `BIGINT` | Foreign Key to `employees(id)` ON DELETE CASCADE |
| `daily_rate` | `DECIMAL(10, 2)` | Agreed daily wage rate for this revision period |
| `effective_from` | `DATE` | Start date when this rate takes effect |
| `note` | `VARCHAR(255)` | Optional revision note / remark |
| `created_at` | `TIMESTAMP` | Record creation timestamp |
| `updated_at` | `TIMESTAMP` | Last updated timestamp |

### Indexes
- `uk_employees_owner_mobile`: Unique index on `(owner_id, mobile_number)`
- `idx_employees_owner_created_id`: Composite index on `(owner_id, created_at, id)` for stable pagination
- `idx_employees_owner_status`: Composite index on `(owner_id, status)` for status filtering
- `idx_employees_owner_name`: Composite index on `(owner_id, employee_name)` for search
- `uk_attendance_owner_emp_date`: Unique constraint on `(owner_id, employee_id, attendance_date)`
- `idx_attendance_owner_date`: Composite index on `(owner_id, attendance_date)`
- `idx_attendance_owner_emp_date`: Composite index on `(owner_id, employee_id, attendance_date)`
- `idx_attendance_owner_status_date`: Composite index on `(owner_id, status, attendance_date)`
- `idx_advances_owner_date`: Composite index on `(owner_id, advance_date)`
- `idx_advances_owner_emp`: Composite index on `(owner_id, employee_id)`
- `idx_advances_owner_emp_date`: Composite index on `(owner_id, employee_id, advance_date)`
- `idx_advances_owner_settled`: Composite index on `(owner_id, is_settled)`
- `uk_monthly_hisab_owner_emp_month`: Unique constraint on `(owner_id, employee_id, hisab_month)`
- `idx_monthly_hisab_owner_month`: Composite index on `(owner_id, hisab_month)`
- `idx_monthly_hisab_owner_emp`: Composite index on `(owner_id, employee_id)`
- `idx_monthly_hisab_owner_status`: Composite index on `(owner_id, status)`
- `idx_rate_hist_owner_emp_eff`: Composite index on `(owner_id, employee_id, effective_from DESC)` for wage history queries
- `idx_rate_hist_owner_eff`: Composite index on `(owner_id, effective_from DESC)` for tenant-wide wage lookups

---

## API Endpoints Reference

### 1. Authentication (Public)
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/v1/auth/register` | Register new owner tenant (supports Multipart or JSON) |
| `POST` | `/api/v1/auth/login` | Initiate credentials verification and dynamic 2FA dispatch |
| `POST` | `/api/v1/auth/verify-otp` | Verify OTP code and obtain JWT Bearer Token |
| `POST` | `/api/v1/auth/resend-otp` | Resend fresh OTP with cooldown |

### 2. Owner Profile (Bearer JWT Required)
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/v1/owners/profile` | Retrieve profile of authenticated owner |
| `PUT` | `/api/v1/owners/profile` | Update profile details and upload new organization logo |

### 3. Employee Management (Bearer JWT Required)
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/v1/employees` | Create employee (`employeeName`, `mobileNumber`, `dailyRate`, `joiningDate`) |
| `GET` | `/api/v1/employees/{id}` | Get employee details by ID (tenant-scoped) |
| `GET` | `/api/v1/employees` | List & search employees (paginated, status filter, name/phone search) |
| `PUT` | `/api/v1/employees/{id}` | Update employee details |
| `DELETE` | `/api/v1/employees/{id}` | Delete employee |
| `PATCH` | `/api/v1/employees/{id}/block` | Block employee (disallows new attendance & advances) |
| `PATCH` | `/api/v1/employees/{id}/unblock` | Restore employee status to `ACTIVE` |
| `POST` | `/api/v1/employees/{id}/rates` | Log new wage rate revision (`dailyRate`, `effectiveFrom`, `note`) |
| `GET` | `/api/v1/employees/{id}/rates?page=0&size=20` | Paginated wage rate revision history for an employee |

### 4. Attendance Management (Bearer JWT Required)
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/v1/attendance` | Mark or upsert attendance for employee (`employeeId`, `attendanceDate`, `status`, `note`) |
| `POST` | `/api/v1/attendance/bulk` | Mark bulk attendance for multiple employees for a specific date in one transaction |
| `GET` | `/api/v1/attendance/{id}` | Get attendance record by ID (tenant-scoped) |
| `PUT` | `/api/v1/attendance/{id}` | Update existing attendance status and note |
| `DELETE` | `/api/v1/attendance/{id}` | Delete attendance record by ID |
| `GET` | `/api/v1/attendance/register?month=YYYY-MM&page=0&size=20` | **Paginated Monthly Attendance Register Grid**: Displays active employees (joined on or before month end) with day 1..N statuses & row totals. Supports `page`, `size`, `sort`. |
| `GET` | `/api/v1/attendance/summary/daily?date=YYYY-MM-DD` | **Daily Summary**: Counts for eligible, marked, unmarked, present, half-day, absent & per-employee status list |
| `GET` | `/api/v1/attendance/summary/monthly?month=YYYY-MM` | **Monthly Summary**: Aggregate statistics, total effective days (`present + 0.5 * half_day`) and total estimated wages |
| `GET` | `/api/v1/attendance/employee/{employeeId}?month=YYYY-MM` | **Employee Monthly History**: Detailed attendance records, payable days, and earnings breakdown for an employee |

### 5. Advance Money Management (Bearer JWT Required)
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/v1/advances` | Disburse advance money to employee (`employeeId`, `amount`, `advanceDate`, `note`) |
| `GET` | `/api/v1/advances/{id}` | Get individual advance record by ID |
| `PUT` | `/api/v1/advances/{id}` | Edit existing advance amount, date, or note |
| `DELETE` | `/api/v1/advances/{id}` | Delete advance record by ID (`204 No Content`) |
| `GET` | `/api/v1/advances/register?month=YYYY-MM&page=0&size=20` | **Paginated Monthly Advance Money Register**: Lists active employees who joined on or before month-end, their monthly advances, and running balances |
| `GET` | `/api/v1/advances/employee/{employeeId}?month=YYYY-MM&page=0&size=20` | **Employee Advance History**: Paginated transactions, period total, and running advance balance |
| `GET` | `/api/v1/advances/summary/monthly?month=YYYY-MM` | **Monthly Summary**: Disbursed totals, transactions, employees assisted, and overdue prior advances |
| `GET` | `/api/v1/advances/summary/running` | **Overall Running Summary**: Organization-wide outstanding running advance and top advance holders |

### 6. Hisab Management (Bearer JWT Required)
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/v1/hisab?month=YYYY-MM&page=0&size=20` | **Monthly Running Hisab Table**: Tabular register showing `employeeName`, `fullDays`, `halfDays`, `totalPresenties`, `rate`, `totalEarnings`, `totalAdvance`, and `finalMoney` (`totalEarnings - totalAdvance`). Filtered strictly by joining date & active status with full pagination support (`page`, `size`, `sort`). |
| `POST` | `/api/v1/hisab/settle` | **Settle Monthly Hisab**: Closes hisab for an employee for a specific month with payment mode (`CASH`, `UPI`, `BANK_TRANSFER`, etc.) and marks all monthly advances up to that month as `SETTLED`. |
| `GET` | `/api/v1/hisab/employee/{employeeId}?page=0&size=12` | **Employee Settlement History**: Paginated list of past monthly hisab settlements and running advance balance for an individual employee. |

### Documentation & Actuator
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`

---

## Testing & Verification

Run the entire test suite via:
```bash
mvn test
```
**Results: 94 Tests Run, 0 Failures, 0 Errors, 0 Skipped (BUILD SUCCESS)**
- [`HisabServiceTest.java`](file:///d:/DOWNLOADS/freelance-projects/saitring-employee-manager/src/test/java/dev/sonu/saitring_employee_manager/hisab/HisabServiceTest.java) (10 Unit Tests: earnings, advances, final money calculation, joining date filter, historical wage rate lookup, settled status snapshot, settlement flow, advance closure)
- [`HisabControllerApiTest.java`](file:///d:/DOWNLOADS/freelance-projects/saitring-employee-manager/src/test/java/dev/sonu/saitring_employee_manager/hisab/HisabControllerApiTest.java) (6 End-to-end API, Multi-Tenancy Isolation, Pagination, Monthly Wage Rate Versioning & Strict Business Rule Tests)
- [`AdvanceServiceTest.java`](file:///d:/DOWNLOADS/freelance-projects/saitring-employee-manager/src/test/java/dev/sonu/saitring_employee_manager/advance/AdvanceServiceTest.java) (10 Unit Tests: joining date rule, running balance, paginated register, summaries)
- [`AdvanceControllerApiTest.java`](file:///d:/DOWNLOADS/freelance-projects/saitring-employee-manager/src/test/java/dev/sonu/saitring_employee_manager/advance/AdvanceControllerApiTest.java) (6 End-to-end API, Multi-Tenancy & Register Integration Tests)
- [`AttendanceServiceTest.java`](file:///d:/DOWNLOADS/freelance-projects/saitring-employee-manager/src/test/java/dev/sonu/saitring_employee_manager/attendance/AttendanceServiceTest.java) (12 Unit Tests)
- [`AttendanceControllerApiTest.java`](file:///d:/DOWNLOADS/freelance-projects/saitring-employee-manager/src/test/java/dev/sonu/saitring_employee_manager/attendance/AttendanceControllerApiTest.java) (7 End-to-end API Tests)
- [`EmployeeServiceTest.java`](file:///d:/DOWNLOADS/freelance-projects/saitring-employee-manager/src/test/java/dev/sonu/saitring_employee_manager/employee/EmployeeServiceTest.java) (12 Unit Tests)
- [`EmployeeControllerApiTest.java`](file:///d:/DOWNLOADS/freelance-projects/saitring-employee-manager/src/test/java/dev/sonu/saitring_employee_manager/employee/EmployeeControllerApiTest.java) (5 End-to-end API & Multi-Tenant Isolation Tests)
- [`AuthControllerApiTest.java`](file:///d:/DOWNLOADS/freelance-projects/saitring-employee-manager/src/test/java/dev/sonu/saitring_employee_manager/auth/AuthControllerApiTest.java) (7 Auth & Swagger Multipart Tests)
- [`AuthServiceTest.java`](file:///d:/DOWNLOADS/freelance-projects/saitring-employee-manager/src/test/java/dev/sonu/saitring_employee_manager/auth/AuthServiceTest.java), [`OtpServiceTest.java`](file:///d:/DOWNLOADS/freelance-projects/saitring-employee-manager/src/test/java/dev/sonu/saitring_employee_manager/auth/OtpServiceTest.java), [`OwnerServiceTest.java`](file:///d:/DOWNLOADS/freelance-projects/saitring-employee-manager/src/test/java/dev/sonu/saitring_employee_manager/owner/OwnerServiceTest.java), [`JwtTokenProviderTest.java`](file:///d:/DOWNLOADS/freelance-projects/saitring-employee-manager/src/test/java/dev/sonu/saitring_employee_manager/security/JwtTokenProviderTest.java)
