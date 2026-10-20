package dev.sonu.saitring_employee_manager.advance.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Running advance summary for an individual employee")
public class EmployeeRunningAdvanceItem {

    @Schema(description = "Employee ID", example = "1")
    private Long employeeId;

    @Schema(description = "Employee Name", example = "Ramesh Kumar")
    private String employeeName;

    @Schema(description = "Mobile Number", example = "+919876543210")
    private String mobileNumber;

    @Schema(description = "Daily Wage Rate", example = "600.00")
    private BigDecimal dailyRate;

    @Schema(description = "Joining Date", example = "2026-09-01")
    private LocalDate joiningDate;

    @Schema(description = "Outstanding unsettled running advance", example = "4500.00")
    private BigDecimal runningAdvance;

    public EmployeeRunningAdvanceItem() {
    }

    public EmployeeRunningAdvanceItem(Long employeeId, String employeeName, String mobileNumber,
                                      BigDecimal dailyRate, LocalDate joiningDate, BigDecimal runningAdvance) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.mobileNumber = mobileNumber;
        this.dailyRate = dailyRate;
        this.joiningDate = joiningDate;
        this.runningAdvance = runningAdvance;
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

    public BigDecimal getRunningAdvance() {
        return runningAdvance;
    }

    public void setRunningAdvance(BigDecimal runningAdvance) {
        this.runningAdvance = runningAdvance;
    }
}
