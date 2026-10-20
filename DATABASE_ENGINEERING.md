# Database Engineering and Performance Guide

## Owner table indexes

The initial migration `V1__create_owners.sql` creates:
- Primary key on `id`
- Unique index on `email`
- Unique index on `phone_number`
- `idx_owners_created_at_id (created_at, id)` for stable ordered listing
- `idx_owners_status_created_at_id (status, created_at, id)` for future status-filtered listing

Because the application has not been run and this migration has not been applied, these indexes are included directly in V1. Do not add a separate V2 migration for these same indexes.

The two listing indexes are forward-looking. Keep them only if the application will use those query patterns; indexes increase storage and write costs. Authentication lookups are already covered by the unique email and phone indexes.

## Multi-tenant indexing standard for future modules

Use tenant identifiers in all tenant-scoped indexes and predicates. Examples below are design patterns, not migrations to run without checking the actual schema:

```sql
-- Tenant-scoped employee listing
CREATE INDEX idx_employees_owner_created_id
    ON employees (owner_id, created_at, id);

-- Mobile number unique inside each tenant, not globally
CREATE UNIQUE INDEX uk_employees_owner_mobile
    ON employees (owner_id, mobile);

-- Attendance by tenant, employee, and date range
CREATE INDEX idx_attendance_owner_employee_date
    ON attendance (owner_id, employee_id, attendance_date);

-- Advances by tenant, employee, and date range
CREATE INDEX idx_advances_owner_employee_date
    ON advances (owner_id, employee_id, advance_date);

-- One month closing per tenant/month, if this matches the domain rules
CREATE UNIQUE INDEX uk_month_closing_owner_month
    ON month_closings (owner_id, closing_month);
```

Adapt names, types, uniqueness rules, and existing constraints to the real tables. If child rows derive tenant ownership through a parent, decide deliberately whether to store `owner_id` redundantly or join through the parent; enforce consistency either way.

## Query design rules

1. Derive tenant identity from the authenticated security context; never authorize based on a client-supplied owner ID.
2. For composite indexes, put equality-filter columns first, followed by range/order columns. Confirm the real query shape first.
3. Use deterministic ordering with a unique tie-breaker such as `id`.
4. Prefer keyset pagination for large lists rather than deep `OFFSET`; cap page size.
5. Return DTO projections with only needed fields for list screens; avoid returning entities or large payloads.
6. Avoid N+1 queries using projections, intentional fetch joins, or batch fetching; do not make all relationships eager.
7. Keep database transactions short; avoid remote API calls inside long-running transactions.
8. Use database constraints (unique, foreign key, NOT NULL and appropriate checks) as well as application validation.
9. Keep `spring.jpa.hibernate.ddl-auto=validate`. Use Flyway for schema changes.
10. Use UTC timestamps consistently.
11. Configure Hikari connection pool sizes based on database capacity and deployment replica count.
12. Cache only deliberately. OTP challenge keys should be short-lived and must not be confused with authorization data caches.

## Query plan review

For each high-volume query:
1. Capture the actual SQL and representative parameters.
2. Run `EXPLAIN ANALYZE` on MySQL 8.0.18+ in a safe environment.
3. Inspect chosen indexes, rows examined, sorting, and join order.
4. Compare against realistic data volume and concurrency.
5. Re-check after data and workload growth.

Do not claim an index improves performance until query plans and workload measurements support it. Avoid premature partitioning.

## Production database safeguards

- Add foreign keys and deliberate deletion rules for child tables.
- Consider `@Version` for concurrently edited records.
- Add consistent audit fields and explicit correction history for important financial records.
- Add MySQL integration tests for tenant isolation, unique constraints, migration startup, and concurrent writes.
- Monitor slow queries, pool exhaustion, lock waits, deadlocks, table growth, and migration duration.
