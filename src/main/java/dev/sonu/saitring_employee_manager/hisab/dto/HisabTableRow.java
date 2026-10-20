package dev.sonu.saitring_employee_manager.hisab.dto;

import dev.sonu.saitring_employee_manager.hisab.entity.HisabStatus;
import dev.sonu.saitring_employee_manager.hisab.entity.PaymentMode;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Schema(description = "Single employee row in the running/settled monthly hisab table")
public class HisabTableRow {

    @Schema(description = "Employee ID", example = "1")
    private Long employeeId;

    @Schema(description = "Employee Name", example = "Ramesh Kumar")
    private String employeeName;

    @Schema(description = "Employee Mobile Number", example = "+919876543210")
    private String mobileNumber;

    @Schema(description = "Employee Joining Date", example = "2026-09-01")
    private LocalDate joiningDate;

    @Schema(description = "Count of full days worked (PRESENT)", example = "22")
    private int fullDays;

    @Schema(description = "Count of half days worked (HALF_DAY)", example = "2")
    private int halfDays;

    @Schema(description = "Total effective presenties worked (fullDays + 0.5 * halfDays)", example = "23.0")
    private double totalPresenties;

    @Schema(description = "Daily wage rate (rate/day)", example = "600.00")
    private BigDecimal rate;

    @Schema(description = "Total wages earned (rate * totalPresenties)", example = "13800.00")
    private BigDecimal totalEarnings;

    @Schema(description = "Opening advance balance carried forward from prior settled month (+/-)", example = "1000.00")
    private BigDecimal openingBalance = BigDecimal.ZERO;

    @Schema(description = "Advance money disbursed in the current month", example = "6000.00")
    private BigDecimal monthlyAdvance = BigDecimal.ZERO;

    @Schema(description = "Total advance balance for this month (openingBalance + monthlyAdvance)", example = "7000.00")
    private BigDecimal totalAdvance;

    @Schema(description = "Cumulative unsettled running advance across all time", example = "3000.00")
    private BigDecimal runningAdvanceBalance;

    @Schema(description = "Final money payable to worker (totalEarnings - totalAdvance)", example = "10800.00")
    private BigDecimal finalMoney;

    @Schema(description = "Actual money paid out to employee upon settlement clearance", example = "2000.00")
    private BigDecimal settlementAmount;

    @Schema(description = "Carry forward balance to next month's opening advance (finalMoney - settlementAmount)", example = "1000.00")
    private BigDecimal carryForward;

    @Schema(description = "Hisab status (RUNNING or SETTLED)", example = "RUNNING")
    private HisabStatus status;

    @Schema(description = "Payment mode if settled", example = "CASH")
    private PaymentMode paymentMode;

    @Schema(description = "Timestamp when settled")
    private Instant settledAt;

    @Schema(description = "Optional note or remarks", example = "Monthly hisab cleared")
    private String note;

    public HisabTableRow() {
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public LocalDate getJoiningDate() {
        return joiningDate;
    }

    public void setJoiningDate(LocalDate joiningDate) {
        this.joiningDate = joiningDate;
    }

    public int getFullDays() {
        return fullDays;
    }

    public void setFullDays(int fullDays) {
        this.fullDays = fullDays;
    }

    public int getHalfDays() {
        return halfDays;
    }

    public void setHalfDays(int halfDays) {
        this.halfDays = halfDays;
    }

    public double getTotalPresenties() {
        return totalPresenties;
    }

    public void setTotalPresenties(double totalPresenties) {
        this.totalPresenties = totalPresenties;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    public BigDecimal getTotalEarnings() {
        return totalEarnings;
    }

    public void setTotalEarnings(BigDecimal totalEarnings) {
        this.totalEarnings = totalEarnings;
    }

    public BigDecimal getOpeningBalance() {
        return openingBalance;
    }

    public void setOpeningBalance(BigDecimal openingBalance) {
        this.openingBalance = openingBalance != null ? openingBalance : BigDecimal.ZERO;
    }

    public BigDecimal getMonthlyAdvance() {
        return monthlyAdvance;
    }

    public void setMonthlyAdvance(BigDecimal monthlyAdvance) {
        this.monthlyAdvance = monthlyAdvance != null ? monthlyAdvance : BigDecimal.ZERO;
    }

    public BigDecimal getTotalAdvance() {
        return totalAdvance;
    }

    public void setTotalAdvance(BigDecimal totalAdvance) {
        this.totalAdvance = totalAdvance;
    }

    public BigDecimal getRunningAdvanceBalance() {
        return runningAdvanceBalance;
    }

    public void setRunningAdvanceBalance(BigDecimal runningAdvanceBalance) {
        this.runningAdvanceBalance = runningAdvanceBalance;
    }

    public BigDecimal getFinalMoney() {
        return finalMoney;
    }

    public void setFinalMoney(BigDecimal finalMoney) {
        this.finalMoney = finalMoney;
    }

    public BigDecimal getSettlementAmount() {
        return settlementAmount;
    }

    public void setSettlementAmount(BigDecimal settlementAmount) {
        this.settlementAmount = settlementAmount;
    }

    public BigDecimal getCarryForward() {
        return carryForward;
    }

    public void setCarryForward(BigDecimal carryForward) {
        this.carryForward = carryForward;
    }

    public HisabStatus getStatus() {
        return status;
    }

    public void setStatus(HisabStatus status) {
        this.status = status;
    }

    public PaymentMode getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(PaymentMode paymentMode) {
        this.paymentMode = paymentMode;
    }

    public Instant getSettledAt() {
        return settledAt;
    }

    public void setSettledAt(Instant settledAt) {
        this.settledAt = settledAt;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
