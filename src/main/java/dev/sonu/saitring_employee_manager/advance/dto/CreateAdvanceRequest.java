package dev.sonu.saitring_employee_manager.advance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Request body to disburse advance money to an employee")
public class CreateAdvanceRequest {

    @NotNull(message = "Employee ID is required")
    @Schema(description = "ID of the employee", example = "1")
    private Long employeeId;

    @NotNull(message = "Advance amount is required")
    @DecimalMin(value = "0.01", message = "Advance amount must be greater than zero")
    @Schema(description = "Advance money amount in rupees", example = "500.00")
    private BigDecimal amount;

    @NotNull(message = "Advance date is required")
    @Schema(description = "Date advance was given (YYYY-MM-DD)", example = "2026-10-20")
    private LocalDate advanceDate;

    @Size(max = 255, message = "Note cannot exceed 255 characters")
    @Schema(description = "Optional note or reason for advance", example = "Festival advance")
    private String note;

    public CreateAdvanceRequest() {
    }

    public CreateAdvanceRequest(Long employeeId, BigDecimal amount, LocalDate advanceDate, String note) {
        this.employeeId = employeeId;
        this.amount = amount;
        this.advanceDate = advanceDate;
        this.note = note;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
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
}
