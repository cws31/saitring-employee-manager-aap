package dev.sonu.saitring_employee_manager.attendance.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Overall monthly attendance summary for the authenticated owner")
public class MonthlyAttendanceSummaryResponse {

    @Schema(description = "Target month in YYYY-MM format", example = "2026-10")
    private String month;

    @Schema(description = "Total number of days in the month", example = "31")
    private int totalDaysInMonth;

    @Schema(description = "Total eligible active employees in this month", example = "12")
    private int totalEligibleEmployees;

    @Schema(description = "Total present records across all employees", example = "280")
    private int totalPresentRecords;

    @Schema(description = "Total half-day records across all employees", example = "14")
    private int totalHalfDayRecords;

    @Schema(description = "Total absent records across all employees", example = "25")
    private int totalAbsentRecords;

    @Schema(description = "Total effective days worked across all employees (present + 0.5 * halfDay)", example = "287.0")
    private double totalEffectiveDays;

    @Schema(description = "Total estimated wages across all employees for this month", example = "172200.00")
    private BigDecimal totalEstimatedWages;

    public MonthlyAttendanceSummaryResponse() {
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public int getTotalDaysInMonth() {
        return totalDaysInMonth;
    }

    public void setTotalDaysInMonth(int totalDaysInMonth) {
        this.totalDaysInMonth = totalDaysInMonth;
    }

    public int getTotalEligibleEmployees() {
        return totalEligibleEmployees;
    }

    public void setTotalEligibleEmployees(int totalEligibleEmployees) {
        this.totalEligibleEmployees = totalEligibleEmployees;
    }

    public int getTotalPresentRecords() {
        return totalPresentRecords;
    }

    public void setTotalPresentRecords(int totalPresentRecords) {
        this.totalPresentRecords = totalPresentRecords;
    }

    public int getTotalHalfDayRecords() {
        return totalHalfDayRecords;
    }

    public void setTotalHalfDayRecords(int totalHalfDayRecords) {
        this.totalHalfDayRecords = totalHalfDayRecords;
    }

    public int getTotalAbsentRecords() {
        return totalAbsentRecords;
    }

    public void setTotalAbsentRecords(int totalAbsentRecords) {
        this.totalAbsentRecords = totalAbsentRecords;
    }

    public double getTotalEffectiveDays() {
        return totalEffectiveDays;
    }

    public void setTotalEffectiveDays(double totalEffectiveDays) {
        this.totalEffectiveDays = totalEffectiveDays;
    }

    public BigDecimal getTotalEstimatedWages() {
        return totalEstimatedWages;
    }

    public void setTotalEstimatedWages(BigDecimal totalEstimatedWages) {
        this.totalEstimatedWages = totalEstimatedWages;
    }
}
