package dev.sonu.saitring_employee_manager.advance.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "Single employee row in the monthly advance money register")
public class AdvanceRegisterEmployeeRow {

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

    @Schema(description = "List of all advances given to this employee in this month")
    private List<AdvanceResponse> monthlyAdvances;

    @Schema(description = "Total advance money disbursed to this employee in this month", example = "1500.00")
    private BigDecimal monthlyTotal;

    @Schema(description = "Cumulative total unsettled running advance balance for this employee", example = "4500.00")
    private BigDecimal runningAdvanceBalance;

    public AdvanceRegisterEmployeeRow() {
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

    public List<AdvanceResponse> getMonthlyAdvances() {
        return monthlyAdvances;
    }

    public void setMonthlyAdvances(List<AdvanceResponse> monthlyAdvances) {
        this.monthlyAdvances = monthlyAdvances;
    }

    public BigDecimal getMonthlyTotal() {
        return monthlyTotal;
    }

    public void setMonthlyTotal(BigDecimal monthlyTotal) {
        this.monthlyTotal = monthlyTotal;
    }

    public BigDecimal getRunningAdvanceBalance() {
        return runningAdvanceBalance;
    }

    public void setRunningAdvanceBalance(BigDecimal runningAdvanceBalance) {
        this.runningAdvanceBalance = runningAdvanceBalance;
    }
}
