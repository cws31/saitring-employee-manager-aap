package dev.sonu.saitring_employee_manager.advance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Detailed advance history and running advance balance for a specific employee")
public class EmployeeAdvanceHistoryResponse {

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

    @Schema(description = "Cumulative unsettled running advance balance", example = "3500.00")
    private BigDecimal runningAdvanceBalance;

    @Schema(description = "Filter month (if specified, YYYY-MM)", example = "2026-10")
    private String month;

    @Schema(description = "Total advance disbursed in the filtered period", example = "1500.00")
    private BigDecimal periodTotal;

    @Schema(description = "Paginated list of advance records")
    private Page<AdvanceResponse> advances;

    public EmployeeAdvanceHistoryResponse() {
    }

    public EmployeeAdvanceHistoryResponse(Long employeeId, String employeeName, String mobileNumber,
                                          BigDecimal dailyRate, LocalDate joiningDate,
                                          BigDecimal runningAdvanceBalance, String month, BigDecimal periodTotal,
                                          Page<AdvanceResponse> advances) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.mobileNumber = mobileNumber;
        this.dailyRate = dailyRate;
        this.joiningDate = joiningDate;
        this.runningAdvanceBalance = runningAdvanceBalance;
        this.month = month;
        this.periodTotal = periodTotal;
        this.advances = advances;
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

    public BigDecimal getRunningAdvanceBalance() {
        return runningAdvanceBalance;
    }

    public void setRunningAdvanceBalance(BigDecimal runningAdvanceBalance) {
        this.runningAdvanceBalance = runningAdvanceBalance;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public BigDecimal getPeriodTotal() {
        return periodTotal;
    }

    public void setPeriodTotal(BigDecimal periodTotal) {
        this.periodTotal = periodTotal;
    }

    public Page<AdvanceResponse> getAdvances() {
        return advances;
    }

    public void setAdvances(Page<AdvanceResponse> advances) {
        this.advances = advances;
    }
}
