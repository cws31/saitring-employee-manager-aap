package dev.sonu.saitring_employee_manager.attendance.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "Detailed monthly attendance and wage summary for a single employee")
public class EmployeeMonthlySummaryResponse {

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

    @Schema(description = "Month in YYYY-MM format", example = "2026-10")
    private String month;

    @Schema(description = "Total days present in this month", example = "22")
    private int totalPresent;

    @Schema(description = "Total half-days in this month", example = "2")
    private int totalHalfDay;

    @Schema(description = "Total days absent in this month", example = "2")
    private int totalAbsent;

    @Schema(description = "Total effective payable days (present + 0.5 * halfDay)", example = "23.0")
    private double effectiveDays;

    @Schema(description = "Estimated salary/wages for this month (effectiveDays * dailyRate)", example = "13800.00")
    private BigDecimal estimatedSalary;

    @Schema(description = "List of all marked attendance records for this employee in this month")
    private List<AttendanceResponse> attendanceHistory;

    public EmployeeMonthlySummaryResponse() {
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

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public int getTotalPresent() {
        return totalPresent;
    }

    public void setTotalPresent(int totalPresent) {
        this.totalPresent = totalPresent;
    }

    public int getTotalHalfDay() {
        return totalHalfDay;
    }

    public void setTotalHalfDay(int totalHalfDay) {
        this.totalHalfDay = totalHalfDay;
    }

    public int getTotalAbsent() {
        return totalAbsent;
    }

    public void setTotalAbsent(int totalAbsent) {
        this.totalAbsent = totalAbsent;
    }

    public double getEffectiveDays() {
        return effectiveDays;
    }

    public void setEffectiveDays(double effectiveDays) {
        this.effectiveDays = effectiveDays;
    }

    public BigDecimal getEstimatedSalary() {
        return estimatedSalary;
    }

    public void setEstimatedSalary(BigDecimal estimatedSalary) {
        this.estimatedSalary = estimatedSalary;
    }

    public List<AttendanceResponse> getAttendanceHistory() {
        return attendanceHistory;
    }

    public void setAttendanceHistory(List<AttendanceResponse> attendanceHistory) {
        this.attendanceHistory = attendanceHistory;
    }
}
