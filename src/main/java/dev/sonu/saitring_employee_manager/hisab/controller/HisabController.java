package dev.sonu.saitring_employee_manager.hisab.controller;

import dev.sonu.saitring_employee_manager.common.exception.BadRequestException;
import dev.sonu.saitring_employee_manager.common.exception.ErrorResponse;
import dev.sonu.saitring_employee_manager.hisab.dto.*;
import dev.sonu.saitring_employee_manager.hisab.service.HisabService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;

@RestController
@RequestMapping("/api/v1/hisab")
@Tag(name = "Hisab Management", description = "Endpoints for monthly running hisab table, settlement, and employee hisab history")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('OWNER')")
public class HisabController {

    private final HisabService hisabService;

    public HisabController(HisabService hisabService) {
        this.hisabService = hisabService;
    }

    @GetMapping
    @Operation(summary = "Get Monthly Running Hisab Table",
            description = "Retrieves a paginated tabular running hisab register for the given month. Displays employee name, full days, half days, total presenties, daily rate, total earnings, total advance, and final payable amount (total earnings - total advance). Excludes blocked employees and employees who joined after the target month.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Monthly hisab table retrieved successfully",
                    content = @Content(schema = @Schema(implementation = MonthlyHisabTableResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid month format",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<MonthlyHisabTableResponse> getMonthlyHisabTable(
            @AuthenticationPrincipal OwnerPrincipal principal,
            @Parameter(description = "Target month in YYYY-MM format (defaults to current month if omitted)", example = "2026-10")
            @RequestParam(required = false) String month,
            @ParameterObject
            @PageableDefault(size = 20, sort = "joiningDate", direction = Sort.Direction.ASC) Pageable pageable) {
        YearMonth yearMonth = parseYearMonth(month);
        MonthlyHisabTableResponse response = hisabService.getMonthlyHisabTable(principal.getId(), yearMonth, pageable);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/settle")
    @Operation(summary = "Settle Monthly Hisab for Employee",
            description = "Settles and closes the hisab for an employee for a specific month. Marks all unsettled monthly advances up to that month as SETTLED.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Hisab settled successfully",
                    content = @Content(schema = @Schema(implementation = HisabResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error or hisab already settled",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Employee not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<HisabResponse> settleHisab(
            @AuthenticationPrincipal OwnerPrincipal principal,
            @Valid @RequestBody SettleHisabRequest request) {
        HisabResponse response = hisabService.settleHisab(principal.getId(), request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "Get Employee Settlement History",
            description = "Retrieves paginated settled hisab records and total settled payout history for an employee.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employee hisab history retrieved successfully",
                    content = @Content(schema = @Schema(implementation = EmployeeHisabHistoryResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Employee not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EmployeeHisabHistoryResponse> getEmployeeHisabHistory(
            @AuthenticationPrincipal OwnerPrincipal principal,
            @PathVariable Long employeeId,
            @ParameterObject
            @PageableDefault(size = 12, sort = "yearMonth", direction = Sort.Direction.DESC) Pageable pageable) {
        EmployeeHisabHistoryResponse response = hisabService.getEmployeeHisabHistory(principal.getId(), employeeId, pageable);
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
