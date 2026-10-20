package dev.sonu.saitring_employee_manager.advance.entity;

import dev.sonu.saitring_employee_manager.employee.entity.Employee;
import dev.sonu.saitring_employee_manager.owner.entity.Owner;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(
        name = "advances",
        indexes = {
                @Index(name = "idx_advances_owner_date", columnList = "owner_id, advance_date"),
                @Index(name = "idx_advances_owner_emp", columnList = "owner_id, employee_id"),
                @Index(name = "idx_advances_owner_emp_date", columnList = "owner_id, employee_id, advance_date"),
                @Index(name = "idx_advances_owner_settled", columnList = "owner_id, is_settled")
        }
)
public class Advance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private Owner owner;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "advance_date", nullable = false)
    private LocalDate advanceDate;

    @Column(name = "note", length = 255)
    private String note;

    @Column(name = "is_settled", nullable = false)
    private boolean isSettled = false;

    @Column(name = "settled_at")
    private Instant settledAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Advance() {
    }

    public Advance(Owner owner, Employee employee, BigDecimal amount, LocalDate advanceDate, String note) {
        this.owner = owner;
        this.employee = employee;
        this.amount = amount;
        this.advanceDate = advanceDate;
        this.note = note;
        this.isSettled = false;
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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getAdvanceDate() {
        return advanceDate;
    }

    public void setAdvanceDate(LocalDate advanceDate) {
        this.advanceDate = advanceDate;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public boolean isSettled() {
        return isSettled;
    }

    public void setSettled(boolean settled) {
        isSettled = settled;
    }

    public Instant getSettledAt() {
        return settledAt;
    }

    public void setSettledAt(Instant settledAt) {
        this.settledAt = settledAt;
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
        Advance advance = (Advance) o;
        return Objects.equals(id, advance.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
