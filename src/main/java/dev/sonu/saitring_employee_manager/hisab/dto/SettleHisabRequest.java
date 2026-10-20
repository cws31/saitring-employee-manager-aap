package dev.sonu.saitring_employee_manager.hisab.dto;

import dev.sonu.saitring_employee_manager.hisab.entity.PaymentMode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Request body to settle monthly hisab for an employee")
public class SettleHisabRequest {

    @NotNull(message = "Employee ID is required")
    @Schema(description = "ID of the employee", example = "1")
    private Long employeeId;

    @NotBlank(message = "Month is required")
    @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])$", message = "Month must be in YYYY-MM format")
    @Schema(description = "Month to settle in YYYY-MM format", example = "2026-10")
    private String month;

    @NotNull(message = "Payment mode is required (CASH, UPI, BANK_TRANSFER, CHEQUE, OTHER)")
    @Schema(description = "Payment mode for final payout settlement", example = "CASH")
    private PaymentMode paymentMode;

    @Schema(description = "Actual money paid to employee at settlement clearance. If omitted/null, defaults to entire finalMoney (complete payout)", example = "2000.00")
    private BigDecimal settlementAmount;

    @Size(max = 255, message = "Note cannot exceed 255 characters")
    @Schema(description = "Optional settlement remarks or transaction reference", example = "Paid cash at site")
    private String note;

    public SettleHisabRequest() {
    }

    public SettleHisabRequest(Long employeeId, String month, PaymentMode paymentMode, String note) {
        this.employeeId = employeeId;
        this.month = month;
        this.paymentMode = paymentMode;
        this.note = note;
    }

    public SettleHisabRequest(Long employeeId, String month, PaymentMode paymentMode, BigDecimal settlementAmount, String note) {
        this.employeeId = employeeId;
        this.month = month;
        this.paymentMode = paymentMode;
        this.settlementAmount = settlementAmount;
        this.note = note;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public PaymentMode getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(PaymentMode paymentMode) {
        this.paymentMode = paymentMode;
    }

    public BigDecimal getSettlementAmount() {
        return settlementAmount;
    }

    public void setSettlementAmount(BigDecimal settlementAmount) {
        this.settlementAmount = settlementAmount;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
