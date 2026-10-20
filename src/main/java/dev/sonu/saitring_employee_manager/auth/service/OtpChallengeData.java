package dev.sonu.saitring_employee_manager.auth.service;

import dev.sonu.saitring_employee_manager.auth.dto.OtpDeliveryChannel;

import java.io.Serializable;
import java.time.Instant;

public class OtpChallengeData implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long ownerId;
    private String identifier;
    private String otp;
    private OtpDeliveryChannel channel;
    private String destination;
    private int attempts;
    private Instant createdAt;
    private Instant expiresAt;

    public OtpChallengeData() {
    }

    public OtpChallengeData(Long ownerId, String identifier, String otp, OtpDeliveryChannel channel,
                            String destination, int attempts, Instant createdAt, Instant expiresAt) {
        this.ownerId = ownerId;
        this.identifier = identifier;
        this.otp = otp;
        this.channel = channel;
        this.destination = destination;
        this.attempts = attempts;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public OtpDeliveryChannel getChannel() {
        return channel;
    }

    public void setChannel(OtpDeliveryChannel channel) {
        this.channel = channel;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }
}
