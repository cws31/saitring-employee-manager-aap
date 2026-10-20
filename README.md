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
├── employee/                  # Multi-Tenant Employee Management Module
│   ├── controller/            # EmployeeController (CRUD, List, Block/Unblock)
│   ├── dto/                   # CreateEmployeeRequest, UpdateEmployeeRequest, EmployeeResponse
│   ├── entity/                # Employee JPA Entity, EmployeeStatus
│   ├── repository/            # EmployeeRepository (Tenant-scoped queries)
│   └── service/               # EmployeeService & Implementation
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

### Indexes
- `uk_employees_owner_mobile`: Unique index on `(owner_id, mobile_number)`
- `idx_employees_owner_created_id`: Composite index on `(owner_id, created_at, id)` for stable pagination
- `idx_employees_owner_status`: Composite index on `(owner_id, status)` for status filtering
- `idx_employees_owner_name`: Composite index on `(owner_id, employee_name)` for search

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

### Documentation & Actuator
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`

---

## Testing & Verification

Run the entire test suite via:
```bash
mvn clean test
```
**Results: 42 Tests Run, 0 Failures, 0 Errors, 0 Skipped (BUILD SUCCESS)**
- [`EmployeeServiceTest.java`](file:///d:/DOWNLOADS/freelance-projects/saitring-employee-manager/src/test/java/dev/sonu/saitring_employee_manager/employee/EmployeeServiceTest.java) (12 Unit Tests)
- [`EmployeeControllerApiTest.java`](file:///d:/DOWNLOADS/freelance-projects/saitring-employee-manager/src/test/java/dev/sonu/saitring_employee_manager/employee/EmployeeControllerApiTest.java) (4 End-to-end API & Multi-Tenant Isolation Tests)
- [`AuthControllerApiTest.java`](file:///d:/DOWNLOADS/freelance-projects/saitring-employee-manager/src/test/java/dev/sonu/saitring_employee_manager/auth/AuthControllerApiTest.java) (7 Auth & Swagger Multipart Tests)
- [`AuthServiceTest.java`](file:///d:/DOWNLOADS/freelance-projects/saitring-employee-manager/src/test/java/dev/sonu/saitring_employee_manager/auth/AuthServiceTest.java), [`OtpServiceTest.java`](file:///d:/DOWNLOADS/freelance-projects/saitring-employee-manager/src/test/java/dev/sonu/saitring_employee_manager/auth/OtpServiceTest.java), [`OwnerServiceTest.java`](file:///d:/DOWNLOADS/freelance-projects/saitring-employee-manager/src/test/java/dev/sonu/saitring_employee_manager/owner/OwnerServiceTest.java), [`JwtTokenProviderTest.java`](file:///d:/DOWNLOADS/freelance-projects/saitring-employee-manager/src/test/java/dev/sonu/saitring_employee_manager/security/JwtTokenProviderTest.java)
