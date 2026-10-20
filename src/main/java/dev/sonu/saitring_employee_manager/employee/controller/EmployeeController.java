package dev.sonu.saitring_employee_manager.employee.controller;

import dev.sonu.saitring_employee_manager.common.exception.ErrorResponse;
import dev.sonu.saitring_employee_manager.employee.dto.*;
import dev.sonu.saitring_employee_manager.employee.entity.EmployeeStatus;
import dev.sonu.saitring_employee_manager.employee.service.EmployeeService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/employees")
@Tag(name = "Employee Management", description = "Multi-tenant employee operations: CRUD, listing, filtering, and block/unblock actions strictly scoped to the authenticated owner tenant.")
@SecurityRequirement(name = "bearerAuth")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping
    @Operation(summary = "Create an Employee",
            description = "Creates a new employee record for the authenticated owner tenant. Mobile number must be unique within the owner's organization.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Employee created successfully",
                    content = @Content(schema = @Schema(implementation = EmployeeResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Mobile number already exists in this organization",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EmployeeResponse> createEmployee(
            @AuthenticationPrincipal OwnerPrincipal principal,
            @Valid @RequestBody CreateEmployeeRequest request) {
        EmployeeResponse response = employeeService.createEmployee(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Employee by ID",
            description = "Retrieves an employee belonging to the authenticated owner tenant. If the employee does not exist or belongs to another owner, 404 is returned.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employee found",
                    content = @Content(schema = @Schema(implementation = EmployeeResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Employee not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EmployeeResponse> getEmployeeById(
            @AuthenticationPrincipal OwnerPrincipal principal,
            @PathVariable Long id) {
        EmployeeResponse response = employeeService.getEmployeeById(principal.getId(), id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "List & Search Employees",
            description = "Returns a paginated list of employees for the authenticated owner tenant with optional status filtering and name/mobile search query.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of employees returned"),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Page<EmployeeResponse>> listEmployees(
            @AuthenticationPrincipal OwnerPrincipal principal,
            @Parameter(description = "Filter by employee status: ACTIVE or BLOCKED")
            @RequestParam(required = false) EmployeeStatus status,
            @Parameter(description = "Search query by name or phone number")
            @RequestParam(required = false) String query,
            @org.springdoc.core.annotations.ParameterObject
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<EmployeeResponse> page = employeeService.listEmployees(principal.getId(), status, query, pageable);
        return ResponseEntity.ok(page);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Employee",
            description = "Updates details of an existing employee belonging to the authenticated owner tenant.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employee updated successfully",
                    content = @Content(schema = @Schema(implementation = EmployeeResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Employee not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Mobile number conflict within organization",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EmployeeResponse> updateEmployee(
            @AuthenticationPrincipal OwnerPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody UpdateEmployeeRequest request) {
        EmployeeResponse response = employeeService.updateEmployee(principal.getId(), id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Employee",
            description = "Deletes an employee belonging to the authenticated owner tenant.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Employee deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Employee not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteEmployee(
            @AuthenticationPrincipal OwnerPrincipal principal,
            @PathVariable Long id) {
        employeeService.deleteEmployee(principal.getId(), id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/block")
    @Operation(summary = "Block Employee",
            description = "Changes employee status to BLOCKED. Blocked employees cannot be selected for daily attendance or new advances.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employee blocked successfully",
                    content = @Content(schema = @Schema(implementation = EmployeeResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Employee not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EmployeeResponse> blockEmployee(
            @AuthenticationPrincipal OwnerPrincipal principal,
            @PathVariable Long id) {
        EmployeeResponse response = employeeService.blockEmployee(principal.getId(), id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/unblock")
    @Operation(summary = "Unblock Employee",
            description = "Restores employee status from BLOCKED back to ACTIVE.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employee unblocked successfully",
                    content = @Content(schema = @Schema(implementation = EmployeeResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Employee not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EmployeeResponse> unblockEmployee(
            @AuthenticationPrincipal OwnerPrincipal principal,
            @PathVariable Long id) {
        EmployeeResponse response = employeeService.unblockEmployee(principal.getId(), id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/rates")
    @Operation(summary = "Record Employee Wage Rate Revision",
            description = "Logs a new daily wage rate revision with effective date for historical rate tracking.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Wage rate revision recorded successfully",
                    content = @Content(schema = @Schema(implementation = EmployeeRateResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error or date before joining date",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Employee not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EmployeeRateResponse> addRateRevision(
            @AuthenticationPrincipal OwnerPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody RateRevisionRequest request) {
        EmployeeRateResponse response = employeeService.addRateRevision(principal.getId(), id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}/rates")
    @Operation(summary = "Get Employee Wage Rate History",
            description = "Retrieves paginated history of wage rate revisions for the employee.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rate history retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Employee not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Page<EmployeeRateResponse>> getEmployeeRateHistory(
            @AuthenticationPrincipal OwnerPrincipal principal,
            @PathVariable Long id,
            @org.springdoc.core.annotations.ParameterObject
            @PageableDefault(size = 20, sort = "effectiveFrom", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<EmployeeRateResponse> response = employeeService.getEmployeeRateHistory(principal.getId(), id, pageable);
        return ResponseEntity.ok(response);
    }
}
