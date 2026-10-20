# Saitring Employee Manager (SEMA) - Modular Multi-Tenant Backend

## Overview
A production-grade, multi-tenant SaaS backend built with **Spring Boot 3.4**, **Java 21/25**, **MySQL 8**, **Redis**, and **Flyway**. Designed specifically for scaffolding, shuttering, and formwork (*saitring*) business owners to manage employees, advance balances, daily attendance, and monthly settlements.

---

## Architecture & Modular Design

The codebase strictly follows a clean modular layered architecture (`Presentation -> Service -> Repository -> Persistence`):

```
dev.sonu.saitring_employee_manager/
├── auth/                      # Authentication & Dynamic 2FA Module
│   ├── controller/            # AuthController (REST Presentation)
│   ├── dto/                   # Request/Response Data Transfer Objects
│   └── service/               # AuthService, OtpService, RedisOtpStorageService
├── owner/                     # Tenant Domain Module
│   ├── controller/            # OwnerController (Profile Management)
│   ├── dto/                   # OwnerResponse, UpdateOwnerProfileRequest
│   ├── entity/                # Owner JPA Entity, Enums
│   ├── repository/            # OwnerRepository
│   └── service/               # OwnerService & Implementation
├── common/                    # Shared Cross-Cutting Module
│   ├── config/                # SecurityConfig, OpenApiConfig, RedisConfig
│   ├── exception/             # GlobalExceptionHandler, Custom AppException hierarchy
│   ├── notification/          # Brevo (Email OTP) & Twilio (SMS OTP)
│   └── storage/               # Supabase Storage (Logo upload & deletion)
└── security/                  # Spring Security 6 & JJWT 0.12 Integration
    ├── JwtTokenProvider       # Token generation, claims, signing
    ├── JwtAuthenticationFilter# Stateless request filtering
    ├── OwnerPrincipal         # UserDetails implementation
    └── CustomUserDetailsService
```

### Multi-Tenancy Architecture
- Each registered business owner acts as a distinct **tenant** (`owners.id`).
- Future modules (`employee`, `attendance`, `advances`, `monthly_hisab`) will be packaged into their own distinct domain folders (e.g. `dev.sonu.saitring_employee_manager.employee`), referencing `owner_id` for strict multi-tenant row-level data isolation.

---

## Database Engineering & Schema (Flyway)

