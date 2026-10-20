package dev.sonu.saitring_employee_manager.attendance.dto;

import dev.sonu.saitring_employee_manager.attendance.entity.AttendanceStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(description = "Request body to mark or update attendance for an employee on a specific date")
public class MarkAttendanceRequest {

    @NotNull(message = "Employee ID is required")
    @Schema(description = "ID of the employee", example = "1")
    private Long employeeId;

    @NotNull(message = "Attendance date is required")
    @Schema(description = "Date of attendance (YYYY-MM-DD)", example = "2026-10-20")
    private LocalDate attendanceDate;

    @NotNull(message = "Attendance status is required (PRESENT, HALF_DAY, ABSENT)")
    @Schema(description = "Attendance status", example = "PRESENT")
    private AttendanceStatus status;

    @Size(max = 255, message = "Note cannot exceed 255 characters")
    @Schema(description = "Optional note or remark", example = "Worked full shift at site A")
    private String note;

    public MarkAttendanceRequest() {
    }

    public MarkAttendanceRequest(Long employeeId, LocalDate attendanceDate, AttendanceStatus status, String note) {
        this.employeeId = employeeId;
        this.attendanceDate = attendanceDate;
        this.status = status;
        this.note = note;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
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
}
