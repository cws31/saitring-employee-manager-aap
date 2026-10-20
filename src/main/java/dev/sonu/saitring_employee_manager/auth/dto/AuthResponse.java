package dev.sonu.saitring_employee_manager.auth.dto;

import dev.sonu.saitring_employee_manager.owner.dto.OwnerResponse;

public class AuthResponse {

    private String accessToken;
    private String tokenType = "Bearer";
    private long expiresInSeconds;
    private OwnerResponse owner;

    public AuthResponse() {
    }

    public AuthResponse(String accessToken, String tokenType, long expiresInSeconds, OwnerResponse owner) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.expiresInSeconds = expiresInSeconds;
        this.owner = owner;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public long getExpiresInSeconds() {
        return expiresInSeconds;
    }

    public void setExpiresInSeconds(long expiresInSeconds) {
        this.expiresInSeconds = expiresInSeconds;
    }

    public OwnerResponse getOwner() {
        return owner;
    }

    public void setOwner(OwnerResponse owner) {
        this.owner = owner;
    }
}
