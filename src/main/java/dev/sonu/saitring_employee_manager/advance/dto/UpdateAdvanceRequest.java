package dev.sonu.saitring_employee_manager.advance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Request body to edit an existing advance money record")
public class UpdateAdvanceRequest {

    @NotNull(message = "Advance amount is required")
    @DecimalMin(value = "0.01", message = "Advance amount must be greater than zero")
    @Schema(description = "Updated advance money amount", example = "600.00")
    private BigDecimal amount;

    @NotNull(message = "Advance date is required")
    @Schema(description = "Updated date of advance (YYYY-MM-DD)", example = "2026-10-21")
    private LocalDate advanceDate;

    @Size(max = 255, message = "Note cannot exceed 255 characters")
    @Schema(description = "Updated optional note or remark", example = "Corrected advance amount")
    private String note;

    public UpdateAdvanceRequest() {
    }

    public UpdateAdvanceRequest(BigDecimal amount, LocalDate advanceDate, String note) {
        this.amount = amount;
        this.advanceDate = advanceDate;
        this.note = note;
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
