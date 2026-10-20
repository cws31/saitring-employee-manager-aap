package dev.sonu.saitring_employee_manager.advance.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Overall advance money and running balance summary for owner view")
public class OwnerAdvanceSummaryResponse {

    @Schema(description = "Total cumulative outstanding running advance across all employees", example = "65000.00")
    private BigDecimal totalOutstandingRunningAdvance;

    @Schema(description = "Total active employees in organization", example = "25")
    private long totalActiveEmployees;

    @Schema(description = "Number of active employees who currently have an outstanding running advance", example = "12")
    private long employeesWithAdvanceCount;

    @Schema(description = "List of top employees with highest running advance balance")
    private List<EmployeeRunningAdvanceItem> topAdvanceHolders;

    public OwnerAdvanceSummaryResponse() {
    }

    public OwnerAdvanceSummaryResponse(BigDecimal totalOutstandingRunningAdvance, long totalActiveEmployees,
                                       long employeesWithAdvanceCount, List<EmployeeRunningAdvanceItem> topAdvanceHolders) {
        this.totalOutstandingRunningAdvance = totalOutstandingRunningAdvance;
        this.totalActiveEmployees = totalActiveEmployees;
        this.employeesWithAdvanceCount = employeesWithAdvanceCount;
        this.topAdvanceHolders = topAdvanceHolders;
    }

    public BigDecimal getTotalOutstandingRunningAdvance() {
        return totalOutstandingRunningAdvance;
    }

    public void setTotalOutstandingRunningAdvance(BigDecimal totalOutstandingRunningAdvance) {
        this.totalOutstandingRunningAdvance = totalOutstandingRunningAdvance;
    }

    public long getTotalActiveEmployees() {
        return totalActiveEmployees;
    }

    public void setTotalActiveEmployees(long totalActiveEmployees) {
        this.totalActiveEmployees = totalActiveEmployees;
    }

    public long getEmployeesWithAdvanceCount() {
        return employeesWithAdvanceCount;
    }

    public void setEmployeesWithAdvanceCount(long employeesWithAdvanceCount) {
        this.employeesWithAdvanceCount = employeesWithAdvanceCount;
    }

    public List<EmployeeRunningAdvanceItem> getTopAdvanceHolders() {
        return topAdvanceHolders;
    }

    public void setTopAdvanceHolders(List<EmployeeRunningAdvanceItem> topAdvanceHolders) {
        this.topAdvanceHolders = topAdvanceHolders;
    }
}
