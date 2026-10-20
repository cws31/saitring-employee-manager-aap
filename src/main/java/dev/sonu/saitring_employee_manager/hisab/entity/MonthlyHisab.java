package dev.sonu.saitring_employee_manager.hisab.entity;

import dev.sonu.saitring_employee_manager.employee.entity.Employee;
import dev.sonu.saitring_employee_manager.owner.entity.Owner;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(
        name = "monthly_hisab",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_monthly_hisab_owner_emp_month", columnNames = {"owner_id", "employee_id", "hisab_month"})
        },
        indexes = {
                @Index(name = "idx_monthly_hisab_owner_month", columnList = "owner_id, hisab_month"),
                @Index(name = "idx_monthly_hisab_owner_emp", columnList = "owner_id, employee_id"),
                @Index(name = "idx_monthly_hisab_owner_status", columnList = "owner_id, status")
        }
)
public class MonthlyHisab {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private Owner owner;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "hisab_month", nullable = false, length = 7)
    private String month;

    @Column(name = "daily_rate", nullable = false, precision = 10, scale = 2)
    private BigDecimal dailyRate;

    @Column(name = "full_days", nullable = false)
    private int fullDays;

    @Column(name = "half_days", nullable = false)
    private int halfDays;

    @Column(name = "effective_days", nullable = false, precision = 5, scale = 2)
    private BigDecimal effectiveDays;

    @Column(name = "total_earnings", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalEarnings;

    @Column(name = "total_advance", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAdvance;

    @Column(name = "final_money", nullable = false, precision = 10, scale = 2)
    private BigDecimal finalMoney;

    @Column(name = "opening_balance", nullable = false, precision = 10, scale = 2)
    private BigDecimal openingBalance = BigDecimal.ZERO;

    @Column(name = "settlement_amount", precision = 10, scale = 2)
    private BigDecimal settlementAmount;

    @Column(name = "carry_forward", nullable = false, precision = 10, scale = 2)
    private BigDecimal carryForward = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private HisabStatus status = HisabStatus.RUNNING;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_mode", length = 30)
    private PaymentMode paymentMode;

    @Column(name = "settled_at")
    private Instant settledAt;

    @Column(name = "note", length = 255)
    private String note;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public MonthlyHisab() {
    }

    public MonthlyHisab(Owner owner, Employee employee, String month, BigDecimal dailyRate,
                        int fullDays, int halfDays, BigDecimal effectiveDays,
                        BigDecimal totalEarnings, BigDecimal totalAdvance, BigDecimal finalMoney,
                        HisabStatus status, PaymentMode paymentMode, Instant settledAt, String note) {
        this(owner, employee, month, dailyRate, fullDays, halfDays, effectiveDays,
                BigDecimal.ZERO, totalEarnings, totalAdvance, finalMoney,
                finalMoney, BigDecimal.ZERO,
                status, paymentMode, settledAt, note);
    }

    public MonthlyHisab(Owner owner, Employee employee, String month, BigDecimal dailyRate,
                        int fullDays, int halfDays, double effectiveDays,
                        BigDecimal totalEarnings, BigDecimal totalAdvance, BigDecimal finalMoney,
                        HisabStatus status, PaymentMode paymentMode, Instant settledAt, String note) {
        this(owner, employee, month, dailyRate, fullDays, halfDays,
                BigDecimal.valueOf(effectiveDays).setScale(2, RoundingMode.HALF_UP),
                BigDecimal.ZERO, totalEarnings, totalAdvance, finalMoney,
                finalMoney, BigDecimal.ZERO,
                status, paymentMode, settledAt, note);
    }

    public MonthlyHisab(Owner owner, Employee employee, String month, BigDecimal dailyRate,
                        int fullDays, int halfDays, BigDecimal effectiveDays,
                        BigDecimal openingBalance, BigDecimal totalEarnings, BigDecimal totalAdvance, BigDecimal finalMoney,
                        BigDecimal settlementAmount, BigDecimal carryForward,
                        HisabStatus status, PaymentMode paymentMode, Instant settledAt, String note) {
        this.owner = owner;
        this.employee = employee;
        this.month = month;
        this.dailyRate = dailyRate;
        this.fullDays = fullDays;
        this.halfDays = halfDays;
        this.effectiveDays = effectiveDays;
        this.openingBalance = openingBalance != null ? openingBalance : BigDecimal.ZERO;
        this.totalEarnings = totalEarnings;
        this.totalAdvance = totalAdvance;
        this.finalMoney = finalMoney;
        this.settlementAmount = settlementAmount;
        this.carryForward = carryForward != null ? carryForward : BigDecimal.ZERO;
        this.status = status;
        this.paymentMode = paymentMode;
        this.settledAt = settledAt;
        this.note = note;
    }

    public MonthlyHisab(Owner owner, Employee employee, String month, BigDecimal dailyRate,
                        int fullDays, int halfDays, double effectiveDays,
                        BigDecimal openingBalance, BigDecimal totalEarnings, BigDecimal totalAdvance, BigDecimal finalMoney,
                        BigDecimal settlementAmount, BigDecimal carryForward,
                        HisabStatus status, PaymentMode paymentMode, Instant settledAt, String note) {
        this(owner, employee, month, dailyRate, fullDays, halfDays,
                BigDecimal.valueOf(effectiveDays).setScale(2, RoundingMode.HALF_UP),
                openingBalance, totalEarnings, totalAdvance, finalMoney,
                settlementAmount, carryForward,
                status, paymentMode, settledAt, note);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Owner getOwner() {
        return owner;
    }

    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public BigDecimal getDailyRate() {
        return dailyRate;
    }

    public void setDailyRate(BigDecimal dailyRate) {
        this.dailyRate = dailyRate;
    }

    public int getFullDays() {
        return fullDays;
    }

    public void setFullDays(int fullDays) {
        this.fullDays = fullDays;
    }

    public int getHalfDays() {
        return halfDays;
    }

    public void setHalfDays(int halfDays) {
        this.halfDays = halfDays;
    }

    public BigDecimal getEffectiveDays() {
        return effectiveDays;
    }

    public void setEffectiveDays(BigDecimal effectiveDays) {
        this.effectiveDays = effectiveDays;
    }

    public void setEffectiveDays(double effectiveDays) {
        this.effectiveDays = BigDecimal.valueOf(effectiveDays).setScale(2, RoundingMode.HALF_UP);
    }

    public double getEffectiveDaysAsDouble() {
        return effectiveDays != null ? effectiveDays.doubleValue() : 0.0;
    }

    public BigDecimal getTotalEarnings() {
        return totalEarnings;
    }

    public void setTotalEarnings(BigDecimal totalEarnings) {
        this.totalEarnings = totalEarnings;
    }

    public BigDecimal getTotalAdvance() {
        return totalAdvance;
    }

    public void setTotalAdvance(BigDecimal totalAdvance) {
        this.totalAdvance = totalAdvance;
    }

    public BigDecimal getFinalMoney() {
        return finalMoney;
    }

    public void setFinalMoney(BigDecimal finalMoney) {
        this.finalMoney = finalMoney;
    }

    public BigDecimal getOpeningBalance() {
        return openingBalance;
    }

    public void setOpeningBalance(BigDecimal openingBalance) {
        this.openingBalance = openingBalance != null ? openingBalance : BigDecimal.ZERO;
    }

    public BigDecimal getSettlementAmount() {
        return settlementAmount;
    }

    public void setSettlementAmount(BigDecimal settlementAmount) {
        this.settlementAmount = settlementAmount;
    }

    public BigDecimal getCarryForward() {
        return carryForward;
    }

    public void setCarryForward(BigDecimal carryForward) {
        this.carryForward = carryForward != null ? carryForward : BigDecimal.ZERO;
    }

    public HisabStatus getStatus() {
        return status;
    }

    public void setStatus(HisabStatus status) {
        this.status = status;
    }

    public PaymentMode getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(PaymentMode paymentMode) {
        this.paymentMode = paymentMode;
    }

    public Instant getSettledAt() {
        return settledAt;
    }

    public void setSettledAt(Instant settledAt) {
        this.settledAt = settledAt;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MonthlyHisab that = (MonthlyHisab) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
