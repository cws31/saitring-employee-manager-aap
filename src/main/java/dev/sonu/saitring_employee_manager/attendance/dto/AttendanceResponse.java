package dev.sonu.saitring_employee_manager.attendance.dto;

import dev.sonu.saitring_employee_manager.attendance.entity.Attendance;
import dev.sonu.saitring_employee_manager.attendance.entity.AttendanceStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Schema(description = "Response payload representing an attendance record")
public class AttendanceResponse {

    @Schema(description = "Attendance record ID", example = "1")
    private Long id;

    @Schema(description = "Employee ID", example = "10")
    private Long employeeId;

    @Schema(description = "Employee name", example = "Ramesh Kumar")
    private String employeeName;

    @Schema(description = "Employee mobile number", example = "+919876543210")
    private String mobileNumber;

    @Schema(description = "Employee daily rate", example = "600.00")
    private BigDecimal dailyRate;

    @Schema(description = "Employee joining date", example = "2026-09-01")
    private LocalDate joiningDate;

    @Schema(description = "Attendance date", example = "2026-10-20")
    private LocalDate attendanceDate;

    @Schema(description = "Attendance status", example = "PRESENT")
    private AttendanceStatus status;

    @Schema(description = "Optional note or remark", example = "Site A shuttering")
    private String note;

    @Schema(description = "Record creation timestamp")
    private Instant createdAt;

    @Schema(description = "Record update timestamp")
    private Instant updatedAt;

    public AttendanceResponse() {
    }

    public static AttendanceResponse fromEntity(Attendance attendance) {
        AttendanceResponse dto = new AttendanceResponse();
        dto.setId(attendance.getId());
        if (attendance.getEmployee() != null) {
            dto.setEmployeeId(attendance.getEmployee().getId());
            dto.setEmployeeName(attendance.getEmployee().getEmployeeName());
            dto.setMobileNumber(attendance.getEmployee().getMobileNumber());
            dto.setDailyRate(attendance.getEmployee().getDailyRate());
            dto.setJoiningDate(attendance.getEmployee().getJoiningDate());
        }
        dto.setAttendanceDate(attendance.getAttendanceDate());
        dto.setStatus(attendance.getStatus());
        dto.setNote(attendance.getNote());
        dto.setCreatedAt(attendance.getCreatedAt());
        dto.setUpdatedAt(attendance.getUpdatedAt());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public LocalDate getAttendanceDate() {
        return attendanceDate;
    }

    public void setAttendanceDate(LocalDate attendanceDate) {
        this.attendanceDate = attendanceDate;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
