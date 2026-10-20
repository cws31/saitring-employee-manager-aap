package dev.sonu.saitring_employee_manager.attendance.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Full monthly attendance register showing eligible active employees and day-by-day status with pagination")
public class MonthlyRegisterResponse {

    @Schema(description = "Month in YYYY-MM format", example = "2026-10")
    private String month;

    @Schema(description = "Total number of days in this month", example = "31")
    private int daysInMonth;

    @Schema(description = "Total number of eligible active employees in this month across all pages", example = "45")
    private int totalEligibleEmployees;

    @Schema(description = "Current page number (0-based)", example = "0")
    private int pageNumber;

    @Schema(description = "Page size (records per page)", example = "20")
    private int pageSize;

    @Schema(description = "Total number of eligible employee records across all pages", example = "45")
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

    @Schema(description = "List of employee rows with their day-by-day attendance status for the current page")
    private List<RegisterEmployeeRow> employees;

    public MonthlyRegisterResponse() {
    }

    public MonthlyRegisterResponse(String month, int daysInMonth, int totalEligibleEmployees, List<RegisterEmployeeRow> employees) {
        this.month = month;
        this.daysInMonth = daysInMonth;
        this.totalEligibleEmployees = totalEligibleEmployees;
        this.employees = employees;
        this.pageNumber = 0;
        this.pageSize = employees != null ? employees.size() : 0;
        this.totalElements = totalEligibleEmployees;
        this.totalPages = totalEligibleEmployees > 0 ? 1 : 0;
        this.first = true;
        this.last = true;
        this.hasNext = false;
        this.hasPrevious = false;
    }

    public MonthlyRegisterResponse(String month, int daysInMonth, int totalEligibleEmployees,
                                   int pageNumber, int pageSize, long totalElements, int totalPages,
                                   boolean first, boolean last, boolean hasNext, boolean hasPrevious,
                                   List<RegisterEmployeeRow> employees) {
        this.month = month;
        this.daysInMonth = daysInMonth;
        this.totalEligibleEmployees = totalEligibleEmployees;
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

    public int getDaysInMonth() {
        return daysInMonth;
    }

    public void setDaysInMonth(int daysInMonth) {
        this.daysInMonth = daysInMonth;
    }

    public int getTotalEligibleEmployees() {
        return totalEligibleEmployees;
    }

    public void setTotalEligibleEmployees(int totalEligibleEmployees) {
        this.totalEligibleEmployees = totalEligibleEmployees;
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

    public List<RegisterEmployeeRow> getEmployees() {
        return employees;
    }

    public void setEmployees(List<RegisterEmployeeRow> employees) {
        this.employees = employees;
    }
}
