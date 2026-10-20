package dev.sonu.saitring_employee_manager.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class VerifyOtpRequest {

    @NotBlank(message = "Challenge token is required")
    private String challengeToken;

    @NotBlank(message = "OTP is required")
    @Pattern(regexp = "^\\d{6}$", message = "OTP must be a 6-digit numeric code")
    private String otp;

    public VerifyOtpRequest() {
    }

    public VerifyOtpRequest(String challengeToken, String otp) {
        this.challengeToken = challengeToken;
        this.otp = otp;
    }

    public String getChallengeToken() {
        return challengeToken;
    }

    public void setChallengeToken(String challengeToken) {
        this.challengeToken = challengeToken;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }
}
