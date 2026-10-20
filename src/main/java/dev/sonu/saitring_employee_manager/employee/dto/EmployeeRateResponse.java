package dev.sonu.saitring_employee_manager.employee.dto;

import dev.sonu.saitring_employee_manager.employee.entity.EmployeeRateHistory;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Schema(description = "Response representing an employee wage rate log entry")
public class EmployeeRateResponse {

    @Schema(description = "Rate history record ID", example = "1")
    private Long id;

    @Schema(description = "Employee ID", example = "101")
    private Long employeeId;

    @Schema(description = "Daily wage rate", example = "650.00")
    private BigDecimal dailyRate;

    @Schema(description = "Date from which this rate is effective", example = "2026-10-01")
    private LocalDate effectiveFrom;

    @Schema(description = "Optional note / remarks", example = "Skill revision")
    private String note;

    @Schema(description = "Timestamp when recorded")
    private Instant createdAt;

    public EmployeeRateResponse() {
    }

    public EmployeeRateResponse(Long id, Long employeeId, BigDecimal dailyRate, LocalDate effectiveFrom, String note, Instant createdAt) {
        this.id = id;
        this.employeeId = employeeId;
        this.dailyRate = dailyRate;
        this.effectiveFrom = effectiveFrom;
        this.note = note;
        this.createdAt = createdAt;
    }

    public static EmployeeRateResponse fromEntity(EmployeeRateHistory entity) {
        return new EmployeeRateResponse(
                entity.getId(),
                entity.getEmployee().getId(),
                entity.getDailyRate(),
                entity.getEffectiveFrom(),
                entity.getNote(),
                entity.getCreatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public BigDecimal getDailyRate() {
        return dailyRate;
    }

    public LocalDate getEffectiveFrom() {
        return effectiveFrom;
    }

    public String getNote() {
        return note;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
