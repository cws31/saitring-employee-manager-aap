package dev.sonu.saitring_employee_manager.attendance.dto;

import dev.sonu.saitring_employee_manager.attendance.entity.AttendanceStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Schema(description = "Single employee row in the monthly attendance register")
public class RegisterEmployeeRow {

    @Schema(description = "Employee ID", example = "1")
    private Long employeeId;

    @Schema(description = "Employee Name", example = "Ramesh Kumar")
    private String employeeName;

    @Schema(description = "Employee Mobile Number", example = "+919876543210")
    private String mobileNumber;

    @Schema(description = "Employee Daily Rate", example = "600.00")
    private BigDecimal dailyRate;

    @Schema(description = "Employee Joining Date", example = "2026-09-01")
    private LocalDate joiningDate;

    @Schema(description = "Map of day-of-month (1..31) to attendance status (PRESENT, HALF_DAY, ABSENT). Null/absent if unmarked")
    private Map<Integer, AttendanceStatus> dailyStatus;

    @Schema(description = "Total present days in this month", example = "22")
    private int presentCount;

    @Schema(description = "Total half-day records in this month", example = "2")
    private int halfDayCount;

    @Schema(description = "Total absent records in this month", example = "2")
    private int absentCount;

    @Schema(description = "Total effective days worked (present + 0.5 * halfDay)", example = "23.0")
    private double totalEffectiveDays;

    @Schema(description = "Total estimated wages for this employee (effectiveDays * dailyRate)", example = "13800.00")
    private BigDecimal estimatedWages;

    public RegisterEmployeeRow() {
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

    public Map<Integer, AttendanceStatus> getDailyStatus() {
        return dailyStatus;
    }

    public void setDailyStatus(Map<Integer, AttendanceStatus> dailyStatus) {
        this.dailyStatus = dailyStatus;
    }

    public int getPresentCount() {
        return presentCount;
    }

    public void setPresentCount(int presentCount) {
        this.presentCount = presentCount;
    }

    public int getHalfDayCount() {
        return halfDayCount;
    }

    public void setHalfDayCount(int halfDayCount) {
        this.halfDayCount = halfDayCount;
    }

    public int getAbsentCount() {
        return absentCount;
    }

    public void setAbsentCount(int absentCount) {
        this.absentCount = absentCount;
    }

    public double getTotalEffectiveDays() {
        return totalEffectiveDays;
    }

    public void setTotalEffectiveDays(double totalEffectiveDays) {
        this.totalEffectiveDays = totalEffectiveDays;
    }

    public BigDecimal getEstimatedWages() {
        return estimatedWages;
    }

    public void setEstimatedWages(BigDecimal estimatedWages) {
        this.estimatedWages = estimatedWages;
    }
}
