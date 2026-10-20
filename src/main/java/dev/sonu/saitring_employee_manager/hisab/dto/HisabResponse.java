package dev.sonu.saitring_employee_manager.hisab.dto;

import dev.sonu.saitring_employee_manager.hisab.entity.HisabStatus;
import dev.sonu.saitring_employee_manager.hisab.entity.MonthlyHisab;
import dev.sonu.saitring_employee_manager.hisab.entity.PaymentMode;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Schema(description = "Response representing a settled or calculated monthly hisab")
public class HisabResponse {

    @Schema(description = "Hisab record ID", example = "1")
    private Long id;

    @Schema(description = "Employee ID", example = "1")
    private Long employeeId;

    @Schema(description = "Employee Name", example = "Ramesh Kumar")
    private String employeeName;

    @Schema(description = "Mobile Number", example = "+919876543210")
    private String mobileNumber;

    @Schema(description = "Joining Date", example = "2026-09-01")
    private LocalDate joiningDate;

    @Schema(description = "Month in YYYY-MM format", example = "2026-10")
    private String month;

    @Schema(description = "Daily Wage Rate", example = "600.00")
    private BigDecimal dailyRate;

    @Schema(description = "Full days worked (PRESENT)", example = "22")
    private int fullDays;

    @Schema(description = "Half days worked (HALF_DAY)", example = "2")
    private int halfDays;

    @Schema(description = "Total effective presenties worked", example = "23.0")
    private double effectiveDays;

    @Schema(description = "Total wages earned", example = "13800.00")
    private BigDecimal totalEarnings;

    @Schema(description = "Total advance deducted", example = "3000.00")
    private BigDecimal totalAdvance;

    @Schema(description = "Final money (totalEarnings - totalAdvance)", example = "10800.00")
    private BigDecimal finalMoney;

    @Schema(description = "Hisab status (RUNNING or SETTLED)", example = "SETTLED")
    private HisabStatus status;

    @Schema(description = "Payment mode for settlement", example = "CASH")
    private PaymentMode paymentMode;

    @Schema(description = "Settlement timestamp")
    private Instant settledAt;

    @Schema(description = "Settlement remarks or note", example = "Paid cash at site")
    private String note;

    @Schema(description = "Record creation timestamp")
    private Instant createdAt;

    public HisabResponse() {
    }

    public static HisabResponse fromEntity(MonthlyHisab entity) {
        HisabResponse dto = new HisabResponse();
        dto.setId(entity.getId());
        if (entity.getEmployee() != null) {
            dto.setEmployeeId(entity.getEmployee().getId());
            dto.setEmployeeName(entity.getEmployee().getEmployeeName());
            dto.setMobileNumber(entity.getEmployee().getMobileNumber());
            dto.setJoiningDate(entity.getEmployee().getJoiningDate());
        }
        dto.setMonth(entity.getMonth());
        dto.setDailyRate(entity.getDailyRate());
        dto.setFullDays(entity.getFullDays());
        dto.setHalfDays(entity.getHalfDays());
        dto.setEffectiveDays(entity.getEffectiveDays() != null ? entity.getEffectiveDays().doubleValue() : 0.0);
        dto.setTotalEarnings(entity.getTotalEarnings());
        dto.setTotalAdvance(entity.getTotalAdvance());
        dto.setFinalMoney(entity.getFinalMoney());
        dto.setStatus(entity.getStatus());
        dto.setPaymentMode(entity.getPaymentMode());
        dto.setSettledAt(entity.getSettledAt());
        dto.setNote(entity.getNote());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public BigDecimal getDailyRate() {
        return dailyRate;
    }

    public void setDailyRate(BigDecimal dailyRate) {
        this.dailyRate = dailyRate;
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

    public double getEffectiveDays() {
        return effectiveDays;
    }

    public void setEffectiveDays(double effectiveDays) {
        this.effectiveDays = effectiveDays;
    }

    public BigDecimal getTotalEarnings() {
        return totalEarnings;
    }

    public void setTotalEarnings(BigDecimal totalEarnings) {
        this.totalEarnings = totalEarnings;
    }

    public BigDecimal getTotalAdvance() {
        return totalAdvance;
    }

    public void setTotalAdvance(BigDecimal totalAdvance) {
        this.totalAdvance = totalAdvance;
    }

    public BigDecimal getFinalMoney() {
        return finalMoney;
    }

    public void setFinalMoney(BigDecimal finalMoney) {
        this.finalMoney = finalMoney;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
