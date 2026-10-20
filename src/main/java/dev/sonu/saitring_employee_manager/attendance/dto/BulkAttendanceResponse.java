package dev.sonu.saitring_employee_manager.attendance.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "Response payload after bulk marking attendance")
public class BulkAttendanceResponse {

    @Schema(description = "Date of attendance", example = "2026-10-20")
    private LocalDate attendanceDate;

    @Schema(description = "Total records processed (created/updated)", example = "15")
    private int processedCount;

    @Schema(description = "List of processed attendance records")
    private List<AttendanceResponse> records;

    public BulkAttendanceResponse() {
    }

    public BulkAttendanceResponse(LocalDate attendanceDate, int processedCount, List<AttendanceResponse> records) {
        this.attendanceDate = attendanceDate;
        this.processedCount = processedCount;
        this.records = records;
    }

    public LocalDate getAttendanceDate() {
        return attendanceDate;
    }

    public void setAttendanceDate(LocalDate attendanceDate) {
        this.attendanceDate = attendanceDate;
    }

    public int getProcessedCount() {
        return processedCount;
    }

    public void setProcessedCount(int processedCount) {
        this.processedCount = processedCount;
    }

    public List<AttendanceResponse> getRecords() {
        return records;
    }

    public void setRecords(List<AttendanceResponse> records) {
        this.records = records;
    }
}
