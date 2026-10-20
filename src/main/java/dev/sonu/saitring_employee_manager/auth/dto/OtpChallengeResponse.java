package dev.sonu.saitring_employee_manager.auth.dto;

public class OtpChallengeResponse {

    private String challengeToken;
    private OtpDeliveryChannel deliveryChannel;
    private String maskedDestination;
    private long expiresInSeconds;
    private String message;

    public OtpChallengeResponse() {
    }

    public OtpChallengeResponse(String challengeToken, OtpDeliveryChannel deliveryChannel,
                                String maskedDestination, long expiresInSeconds, String message) {
        this.challengeToken = challengeToken;
        this.deliveryChannel = deliveryChannel;
        this.maskedDestination = maskedDestination;
        this.expiresInSeconds = expiresInSeconds;
        this.message = message;
    }

    public String getChallengeToken() {
        return challengeToken;
    }

    public void setChallengeToken(String challengeToken) {
        this.challengeToken = challengeToken;
    }

    public OtpDeliveryChannel getDeliveryChannel() {
        return deliveryChannel;
    }

    public void setDeliveryChannel(OtpDeliveryChannel deliveryChannel) {
        this.deliveryChannel = deliveryChannel;
    }

    public String getMaskedDestination() {
        return maskedDestination;
    }

    public void setMaskedDestination(String maskedDestination) {
        this.maskedDestination = maskedDestination;
    }

    public long getExpiresInSeconds() {
        return expiresInSeconds;
    }

    public void setExpiresInSeconds(long expiresInSeconds) {
        this.expiresInSeconds = expiresInSeconds;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
