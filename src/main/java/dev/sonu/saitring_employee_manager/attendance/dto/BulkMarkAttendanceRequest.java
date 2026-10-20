package dev.sonu.saitring_employee_manager.attendance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "Request body to mark bulk attendance for multiple employees on a specific date")
public class BulkMarkAttendanceRequest {

    @NotNull(message = "Attendance date is required")
    @Schema(description = "Date of attendance (YYYY-MM-DD)", example = "2026-10-20")
    private LocalDate attendanceDate;

    @NotEmpty(message = "Attendance list cannot be empty")
    @Valid
    @Schema(description = "List of employee attendances to mark or update")
    private List<BulkAttendanceItem> attendances;

    public BulkMarkAttendanceRequest() {
    }

    public BulkMarkAttendanceRequest(LocalDate attendanceDate, List<BulkAttendanceItem> attendances) {
        this.attendanceDate = attendanceDate;
        this.attendances = attendances;
    }

    public LocalDate getAttendanceDate() {
        return attendanceDate;
    }

    public void setAttendanceDate(LocalDate attendanceDate) {
        this.attendanceDate = attendanceDate;
    }

    public List<BulkAttendanceItem> getAttendances() {
        return attendances;
    }

    public void setAttendances(List<BulkAttendanceItem> attendances) {
        this.attendances = attendances;
    }
}
