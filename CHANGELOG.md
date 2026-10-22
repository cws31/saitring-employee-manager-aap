# Changelog

All notable engineering changes to the **Saitring Employee Manager (SEMA)** backend service are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

---

## [0.0.1-SNAPSHOT] - 2026-10-22

### Added
- **Core Multi-Tenant Architecture**:
  - Row-level tenant partitioning anchored by `owners` tenant entity (`V1__create_owners.sql`).
  - Strict security context isolation via `OwnerPrincipal` and `@AuthenticationPrincipal`.
  - Anti-enumeration IDOR defense returning `404 Not Found` for cross-tenant resource requests.
- **Dynamic 2FA Authentication Subsystem**:
  - Two-stage authentication with password verification and Redis-backed 6-digit OTP challenges.
  - Multi-channel notification dispatch supporting Brevo (Email/SMS) and Twilio (SMS).
  - Brute-force throttling (max 3 failed attempts) and resend cooldown timers.
  - Stateless JWT token issuing via JJWT 0.12 with HMAC-SHA256 signatures.
- **Workforce Management & Temporal Wage Revision Engine**:
  - Employee master entity with tenant-scoped mobile uniqueness (`V2__create_employees.sql`).
  - Active/Blocked employee status lifecycle control.
  - Temporal daily wage revision catalog with effective-date tracking (`V6__create_employee_rate_history.sql`).
- **Attendance Register Ledger**:
  - Daily attendance tracking supporting `PRESENT` (1.0), `HALF_DAY` (0.5), and `ABSENT` (0.0) (`V3__create_attendance.sql`).
  - Atomic bulk attendance marking API for organization-wide daily roll-calls.
  - Paginated monthly attendance register grid returning day 1..31 matrices with calculated row totals.
  - Daily and monthly aggregate attendance wage liability summaries.
- **Advance Money Ledger**:
  - Micro-advance cash disbursement tracking with positive amount validation (`V4__create_advances.sql`).
  - Running unsettled advance balance tracking per worker and organization-wide.
  - Paginated monthly advance register and employee transaction history.
- **Financial Hisab & Settlement Engine**:
  - Monthly running hisab computation module dynamically resolving effective wage rates, effective days, and running advances (`V5__create_hisab.sql`).
  - Partial payout and excess advance clearance with multi-month cyclic carry-forward propagation (`V7__add_settlement_clearance_columns.sql`).
  - Automatic advance closure on settlement (`is_settled = true`, `settled_at = now()`).
- **Standardized Infrastructure & Observability**:
  - Comprehensive documentation suite under `docs/` (`ARCHITECTURE.md`, `API_SPECIFICATION.md`, `DATABASE_DESIGN.md`, `SETTLEMENT_AND_CALCULATION_ENGINE.md`, `SECURITY_AND_MULTI_TENANCY.md`, `DEPLOYMENT_AND_OPERATIONS.md`).
  - Production multi-stage `Dockerfile` and local multi-service `docker-compose.yml` (MySQL 8 + Redis).
  - Template configuration `.env.example` and `.editorconfig`.
  - Spring Boot Actuator health, info, and metrics telemetry.
  - 99/99 automated unit and integration tests passing.
