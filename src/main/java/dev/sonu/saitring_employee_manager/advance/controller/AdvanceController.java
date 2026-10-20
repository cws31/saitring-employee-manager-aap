package dev.sonu.saitring_employee_manager.advance.controller;

import dev.sonu.saitring_employee_manager.advance.dto.*;
import dev.sonu.saitring_employee_manager.advance.service.AdvanceService;
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
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;

@RestController
@RequestMapping("/api/v1/advances")
@Tag(name = "Advance Money Management", description = "Endpoints for employee advance money, monthly register, history, and running balance summaries")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('OWNER')")
public class AdvanceController {

    private final AdvanceService advanceService;

    public AdvanceController(AdvanceService advanceService) {
        this.advanceService = advanceService;
    }

    @PostMapping
    @Operation(summary = "Disburse Advance Money",
            description = "Records a new advance given to an employee. Cannot disburse before employee's joining date or to blocked employees.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Advance recorded successfully",
                    content = @Content(schema = @Schema(implementation = AdvanceResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error or date before joining date",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Employee not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<AdvanceResponse> createAdvance(
            @AuthenticationPrincipal OwnerPrincipal principal,
            @Valid @RequestBody CreateAdvanceRequest request) {
        AdvanceResponse response = advanceService.createAdvance(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Advance Record by ID",
            description = "Retrieves an individual advance record by its ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Advance record found",
                    content = @Content(schema = @Schema(implementation = AdvanceResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Advance record not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<AdvanceResponse> getAdvanceById(
            @AuthenticationPrincipal OwnerPrincipal principal,
            @PathVariable Long id) {
        AdvanceResponse response = advanceService.getAdvanceById(principal.getId(), id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Advance Record",
            description = "Updates amount, date, or note of an existing advance record.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Advance updated successfully",
                    content = @Content(schema = @Schema(implementation = AdvanceResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Advance record not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<AdvanceResponse> updateAdvance(
            @AuthenticationPrincipal OwnerPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody UpdateAdvanceRequest request) {
        AdvanceResponse response = advanceService.updateAdvance(principal.getId(), id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Advance Record",
            description = "Deletes an advance record by ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Advance record deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Advance record not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteAdvance(
            @AuthenticationPrincipal OwnerPrincipal principal,
            @PathVariable Long id) {
        advanceService.deleteAdvance(principal.getId(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/register")
    @Operation(summary = "Get Monthly Advance Money Register",
            description = "Retrieves the paginated advance register showing active employees who joined on or before month end, their monthly advances, and running balances.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Advance register retrieved successfully",
                    content = @Content(schema = @Schema(implementation = AdvanceRegisterResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid month format",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<AdvanceRegisterResponse> getAdvanceRegister(
            @AuthenticationPrincipal OwnerPrincipal principal,
            @Parameter(description = "Target month in YYYY-MM format (defaults to current month if omitted)", example = "2026-10")
            @RequestParam(required = false) String month,
            @ParameterObject
            @PageableDefault(size = 20, sort = "joiningDate", direction = Sort.Direction.ASC) Pageable pageable) {
        YearMonth yearMonth = parseYearMonth(month);
        AdvanceRegisterResponse response = advanceService.getAdvanceRegister(principal.getId(), yearMonth, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "Get Employee Advance History",
            description = "Retrieves paginated advance history, period totals, and running advance balance for a specific employee.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employee advance history retrieved successfully",
                    content = @Content(schema = @Schema(implementation = EmployeeAdvanceHistoryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid month format",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Employee not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EmployeeAdvanceHistoryResponse> getEmployeeAdvanceHistory(
            @AuthenticationPrincipal OwnerPrincipal principal,
            @PathVariable Long employeeId,
            @Parameter(description = "Optional filter month in YYYY-MM format", example = "2026-10")
            @RequestParam(required = false) String month,
            @ParameterObject
            @PageableDefault(size = 20, sort = "advanceDate", direction = Sort.Direction.DESC) Pageable pageable) {
        YearMonth yearMonth = (month != null && !month.isBlank()) ? parseYearMonth(month) : null;
        EmployeeAdvanceHistoryResponse response = advanceService.getEmployeeAdvanceHistory(principal.getId(), employeeId, yearMonth, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/summary/monthly")
    @Operation(summary = "Get Monthly Advance Summary",
            description = "Retrieves monthly advance disbursements, number of transactions, employees assisted, and overdue prior advances.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Monthly advance summary retrieved successfully",
                    content = @Content(schema = @Schema(implementation = MonthlyAdvanceSummaryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid month format",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<MonthlyAdvanceSummaryResponse> getMonthlyAdvanceSummary(
            @AuthenticationPrincipal OwnerPrincipal principal,
            @Parameter(description = "Target month in YYYY-MM format (defaults to current month if omitted)", example = "2026-10")
            @RequestParam(required = false) String month) {
        YearMonth yearMonth = parseYearMonth(month);
        MonthlyAdvanceSummaryResponse response = advanceService.getMonthlyAdvanceSummary(principal.getId(), yearMonth);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/summary/running")
    @Operation(summary = "Get Overall Running Advance Summary",
            description = "Retrieves overall cumulative running advance for owner view with top advance holders and organization totals.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Running advance summary retrieved successfully",
                    content = @Content(schema = @Schema(implementation = OwnerAdvanceSummaryResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<OwnerAdvanceSummaryResponse> getOwnerAdvanceSummary(
            @AuthenticationPrincipal OwnerPrincipal principal) {
        OwnerAdvanceSummaryResponse response = advanceService.getOwnerAdvanceSummary(principal.getId());
        return ResponseEntity.ok(response);
    }

    private YearMonth parseYearMonth(String month) {
        if (month == null || month.isBlank()) {
            return YearMonth.now();
        }
        try {
            return YearMonth.parse(month.trim());
        } catch (DateTimeParseException e) {
            throw new BadRequestException("Invalid month format [" + month + "]. Expected format is YYYY-MM (e.g. 2026-10)");
        }
    }
}
