package dev.sonu.saitring_employee_manager.attendance.dto;

import dev.sonu.saitring_employee_manager.attendance.entity.AttendanceStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Attendance record of an employee for a specific day")
public class DailyEmployeeAttendanceRecord {

    @Schema(description = "Employee ID", example = "1")
    private Long employeeId;

    @Schema(description = "Employee Name", example = "Ramesh Kumar")
    private String employeeName;

    @Schema(description = "Employee Mobile Number", example = "+919876543210")
    private String mobileNumber;

    @Schema(description = "Daily wage rate", example = "600.00")
    private BigDecimal dailyRate;

    @Schema(description = "Joining Date", example = "2026-09-01")
    private LocalDate joiningDate;

    @Schema(description = "Attendance Record ID (null if unmarked)", example = "12")
    private Long attendanceId;

    @Schema(description = "Attendance status (null if unmarked)", example = "PRESENT")
    private AttendanceStatus status;

    @Schema(description = "Optional note or remark", example = "Site A shuttering")
    private String note;

    @Schema(description = "Whether attendance is marked for this date", example = "true")
    private boolean marked;

    public DailyEmployeeAttendanceRecord() {
    }

    public DailyEmployeeAttendanceRecord(Long employeeId, String employeeName, String mobileNumber,
                                         BigDecimal dailyRate, LocalDate joiningDate, Long attendanceId,
                                         AttendanceStatus status, String note, boolean marked) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.mobileNumber = mobileNumber;
        this.dailyRate = dailyRate;
        this.joiningDate = joiningDate;
        this.attendanceId = attendanceId;
        this.status = status;
        this.note = note;
        this.marked = marked;
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

    public Long getAttendanceId() {
        return attendanceId;
    }

    public void setAttendanceId(Long attendanceId) {
        this.attendanceId = attendanceId;
    }

    public AttendanceStatus getStatus() {
        return status;
    }

    public void setStatus(AttendanceStatus status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public boolean isMarked() {
        return marked;
    }

    public void setMarked(boolean marked) {
        this.marked = marked;
    }
}
