package dev.sonu.saitring_employee_manager.advance.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Paginated monthly advance register showing active eligible employees and their advance details")
public class AdvanceRegisterResponse {

    @Schema(description = "Month in YYYY-MM format", example = "2026-10")
    private String month;

    @Schema(description = "Total number of eligible active employees in this month across all pages", example = "45")
    private int totalEligibleEmployees;

    @Schema(description = "Total advance disbursed across all eligible employees in this month", example = "15000.00")
    private BigDecimal totalMonthlyAdvanceDisbursed;

    @Schema(description = "Total cumulative unsettled running advance across the organization", example = "65000.00")
    private BigDecimal totalRunningAdvance;

    @Schema(description = "Current page number (0-based)", example = "0")
    private int pageNumber;

    @Schema(description = "Page size", example = "20")
    private int pageSize;

    @Schema(description = "Total number of employee records across all pages", example = "45")
    private long totalElements;

    @Schema(description = "Total number of pages", example = "3")
    private int totalPages;

    @Schema(description = "Whether this is the first page", example = "true")
    private boolean first;

    @Schema(description = "Whether this is the last page", example = "false")
    private boolean last;

    @Schema(description = "Whether a next page exists", example = "true")
    private boolean hasNext;

    @Schema(description = "Whether a previous page exists", example = "false")
    private boolean hasPrevious;

    @Schema(description = "List of employee rows with their advance details for the current page")
    private List<AdvanceRegisterEmployeeRow> employees;

    public AdvanceRegisterResponse() {
    }

    public AdvanceRegisterResponse(String month, int totalEligibleEmployees,
                                   BigDecimal totalMonthlyAdvanceDisbursed, BigDecimal totalRunningAdvance,
                                   int pageNumber, int pageSize, long totalElements, int totalPages,
                                   boolean first, boolean last, boolean hasNext, boolean hasPrevious,
                                   List<AdvanceRegisterEmployeeRow> employees) {
        this.month = month;
        this.totalEligibleEmployees = totalEligibleEmployees;
        this.totalMonthlyAdvanceDisbursed = totalMonthlyAdvanceDisbursed;
        this.totalRunningAdvance = totalRunningAdvance;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.first = first;
        this.last = last;
        this.hasNext = hasNext;
        this.hasPrevious = hasPrevious;
        this.employees = employees;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public int getTotalEligibleEmployees() {
        return totalEligibleEmployees;
    }

    public void setTotalEligibleEmployees(int totalEligibleEmployees) {
        this.totalEligibleEmployees = totalEligibleEmployees;
    }

    public BigDecimal getTotalMonthlyAdvanceDisbursed() {
        return totalMonthlyAdvanceDisbursed;
    }

    public void setTotalMonthlyAdvanceDisbursed(BigDecimal totalMonthlyAdvanceDisbursed) {
        this.totalMonthlyAdvanceDisbursed = totalMonthlyAdvanceDisbursed;
    }

    public BigDecimal getTotalRunningAdvance() {
        return totalRunningAdvance;
    }

    public void setTotalRunningAdvance(BigDecimal totalRunningAdvance) {
        this.totalRunningAdvance = totalRunningAdvance;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public boolean isFirst() {
        return first;
    }

    public void setFirst(boolean first) {
        this.first = first;
    }

    public boolean isLast() {
        return last;
    }

    public void setLast(boolean last) {
        this.last = last;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }

    public boolean isHasPrevious() {
        return hasPrevious;
    }

    public void setHasPrevious(boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }

    public List<AdvanceRegisterEmployeeRow> getEmployees() {
        return employees;
    }

    public void setEmployees(List<AdvanceRegisterEmployeeRow> employees) {
        this.employees = employees;
    }
}
