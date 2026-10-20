package dev.sonu.saitring_employee_manager.attendance.controller;

import dev.sonu.saitring_employee_manager.attendance.dto.*;
import dev.sonu.saitring_employee_manager.attendance.service.AttendanceService;
import dev.sonu.saitring_employee_manager.common.exception.BadRequestException;
import dev.sonu.saitring_employee_manager.common.exception.ErrorResponse;
import dev.sonu.saitring_employee_manager.security.OwnerPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;

@RestController
@RequestMapping("/api/v1/attendance")
@Tag(name = "Attendance Management", description = "Endpoints for daily attendance, bulk attendance, monthly register, and summaries")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('OWNER')")
public class AttendanceController {

        private final AttendanceService attendanceService;

        public AttendanceController(AttendanceService attendanceService) {
                this.attendanceService = attendanceService;
        }

        @PostMapping
        @Operation(summary = "Mark Attendance", description = "Marks or updates attendance for an individual employee on a specific date. Cannot mark before employee's joining date.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Attendance marked or updated successfully", content = @Content(schema = @Schema(implementation = AttendanceResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request or date before joining date", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "404", description = "Employee not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        public ResponseEntity<AttendanceResponse> markAttendance(
                        @AuthenticationPrincipal OwnerPrincipal principal,
                        @Valid @RequestBody MarkAttendanceRequest request) {
                AttendanceResponse response = attendanceService.markAttendance(principal.getId(), request);
                return ResponseEntity.ok(response);
        }

        @PostMapping("/bulk")
        @Operation(summary = "Bulk Mark Attendance", description = "Marks or updates attendance for multiple employees on a specific date in a single transaction.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Bulk attendance processed successfully", content = @Content(schema = @Schema(implementation = BulkAttendanceResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Validation error or invalid employee status", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "404", description = "One or more employees not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        public ResponseEntity<BulkAttendanceResponse> bulkMarkAttendance(
                        @AuthenticationPrincipal OwnerPrincipal principal,
                        @Valid @RequestBody BulkMarkAttendanceRequest request) {
                BulkAttendanceResponse response = attendanceService.bulkMarkAttendance(principal.getId(), request);
                return ResponseEntity.ok(response);
        }

        @GetMapping("/{id}")
        @Operation(summary = "Get Attendance By ID", description = "Retrieves an individual attendance record by its ID.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Attendance record found", content = @Content(schema = @Schema(implementation = AttendanceResponse.class))),
                        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "404", description = "Attendance record not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        public ResponseEntity<AttendanceResponse> getAttendanceById(
                        @AuthenticationPrincipal OwnerPrincipal principal,
                        @PathVariable Long id) {
                AttendanceResponse response = attendanceService.getAttendanceById(principal.getId(), id);
                return ResponseEntity.ok(response);
        }

        @PutMapping("/{id}")
        @Operation(summary = "Update Attendance", description = "Updates status or note of an existing attendance record.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Attendance updated successfully", content = @Content(schema = @Schema(implementation = AttendanceResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Validation error", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "404", description = "Attendance record not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        public ResponseEntity<AttendanceResponse> updateAttendance(
                        @AuthenticationPrincipal OwnerPrincipal principal,
                        @PathVariable Long id,
                        @Valid @RequestBody UpdateAttendanceRequest request) {
                AttendanceResponse response = attendanceService.updateAttendance(principal.getId(), id, request);
                return ResponseEntity.ok(response);
        }

        @DeleteMapping("/{id}")
        @Operation(summary = "Delete Attendance", description = "Deletes an attendance record by ID.")
        @ApiResponses({
                        @ApiResponse(responseCode = "204", description = "Attendance record deleted successfully"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "404", description = "Attendance record not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        public ResponseEntity<Void> deleteAttendance(
                        @AuthenticationPrincipal OwnerPrincipal principal,
                        @PathVariable Long id) {
                attendanceService.deleteAttendance(principal.getId(), id);
                return ResponseEntity.noContent().build();
        }

        @GetMapping("/register")
        @Operation(summary = "Get Monthly Attendance Register", description = "Retrieves the paginated monthly attendance register grid. Displays only ACTIVE employees who joined on or before the month end.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Monthly attendance register retrieved successfully", content = @Content(schema = @Schema(implementation = MonthlyRegisterResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid month format", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        public ResponseEntity<MonthlyRegisterResponse> getMonthlyRegister(
                        @AuthenticationPrincipal OwnerPrincipal principal,
                        @Parameter(description = "Target month in YYYY-MM format (defaults to current month if omitted)", example = "2026-10") @RequestParam(required = false) String month,
                        @org.springdoc.core.annotations.ParameterObject
                        @PageableDefault(size = 20, sort = "joiningDate", direction = Sort.Direction.ASC) Pageable pageable) {
                YearMonth yearMonth = parseYearMonth(month);
                MonthlyRegisterResponse response = attendanceService.getMonthlyRegister(principal.getId(), yearMonth, pageable);
                return ResponseEntity.ok(response);
        }

        @GetMapping("/summary/daily")
        @Operation(summary = "Get Daily Attendance Summary", description = "Retrieves daily statistics (eligible employees, marked/unmarked counts, present/half/absent) and employee status list.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Daily attendance summary retrieved successfully", content = @Content(schema = @Schema(implementation = DailyAttendanceSummaryResponse.class))),
                        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        public ResponseEntity<DailyAttendanceSummaryResponse> getDailySummary(
                        @AuthenticationPrincipal OwnerPrincipal principal,
                        @Parameter(description = "Target date (YYYY-MM-DD), defaults to current date if omitted", example = "2026-10-20") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
                LocalDate targetDate = (date != null) ? date : LocalDate.now();
                DailyAttendanceSummaryResponse response = attendanceService.getDailySummary(principal.getId(),
                                targetDate);
                return ResponseEntity.ok(response);
        }

        @GetMapping("/summary/monthly")
        @Operation(summary = "Get Monthly Attendance Summary", description = "Retrieves overall monthly attendance totals and aggregate estimated wage calculations.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Monthly attendance summary retrieved successfully", content = @Content(schema = @Schema(implementation = MonthlyAttendanceSummaryResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid month format", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        public ResponseEntity<MonthlyAttendanceSummaryResponse> getMonthlySummary(
                        @AuthenticationPrincipal OwnerPrincipal principal,
                        @Parameter(description = "Target month in YYYY-MM format (defaults to current month if omitted)", example = "2026-10") @RequestParam(required = false) String month) {
                YearMonth yearMonth = parseYearMonth(month);
                MonthlyAttendanceSummaryResponse response = attendanceService.getMonthlySummary(principal.getId(),
                                yearMonth);
                return ResponseEntity.ok(response);
        }

        @GetMapping("/employee/{employeeId}")
        @Operation(summary = "Get Employee Monthly Attendance History", description = "Retrieves detailed attendance history, effective days, and salary calculations for an employee in a specific month.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Employee monthly attendance history retrieved successfully", content = @Content(schema = @Schema(implementation = EmployeeMonthlySummaryResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid month format", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "404", description = "Employee not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        public ResponseEntity<EmployeeMonthlySummaryResponse> getEmployeeMonthlySummary(
                        @AuthenticationPrincipal OwnerPrincipal principal,
                        @PathVariable Long employeeId,
                        @Parameter(description = "Target month in YYYY-MM format (defaults to current month if omitted)", example = "2026-10") @RequestParam(required = false) String month) {
                YearMonth yearMonth = parseYearMonth(month);
                EmployeeMonthlySummaryResponse response = attendanceService.getEmployeeMonthlySummary(principal.getId(),
                                employeeId, yearMonth);
                return ResponseEntity.ok(response);
        }

        private YearMonth parseYearMonth(String month) {
                if (month == null || month.isBlank()) {
                        return YearMonth.now();
                }
                try {
                        return YearMonth.parse(month.trim());
                } catch (DateTimeParseException e) {
                        throw new BadRequestException("Invalid month format [" + month
                                        + "]. Expected format is YYYY-MM (e.g. 2026-10)");
                }
        }
}
