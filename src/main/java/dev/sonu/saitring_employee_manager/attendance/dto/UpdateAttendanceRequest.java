package dev.sonu.saitring_employee_manager.attendance.dto;

import dev.sonu.saitring_employee_manager.attendance.entity.AttendanceStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Request body to update an existing attendance record")
public class UpdateAttendanceRequest {

    @NotNull(message = "Attendance status is required (PRESENT, HALF_DAY, ABSENT)")
    @Schema(description = "Updated attendance status", example = "HALF_DAY")
    private AttendanceStatus status;

    @Size(max = 255, message = "Note cannot exceed 255 characters")
    @Schema(description = "Optional note or remark", example = "Left at 1 PM due to illness")
    private String note;

    public UpdateAttendanceRequest() {
    }

    public UpdateAttendanceRequest(AttendanceStatus status, String note) {
        this.status = status;
        this.note = note;
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
