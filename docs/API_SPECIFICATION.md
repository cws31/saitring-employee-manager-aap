# REST API Contract & Endpoint Specification

## 1. Overview & Transport Protocol

- **Base URL**: `http://localhost:8080/api/v1`
- **Protocol**: HTTP/1.1 & HTTP/2 over TLS/HTTPS
- **Content-Type**: `application/json` (or `multipart/form-data` for file upload routes)
- **OpenAPI / Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI Schema Definition**: `http://localhost:8080/v3/api-docs`

---

## 2. Authentication & Authorization

All non-auth endpoints require a standard Bearer JWT header:

```http
Authorization: Bearer <eyJhbGciOiJIUzI1NiJ9...>
```

### 2.1 Standard Error Envelope
When a request fails, the API responds with a structured RFC 7807-compliant JSON error object:

```json
{
  "timestamp": "2026-10-22T22:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Cannot settle hisab before employee joining date [2026-11-01].",
  "path": "/api/v1/hisab/settle",
  "fieldErrors": [
    {
      "field": "settlementAmount",
      "message": "Settlement amount cannot be negative"
    }
  ]
}
```

---

## 3. Resource Endpoints Catalog

### 3.1 Authentication (`/api/v1/auth`)

#### `POST /api/v1/auth/register`
Creates a new tenant owner organization. Supports both `multipart/form-data` (with an optional logo file) and `application/json`.
- **Request Body**:
  ```json
  {
    "ownerName": "Ramesh Kumar",
    "organizationName": "Ramesh Scaffolding Works",
    "email": "ramesh@saitring.com",
    "phoneNumber": "+919876543210",
    "password": "StrongPassword123!",
    "address": "Plot 42, Industrial Area, Sector 6, Gurugram"
  }
  ```
- **Response `201 Created`**:
  ```json
  {
    "id": 1,
    "ownerName": "Ramesh Kumar",
    "organizationName": "Ramesh Scaffolding Works",
    "email": "ramesh@saitring.com",
    "phoneNumber": "+919876543210",
    "address": "Plot 42, Industrial Area, Sector 6, Gurugram",
    "logoUrl": null,
    "status": "ACTIVE",
    "role": "ROLE_OWNER"
  }
  ```

#### `POST /api/v1/auth/login`
Validates credentials and dispatches a dynamic 6-digit OTP code to the owner's registered email/phone.
- **Request Body**:
  ```json
  {
    "email": "ramesh@saitring.com",
    "password": "StrongPassword123!"
  }
  ```
- **Response `200 OK`**:
  ```json
  {
    "challengeId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "channel": "EMAIL",
    "maskedDestination": "r*****h@saitring.com",
    "expiresInSeconds": 300,
    "message": "OTP sent successfully. Please verify within 5 minutes."
  }
  ```

#### `POST /api/v1/auth/verify-otp`
Validates the dynamic OTP challenge and yields the final JWT bearer token.
- **Request Body**:
  ```json
  {
    "challengeId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "otpCode": "615785"
  }
  ```
- **Response `200 OK`**:
  ```json
  {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400,
    "owner": {
      "id": 1,
      "ownerName": "Ramesh Kumar",
      "organizationName": "Ramesh Scaffolding Works",
      "email": "ramesh@saitring.com",
      "phoneNumber": "+919876543210"
    }
  }
  ```

#### `POST /api/v1/auth/resend-otp`
Re-dispatches an OTP code subject to a 60-second cooldown period.

---

### 3.2 Owner Profile (`/api/v1/owners`)

#### `GET /api/v1/owners/profile`
Retrieves tenant owner profile based on JWT authentication.

#### `PUT /api/v1/owners/profile`
Updates profile metadata and handles organization logo upload (`multipart/form-data`).

---

### 3.3 Employee Master & Rate History (`/api/v1/employees`)

| Method | Endpoint | Query Parameters | Description |
|---|---|---|---|
| `POST` | `/api/v1/employees` | - | Create new worker profile. Enforces tenant-scoped mobile uniqueness. |
| `GET` | `/api/v1/employees` | `page`, `size`, `sort`, `search`, `status` | Paginated roster with keyword search and status filtering. |
| `GET` | `/api/v1/employees/{id}` | - | Retrieve worker details. Throws `404` on cross-tenant access. |
| `PUT` | `/api/v1/employees/{id}` | - | Update worker name, mobile, or initial joining details. |
| `DELETE` | `/api/v1/employees/{id}` | - | Cascade deletes employee, their attendance, advances, and hisab history. |
| `PATCH` | `/api/v1/employees/{id}/block` | - | Block employee; prevents future attendance and advances. |
| `PATCH` | `/api/v1/employees/{id}/unblock` | - | Re-activate blocked employee. |
| `POST` | `/api/v1/employees/{id}/rates` | - | Append a new temporal wage rate revision (`dailyRate`, `effectiveFrom`, `note`). |
| `GET` | `/api/v1/employees/{id}/rates` | `page`, `size` | Paginated chronological wage rate history. |

---

### 3.4 Attendance Management (`/api/v1/attendance`)

