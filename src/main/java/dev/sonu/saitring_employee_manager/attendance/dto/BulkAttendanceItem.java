package dev.sonu.saitring_employee_manager.attendance.dto;

import dev.sonu.saitring_employee_manager.attendance.entity.AttendanceStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Single employee attendance item for bulk marking")
public class BulkAttendanceItem {

    @NotNull(message = "Employee ID is required")
    @Schema(description = "ID of the employee", example = "1")
    private Long employeeId;

    @NotNull(message = "Attendance status is required (PRESENT, HALF_DAY, ABSENT)")
    @Schema(description = "Attendance status", example = "PRESENT")
    private AttendanceStatus status;

    @Size(max = 255, message = "Note cannot exceed 255 characters")
    @Schema(description = "Optional note or remark", example = "Site B shuttering")
    private String note;

    public BulkAttendanceItem() {
    }

    public BulkAttendanceItem(Long employeeId, AttendanceStatus status, String note) {
        this.employeeId = employeeId;
        this.status = status;
        this.note = note;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
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
