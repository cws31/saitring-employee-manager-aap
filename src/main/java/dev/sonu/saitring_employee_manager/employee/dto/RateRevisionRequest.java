package dev.sonu.saitring_employee_manager.employee.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Request body to log or revise an employee daily wage rate")
public class RateRevisionRequest {

    @NotNull(message = "Daily rate is required")
    @DecimalMin(value = "0.01", message = "Daily rate must be greater than zero")
    @Digits(integer = 8, fraction = 2, message = "Daily rate must be a valid monetary amount with up to 2 decimal places")
    @Schema(description = "Revised daily wage rate (rate per day)", example = "650.00")
    private BigDecimal dailyRate;

    @NotNull(message = "Effective date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Date from which this new daily rate takes effect (YYYY-MM-DD)", example = "2026-10-01")
    private LocalDate effectiveFrom;

    @Size(max = 255, message = "Note cannot exceed 255 characters")
    @Schema(description = "Optional reason or note for rate change", example = "Diwali increment / skill promotion")
    private String note;

    public RateRevisionRequest() {
    }

    public RateRevisionRequest(BigDecimal dailyRate, LocalDate effectiveFrom, String note) {
        this.dailyRate = dailyRate;
        this.effectiveFrom = effectiveFrom;
        this.note = note;
    }

    public BigDecimal getDailyRate() {
        return dailyRate;
    }

    public void setDailyRate(BigDecimal dailyRate) {
        this.dailyRate = dailyRate;
    }

    public LocalDate getEffectiveFrom() {
        return effectiveFrom;
    }

    public void setEffectiveFrom(LocalDate effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
