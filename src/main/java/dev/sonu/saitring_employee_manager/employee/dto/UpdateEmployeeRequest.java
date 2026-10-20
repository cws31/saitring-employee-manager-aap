package dev.sonu.saitring_employee_manager.employee.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public class UpdateEmployeeRequest {

    @NotBlank(message = "Employee name is required")
    @Size(min = 2, max = 100, message = "Employee name must be between 2 and 100 characters")
    @Schema(description = "Updated full name of the employee", example = "Raju Shinde")
    private String employeeName;

    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Mobile number must be valid with 10 to 15 digits")
    @Schema(description = "Updated contact phone number", example = "+919876543210")
    private String mobileNumber;

    @NotNull(message = "Daily rate is required")
    @DecimalMin(value = "0.01", message = "Daily rate must be greater than zero")
    @Digits(integer = 8, fraction = 2, message = "Daily rate must be a valid monetary amount")
    @Schema(description = "Updated daily wage/rate (rate per day)", example = "700.00")
    private BigDecimal dailyRate;

    @NotNull(message = "Joining date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Updated joining date (YYYY-MM-DD)", example = "2026-03-15")
    private LocalDate joiningDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Date from which this new daily rate takes effect. If omitted, defaults to joining date or current date", example = "2026-10-01")
    private LocalDate effectiveFrom;

    public UpdateEmployeeRequest() {
    }

    public UpdateEmployeeRequest(String employeeName, String mobileNumber, BigDecimal dailyRate, LocalDate joiningDate) {
        this(employeeName, mobileNumber, dailyRate, joiningDate, null);
    }

    public UpdateEmployeeRequest(String employeeName, String mobileNumber, BigDecimal dailyRate, LocalDate joiningDate, LocalDate effectiveFrom) {
        this.employeeName = employeeName;
        this.mobileNumber = mobileNumber;
        this.dailyRate = dailyRate;
        this.joiningDate = joiningDate;
        this.effectiveFrom = effectiveFrom;
    }

    public LocalDate getEffectiveFrom() {
        return effectiveFrom;
    }

    public void setEffectiveFrom(LocalDate effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
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

    public BigDecimal getDailyRate() {
        return dailyRate;
    }

    public void setDailyRate(BigDecimal dailyRate) {
        this.dailyRate = dailyRate;
    }

    public LocalDate getJoiningDate() {
        return joiningDate;
    }

    public void setJoiningDate(LocalDate joiningDate) {
        this.joiningDate = joiningDate;
    }
}
