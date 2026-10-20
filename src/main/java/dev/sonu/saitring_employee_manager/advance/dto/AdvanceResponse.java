package dev.sonu.saitring_employee_manager.advance.dto;

import dev.sonu.saitring_employee_manager.advance.entity.Advance;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Schema(description = "Response representing an advance transaction")
public class AdvanceResponse {

    @Schema(description = "Advance record ID", example = "1")
    private Long id;

    @Schema(description = "Employee ID", example = "10")
    private Long employeeId;

    @Schema(description = "Employee Name", example = "Ramesh Kumar")
    private String employeeName;

    @Schema(description = "Employee Mobile Number", example = "+919876543210")
    private String mobileNumber;

    @Schema(description = "Advance amount", example = "500.00")
    private BigDecimal amount;

    @Schema(description = "Advance date", example = "2026-10-20")
    private LocalDate advanceDate;

    @Schema(description = "Optional note or reason", example = "Festival advance")
    private String note;

    @Schema(description = "Whether this advance has been settled in monthly hisab", example = "false")
    private boolean isSettled;

    @Schema(description = "Timestamp when this advance was settled")
    private Instant settledAt;

    @Schema(description = "Creation timestamp")
    private Instant createdAt;

    @Schema(description = "Last update timestamp")
    private Instant updatedAt;

    public AdvanceResponse() {
    }

    public static AdvanceResponse fromEntity(Advance advance) {
        AdvanceResponse dto = new AdvanceResponse();
        dto.setId(advance.getId());
        if (advance.getEmployee() != null) {
            dto.setEmployeeId(advance.getEmployee().getId());
            dto.setEmployeeName(advance.getEmployee().getEmployeeName());
            dto.setMobileNumber(advance.getEmployee().getMobileNumber());
        }
        dto.setAmount(advance.getAmount());
        dto.setAdvanceDate(advance.getAdvanceDate());
        dto.setNote(advance.getNote());
        dto.setSettled(advance.isSettled());
        dto.setSettledAt(advance.getSettledAt());
        dto.setCreatedAt(advance.getCreatedAt());
        dto.setUpdatedAt(advance.getUpdatedAt());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
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
}
