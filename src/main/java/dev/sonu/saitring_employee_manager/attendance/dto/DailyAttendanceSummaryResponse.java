package dev.sonu.saitring_employee_manager.attendance.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "Daily attendance summary for the authenticated owner")
public class DailyAttendanceSummaryResponse {

    @Schema(description = "Date of attendance summary", example = "2026-10-20")
    private LocalDate attendanceDate;

    @Schema(description = "Total eligible active employees on this date", example = "10")
    private int totalEligibleEmployees;

    @Schema(description = "Total employees whose attendance has been marked", example = "9")
    private int totalMarked;

    @Schema(description = "Total employees whose attendance is pending/unmarked", example = "1")
    private int totalUnmarked;

    @Schema(description = "Count of employees present", example = "7")
    private int presentCount;

    @Schema(description = "Count of employees with half day", example = "1")
    private int halfDayCount;

    @Schema(description = "Count of employees absent", example = "1")
    private int absentCount;

    @Schema(description = "Effective days worked today (present + 0.5 * halfDay)", example = "7.5")
    private double totalEffectiveDays;

    @Schema(description = "List of all eligible employees with their attendance status for today")
    private List<DailyEmployeeAttendanceRecord> records;

    public DailyAttendanceSummaryResponse() {
    }

    public LocalDate getAttendanceDate() {
        return attendanceDate;
    }

    public void setAttendanceDate(LocalDate attendanceDate) {
        this.attendanceDate = attendanceDate;
    }

    public int getTotalEligibleEmployees() {
        return totalEligibleEmployees;
    }

    public void setTotalEligibleEmployees(int totalEligibleEmployees) {
        this.totalEligibleEmployees = totalEligibleEmployees;
    }

    public int getTotalMarked() {
        return totalMarked;
    }

    public void setTotalMarked(int totalMarked) {
        this.totalMarked = totalMarked;
    }

    public int getTotalUnmarked() {
        return totalUnmarked;
    }

    public void setTotalUnmarked(int totalUnmarked) {
        this.totalUnmarked = totalUnmarked;
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

    public List<DailyEmployeeAttendanceRecord> getRecords() {
        return records;
    }

    public void setRecords(List<DailyEmployeeAttendanceRecord> records) {
        this.records = records;
    }
}
