package dev.sonu.saitring_employee_manager.auth.dto;

import jakarta.validation.constraints.NotBlank;

public class ResendOtpRequest {

    @NotBlank(message = "Challenge token is required")
    private String challengeToken;

    public ResendOtpRequest() {
    }

    public ResendOtpRequest(String challengeToken) {
        this.challengeToken = challengeToken;
    }

    public String getChallengeToken() {
        return challengeToken;
    }

    public void setChallengeToken(String challengeToken) {
        this.challengeToken = challengeToken;
    }
}
