package dev.sonu.saitring_employee_manager.advance.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Summary of advance money for a specific month")
public class MonthlyAdvanceSummaryResponse {

    @Schema(description = "Target month in YYYY-MM format", example = "2026-10")
    private String month;

    @Schema(description = "Total advance money disbursed in this month", example = "25000.00")
    private BigDecimal totalAdvanceDisbursed;

    @Schema(description = "Number of distinct employees who took an advance in this month", example = "8")
    private long totalEmployeesTookAdvance;

    @Schema(description = "Total number of advance transactions in this month", example = "14")
    private long totalTransactions;

    @Schema(description = "Unsettled overdue advance money from prior months before this month", example = "12000.00")
    private BigDecimal overduePriorAdvance;

    @Schema(description = "Total cumulative unsettled running advance across the organization", example = "37000.00")
    private BigDecimal totalOutstandingRunningAdvance;

    public MonthlyAdvanceSummaryResponse() {
    }

    public MonthlyAdvanceSummaryResponse(String month, BigDecimal totalAdvanceDisbursed,
                                         long totalEmployeesTookAdvance, long totalTransactions,
                                         BigDecimal overduePriorAdvance, BigDecimal totalOutstandingRunningAdvance) {
        this.month = month;
        this.totalAdvanceDisbursed = totalAdvanceDisbursed;
        this.totalEmployeesTookAdvance = totalEmployeesTookAdvance;
        this.totalTransactions = totalTransactions;
        this.overduePriorAdvance = overduePriorAdvance;
        this.totalOutstandingRunningAdvance = totalOutstandingRunningAdvance;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public BigDecimal getTotalAdvanceDisbursed() {
        return totalAdvanceDisbursed;
    }

    public void setTotalAdvanceDisbursed(BigDecimal totalAdvanceDisbursed) {
        this.totalAdvanceDisbursed = totalAdvanceDisbursed;
    }

    public long getTotalEmployeesTookAdvance() {
        return totalEmployeesTookAdvance;
    }

    public void setTotalEmployeesTookAdvance(long totalEmployeesTookAdvance) {
        this.totalEmployeesTookAdvance = totalEmployeesTookAdvance;
    }

    public long getTotalTransactions() {
        return totalTransactions;
    }

    public void setTotalTransactions(long totalTransactions) {
        this.totalTransactions = totalTransactions;
    }

    public BigDecimal getOverduePriorAdvance() {
        return overduePriorAdvance;
    }

    public void setOverduePriorAdvance(BigDecimal overduePriorAdvance) {
        this.overduePriorAdvance = overduePriorAdvance;
    }

    public BigDecimal getTotalOutstandingRunningAdvance() {
        return totalOutstandingRunningAdvance;
    }

    public void setTotalOutstandingRunningAdvance(BigDecimal totalOutstandingRunningAdvance) {
        this.totalOutstandingRunningAdvance = totalOutstandingRunningAdvance;
    }
}