#### `POST /api/v1/attendance`
Marks or upserts single-day attendance for an employee.
- **Request Body**:
  ```json
  {
    "employeeId": 101,
    "attendanceDate": "2026-10-15",
    "status": "PRESENT",
    "note": "Overtime shuttering shift"
  }
  ```

#### `POST /api/v1/attendance/bulk`
Marks or updates attendance for multiple workers in a single atomic database transaction.
- **Request Body**:
  ```json
  {
    "attendanceDate": "2026-10-15",
    "records": [
      { "employeeId": 101, "status": "PRESENT", "note": "" },
      { "employeeId": 102, "status": "HALF_DAY", "note": "Left at lunch" },
      { "employeeId": 103, "status": "ABSENT", "note": "Sick leave" }
    ]
  }
  ```

#### `GET /api/v1/attendance/register?month=YYYY-MM&page=0&size=20`
Returns the **Paginated Monthly Attendance Register Grid**.
- Returns day-by-day status matrix (`day1` through `day31`) for all active workers who joined on or before month end.
- Calculates row summaries: `presentDays`, `halfDays`, `absentDays`, `effectiveDays`.

#### `GET /api/v1/attendance/summary/daily?date=YYYY-MM-DD`
Roll-call summary for a specific calendar day:
- `eligibleEmployees`, `markedEmployees`, `unmarkedEmployees`
- `presentCount`, `halfDayCount`, `absentCount`
- Detailed worker status list.

#### `GET /api/v1/attendance/summary/monthly?month=YYYY-MM`
Monthly aggregate attendance statistics:
- `totalEffectiveDays`
- `totalEstimatedWageLiability`

---

### 3.5 Advance Money Management (`/api/v1/advances`)

#### `POST /api/v1/advances`
Disburses cash advance to an active employee.
- **Request Body**:
  ```json
  {
    "employeeId": 101,
    "amount": 1500.00,
    "advanceDate": "2026-10-10",
    "note": "Family emergency travel"
  }
  ```

#### `GET /api/v1/advances/register?month=YYYY-MM&page=0&size=20`
Monthly advance register displaying month advances and cumulative running balances per worker.

#### `GET /api/v1/advances/employee/{employeeId}?month=YYYY-MM&page=0&size=20`
Paginated advance disbursement history for an individual worker.

#### `GET /api/v1/advances/summary/running`
Overall organization-wide outstanding advance balance and top advance holders list.

---

### 3.6 Hisab & Monthly Settlement (`/api/v1/hisab`)

#### `GET /api/v1/hisab?month=YYYY-MM&page=0&size=20`
Retrieves the **Paginated Monthly Running Hisab Register Grid**.
- Dynamically resolves historical effective wage rates as of the month end.
- Injects opening balance carry-forwards from prior settled months.
- Computes gross earnings, total advance deductions, and net final money.
- **Response `200 OK`**:
  ```json
  {
    "month": "2026-10",
    "rows": [
      {
        "employeeId": 101,
        "employeeName": "Gopal Das",
        "mobileNumber": "+919876543211",
        "rate": 600.00,
        "fullDays": 24,
        "halfDays": 2,
        "totalPresenties": 25.0,
        "totalEarnings": 15000.00,
        "openingBalance": 1000.00,
        "monthlyAdvance": 3000.00,
        "totalAdvance": 2000.00,
        "finalMoney": 13000.00,
        "settlementAmount": null,
        "carryForward": null,
        "status": "RUNNING",
        "runningAdvanceBalance": 2000.00
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 48,
    "totalPages": 3,
    "monthTotals": {
      "totalEmployees": 48,
      "totalEarnings": 684000.00,
      "totalAdvances": 142000.00,
      "netFinalMoney": 542000.00
    }
  }
  ```

#### `POST /api/v1/hisab/settle`
Executes month-end financial settlement for a worker.
- **Request Body**:
  ```json
  {
    "employeeId": 101,
    "month": "2026-10",
    "settlementAmount": 10000.00,
    "paymentMode": "BANK_TRANSFER",
    "note": "Direct NEFT payment; 3000.00 carried forward to November"
  }
  ```
- **Response `200 OK`**:
  ```json
  {
    "id": 85,
    "employeeId": 101,
    "employeeName": "Gopal Das",
    "hisabMonth": "2026-10",
    "dailyRate": 600.00,
    "fullDays": 24,
    "halfDays": 2,
    "effectiveDays": 25.0,
    "openingBalance": 1000.00,
    "totalEarnings": 15000.00,
    "totalAdvance": 2000.00,
    "finalMoney": 13000.00,
    "settlementAmount": 10000.00,
    "carryForward": 3000.00,
    "status": "SETTLED",
    "paymentMode": "BANK_TRANSFER",
    "settledAt": "2026-10-22T22:30:00Z",
    "note": "Direct NEFT payment; 3000.00 carried forward to November"
  }
  ```

#### `GET /api/v1/hisab/employee/{employeeId}?page=0&size=12`
Historical settlements record for a specific employee across calendar months.
