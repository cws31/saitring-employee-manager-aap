package dev.sonu.saitring_employee_manager.hisab.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Employee hisab history across months")
public class EmployeeHisabHistoryResponse {

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

    @Schema(description = "Cumulative unsettled running advance balance", example = "4500.00")
    private BigDecimal runningAdvanceBalance;

    @Schema(description = "Paginated list of monthly hisab settlements")
    private Page<HisabResponse> settlements;

    public EmployeeHisabHistoryResponse() {
    }

    public EmployeeHisabHistoryResponse(Long employeeId, String employeeName, String mobileNumber,
                                        BigDecimal dailyRate, LocalDate joiningDate,
                                        BigDecimal runningAdvanceBalance, Page<HisabResponse> settlements) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.mobileNumber = mobileNumber;
        this.dailyRate = dailyRate;
        this.joiningDate = joiningDate;
        this.runningAdvanceBalance = runningAdvanceBalance;
        this.settlements = settlements;
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

    public Page<HisabResponse> getSettlements() {
        return settlements;
    }

    public void setSettlements(Page<HisabResponse> settlements) {
        this.settlements = settlements;
    }
}
