package dev.sonu.saitring_employee_manager.employee.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CreateEmployeeRequest {

    @NotBlank(message = "Employee name is required")
    @Size(min = 2, max = 100, message = "Employee name must be between 2 and 100 characters")
    @Schema(description = "Full name of the employee/worker", example = "Raju Shinde")
    private String employeeName;

    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Mobile number must be valid with 10 to 15 digits")
    @Schema(description = "Contact phone number of the employee", example = "+919876543210")
    private String mobileNumber;

    @NotNull(message = "Daily rate is required")
    @DecimalMin(value = "0.01", message = "Daily rate must be greater than zero")
    @Digits(integer = 8, fraction = 2, message = "Daily rate must be a valid monetary amount")
    @Schema(description = "Agreed daily wage/rate (rate per day) for the employee", example = "650.00")
    private BigDecimal dailyRate;

    @NotNull(message = "Joining date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Date the employee joined the organization (YYYY-MM-DD)", example = "2026-03-15")
    private LocalDate joiningDate;

    public CreateEmployeeRequest() {
    }

    public CreateEmployeeRequest(String employeeName, String mobileNumber, BigDecimal dailyRate, LocalDate joiningDate) {
        this.employeeName = employeeName;
        this.mobileNumber = mobileNumber;
        this.dailyRate = dailyRate;
        this.joiningDate = joiningDate;
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
