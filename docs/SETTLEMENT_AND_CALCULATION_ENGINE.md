# Financial Settlement & Calculation Engine Specification

## 1. Executive Summary

The **Hisab & Settlement Engine** is the core computational domain of the Saitring Employee Manager. In the construction, shuttering, and scaffolding industry, workers receive unpredictable cash advances during ongoing work, experience shifts between full-day and half-day duty, and frequently have their agreed daily wage rates revised as their responsibilities evolve.

This specification documents the mathematical models, temporal resolution algorithms, ledger balancing rules, and cyclic carry-forward propagation mechanics implemented in [`HisabServiceImpl.java`](file:///d:/DOWNLOADS/freelance-projects/saitring-employee-manager/src/main/java/dev/sonu/saitring_employee_manager/hisab/service/HisabServiceImpl.java).

---

## 2. Mathematical Formulations

### 2.1 Effective Attendance Calculation
Attendance is tracked daily per employee. A day can have one of three statuses:
- $\text{PRESENT}$ ($1.0$ work unit)
- $\text{HALF\_DAY}$ ($0.5$ work unit)
- $\text{ABSENT}$ ($0.0$ work unit)

For any given calendar month $M$ bounded between $[D_{start}, D_{end}]$:

$$E = D_{full} + 0.5 \times D_{half}$$

Where:
- $D_{full} = \sum [status = \text{PRESENT}]$
- $D_{half} = \sum [status = \text{HALF\_DAY}]$
- $E$ is the total effective payable days (stored as `DECIMAL(5, 2)`).

---

### 2.2 Temporal Wage Rate Resolution
Workers often receive wage increments or adjustments midway through their tenure. Rather than overwriting the worker's base rate, every rate change is logged in `employee_rate_history` with an `effective_from` date.

When calculating the hisab for month $M$ (ending on date $D_{end}$):

$$R_{\text{effective}} = \text{daily\_rate from } \text{employee\_rate\_history}$$
$$\text{where } \text{effective\_from} \le D_{end} \text{ ORDER BY } \text{effective\_from} \text{ DESC LIMIT 1}$$

If no historical rate revision exists on or before $D_{end}$, the calculation falls back to the employee's initial `daily_rate`.

---

### 2.3 Gross Earnings Computation
The total gross earnings $W$ for the month are computed using half-up arithmetic:

$$W = \text{round}_{2}(E \times R_{\text{effective}})$$

---

### 2.4 Advance Money Deductions & Opening Balance
Advances are cash disbursements given to employees ahead of monthly settlement.

1. **Monthly Advance Total ($A_{month}$)**:
   $$A_{month} = \sum_{k \in \text{Advances}(M)} \text{amount}_k$$

2. **Opening Balance ($O_{balance}$)**:
   The opening balance is derived directly from the prior settled month's carry-forward balance ($C_{prior}$):
   $$O_{balance} = \begin{cases} C_{prior}, & \text{if prior settled month exists} \\ 0.00, & \text{otherwise} \end{cases}$$

3. **Total Deductions ($A_{total}$)**:
   Total deductions balance out advances taken during the month against the prior carry-forward:
   $$A_{total} = \text{round}_{2}(A_{month} - O_{balance})$$

   > [!NOTE]
   > - If $O_{balance} > 0$ (the company owed money to the employee from the previous month), it reduces the employee's net advance liability.
   > - If $O_{balance} < 0$ (the employee was overdrawn or owed advance money), $- (-O_{balance})$ automatically adds the outstanding debit to the current month's deductions.

---

### 2.5 Net Final Money ($F$)
The net payable amount prior to actual cash disbursement is:

$$F = \text{round}_{2}(W - A_{total})$$

Expanded:
$$F = W - (A_{month} - O_{balance}) = W - A_{month} + O_{balance}$$

---

### 2.6 Settlement Clearance & Carry-Forward ($C$)
When an owner initiates a settlement transaction (`POST /api/v1/hisab/settle`):
- $S$ is the actual cash amount paid out (`settlementAmount`). If not specified by the owner, $S = F$ (full settlement).
- $C$ is the remaining carry-forward balance:

$$C = \text{round}_{2}(F - S)$$

All advances belonging to month $M$ are updated atomically:
$$\text{is\_settled} \leftarrow \text{true}, \quad \text{settled\_at} \leftarrow \text{CURRENT\_TIMESTAMP}$$

The resulting `MonthlyHisab` record transitions to `SETTLED` and becomes immutable.

---

## 3. Concrete Multi-Month Cyclic Chain Example

Consider an employee **Gopal Das** with an initial rate of **\$500.00/day**, joining on `2026-08-01`.

### Cycle 1: Month `2026-08` (Partial Payout)
* **Attendance**: 20 full days, 2 half days $\implies E = 20 + 0.5 \times 2 = 21.0\text{ days}$.
* **Effective Rate**: $\$500.00/\text{day}$.
* **Gross Earnings**: $W = 21.0 \times 500 = \$10,500.00$.
* **Advances**: Worker took $\$3,000.00$ in cash advances during August.
* **Opening Balance**: No prior record $\implies O_{balance} = \$0.00$.
* **Total Deductions**: $A_{total} = \$3,000.00 - \$0.00 = \$3,000.00$.
* **Final Money**: $F = \$10,500.00 - \$3,000.00 = \$7,500.00$.
* **Owner Action**: Owner pays **\$5,000.00** in cash ($S = \$5,000.00$).
* **Carry-Forward**: 
  $$C = \$7,500.00 - \$5,000.00 = +\$2,500.00 \quad \text{(Credit owed to Gopal)}$$

---

### Cycle 2: Month `2026-09` (Rate Increment + Full Payout)
* **Wage Revision**: Rate increased to **\$600.00/day** effective `2026-09-01`.
* **Attendance**: 24 full days, 0 half days $\implies E = 24.0\text{ days}$.
* **Effective Rate**: $\$600.00/\text{day}$.
* **Gross Earnings**: $W = 24.0 \times 600 = \$14,400.00$.
* **Advances**: Worker took $\$4,000.00$ in cash advances during September.
* **Opening Balance**: Inherited from August $C_{prior} = +\$2,500.00$.
* **Total Deductions**: 
  $$A_{total} = \$4,000.00 - \$2,500.00 = \$1,500.00$$
* **Final Money**: 
  $$F = \$14,400.00 - \$1,500.00 = \$12,900.00$$
* **Owner Action**: Owner pays full final balance **\$12,900.00** via Bank Transfer ($S = \$12,900.00$).
* **Carry-Forward**: 
  $$C = \$12,900.00 - \$12,900.00 = \$0.00$$

---

### Cycle 3: Month `2026-10` (Excess Advances / Debit Carry-Forward)
* **Attendance**: 10 full days, 0 half days $\implies E = 10.0\text{ days}$.
* **Effective Rate**: $\$600.00/\text{day}$.
* **Gross Earnings**: $W = 10.0 \times 600 = \$6,000.00$.
* **Advances**: Worker took emergency advances of $\$8,000.00$.
* **Opening Balance**: Inherited from September $C_{prior} = \$0.00$.
* **Total Deductions**: 
  $$A_{total} = \$8,000.00 - \$0.00 = \$8,000.00$$
* **Final Money**: 
  $$F = \$6,000.00 - \$8,000.00 = -\$2,000.00 \quad \text{(Worker owes money)}$$
* **Owner Action**: Settled with $\$0.00$ payout ($S = \$0.00$).
* **Carry-Forward**: 
  $$C = -\$2,000.00 - \$0.00 = -\$2,000.00 \quad \text{(Debit carry-forward to November)}$$

---

## 4. Ledger Invariants & Architectural Guarantees

1. **Deterministic Rounding**: All calculations strictly enforce `BigDecimal.setScale(2, RoundingMode.HALF_UP)` to avoid IEEE 754 floating-point drift.
2. **Snapshot Immutability**: Once a hisab record is settled (`status = 'SETTLED'`), the snapshot captures `daily_rate`, `full_days`, `half_days`, `effective_days`, `opening_balance`, `total_earnings`, `total_advance`, `final_money`, `settlement_amount`, and `carry_forward`. Subsequent rate changes or attendance adjustments do NOT mutate historical hisab records.
3. **Temporal Ordering & Precedence**: Settlements must occur sequentially or reference historical carry-forwards deterministically via `ORDER BY hisab_month DESC LIMIT 1`.
4. **Tenant Isolation Enforcement**: An owner can only query and settle balances for workers within their authenticated tenant boundary (`owner_id = principal.getOwnerId()`).
