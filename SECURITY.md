# Security Policy & Vulnerability Reporting

## 1. Supported Versions

| Version | Supported | Security Update Status |
|---|---|---|
| `0.0.1-SNAPSHOT` (Current) | :white_check_mark: | Active Development & Patches |

---

## 2. Reporting a Security Vulnerability

We prioritize the security of tenant data, workforce wage records, and financial accounting ledgers. If you discover a vulnerability or security defect:

1. **Do Not Open Public Disclosures**: Please do not file public issues or disclose vulnerability details publicly before a resolution has been engineered.
2. **Direct Reporting**: Submit a detailed vulnerability report to the engineering security contact:
   - **Email**: `sema.servicesupport@gmail.com`
3. **Report Contents**:
   - Detailed description of the vulnerability (e.g., IDOR, SQL injection, authentication bypass, cryptographic defect).
   - Step-by-step reproduction instructions or a minimal proof of concept (PoC).
   - Potential impact on tenant isolation or data confidentiality.
4. **Response Timeline**:
   - Initial acknowledgement: within 24 hours.
   - Impact assessment and fix triage: within 72 hours.

---

## 3. Cryptographic & Security Baseline

- **Password Storage**: BCrypt with salted work factor $\ge 10$.
- **Session Tokens**: Stateless HMAC-SHA256 JWTs using JJWT 0.12 with minimum 256-bit entropy keys.
- **Two-Factor Authentication**: Dynamic 6-digit cryptographic OTPs generated via `java.security.SecureRandom` stored in Redis with 300-second TTL and brute-force attempt limits.
- **Tenant Isolation**: Row-level tenant partitioning verified by multi-tenant integration test suites.