Flyway migration script: [`V1__create_owners.sql`](file:///d:/DOWNLOADS/freelance-projects/saitring-employee-manager/src/main/resources/db/migration/V1__create_owners.sql)

### Table Schema: `owners`
| Column | Type | Constraints / Description |
|---|---|---|
| `id` | `BIGINT AUTO_INCREMENT` | Primary Key |
| `owner_name` | `VARCHAR(100)` | Owner's full name |
| `organization_name`| `VARCHAR(150)` | Saitring business name |
| `email` | `VARCHAR(100)` | Unique Tenant Email |
| `phone_number` | `VARCHAR(20)` | Unique Mobile Number |
| `password_hash` | `VARCHAR(255)` | BCrypt hashed password |
| `address` | `TEXT` | Physical business address |
| `logo_url` | `VARCHAR(500)` | Supabase public image URL (Optional) |
| `status` | `VARCHAR(30)` | `ACTIVE`, `INACTIVE`, `SUSPENDED` |
| `role` | `VARCHAR(30)` | `ROLE_OWNER` |
| `created_at` | `TIMESTAMP` | UTC Creation timestamp |
| `updated_at` | `TIMESTAMP` | UTC Last update timestamp |

### Performance Optimization Indexes
- **`uk_owners_email`**: Unique index on `email` for $O(1)$ authentication lookup.
- **`uk_owners_phone_number`**: Unique index on `phone_number` for $O(1)$ phone authentication lookup.
- **`idx_owners_created_at_id`**: Composite index on `(created_at, id)` for stable ordered pagination.
- **`idx_owners_status_created_at_id`**: Composite index on `(status, created_at, id)` for status-filtered queries.

---

## Dynamic 2FA Authentication Flow

1. **Owner Registration**:
   - Accepts owner details and optional organization logo (via `multipart/form-data` or `application/json`).
   - If a logo is provided, it is securely uploaded to **Supabase Storage**.
2. **Login (Step 1)**:
   - Client sends credentials (`identifier` + `password`).
   - If logged in via **Email** -> Generates 6-digit OTP -> Sent via **Brevo** to registered email.
   - If logged in via **Mobile Number** -> Generates 6-digit OTP -> Sent via **Twilio** to registered mobile phone.
   - Challenge stored in **Redis** with a 5-minute TTL, attempt limit, and replay protection.
3. **Verify OTP (Step 2)**:
   - Client submits `challengeToken` and the 6-digit `otp`.
   - Upon verification, challenge is invalidated immediately.
   - Issues **JWT Bearer Token** with owner tenant claims.
4. **Resend OTP**:
   - Allows resending OTP with an enforced 30-second cooldown protection.

---

## API Endpoints Reference

### Authentication Endpoints (Public)
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/v1/auth/register` | Register new owner tenant (supports Multipart or JSON) |
| `POST` | `/api/v1/auth/login` | Initiate credentials verification and dynamic 2FA dispatch |
| `POST` | `/api/v1/auth/verify-otp` | Verify OTP code and obtain JWT Bearer Token |
| `POST` | `/api/v1/auth/resend-otp` | Resend fresh OTP with rate limit cooldown |

### Owner Profile Endpoints (Requires `Authorization: Bearer <token>`)
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/v1/owners/profile` | Retrieve profile of authenticated owner |
| `PUT` | `/api/v1/owners/profile` | Update profile details and upload new organization logo |

### Documentation & Actuator
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`
- **Health Check**: `http://localhost:8080/actuator/health`

---

## External Services Configuration

Environment variables configured in `.env` or application runtime:
- **Brevo**: `BREVO_API_KEY`, `BREVO_SENDER_EMAIL`, `BREVO_SENDER_NAME`
- **Twilio**: `TWILIO_ACCOUNT_SID`, `TWILIO_AUTH_TOKEN`, `TWILIO_FROM_NUMBER`
- **Supabase Storage**: `SUPABASE_URL`, `SUPABASE_KEY`, `SUPABASE_BUCKET`
- **Database & Redis**: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `UPSTASH_REDIS_URL`

*(When running locally or during tests without active external provider keys, the system gracefully logs simulated dispatches without crashing).*

---

## Testing Verification

The module has comprehensive multi-tiered testing:
- **Unit Tests**: [`AuthServiceTest`](file:///d:/DOWNLOADS/freelance-projects/saitring-employee-manager/src/test/java/dev/sonu/saitring_employee_manager/auth/AuthServiceTest.java), [`OtpServiceTest`](file:///d:/DOWNLOADS/freelance-projects/saitring-employee-manager/src/test/java/dev/sonu/saitring_employee_manager/auth/OtpServiceTest.java), [`OwnerServiceTest`](file:///d:/DOWNLOADS/freelance-projects/saitring-employee-manager/src/test/java/dev/sonu/saitring_employee_manager/owner/OwnerServiceTest.java), [`JwtTokenProviderTest`](file:///d:/DOWNLOADS/freelance-projects/saitring-employee-manager/src/test/java/dev/sonu/saitring_employee_manager/security/JwtTokenProviderTest.java)
- **Integration & API Tests**: [`AuthControllerApiTest`](file:///d:/DOWNLOADS/freelance-projects/saitring-employee-manager/src/test/java/dev/sonu/saitring_employee_manager/auth/AuthControllerApiTest.java)

Run all tests via:
```bash
mvn clean test
```
Result: **24 Tests Run, 0 Failures, 0 Errors, 0 Skipped (BUILD SUCCESS)**
