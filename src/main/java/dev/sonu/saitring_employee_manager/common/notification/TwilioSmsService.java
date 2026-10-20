package dev.sonu.saitring_employee_manager.common.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class TwilioSmsService implements SmsNotificationService {

    private static final Logger log = LoggerFactory.getLogger(TwilioSmsService.class);

    private final String accountSid;
    private final String authToken;
    private final String fromNumber;
    private final RestClient restClient;

    public TwilioSmsService(
            @Value("${app.twilio.account-sid:}") String accountSid,
            @Value("${app.twilio.auth-token:}") String authToken,
            @Value("${app.twilio.from-number:}") String fromNumber) {
        this.accountSid = accountSid != null ? accountSid.trim() : "";
        this.authToken = authToken != null ? authToken.trim() : "";
        this.fromNumber = fromNumber != null ? fromNumber.trim() : "";
        this.restClient = RestClient.builder().build();
    }

    @Override
    public void sendOtpSms(String recipientPhoneNumber, String otp) {
        if (accountSid.isBlank() || authToken.isBlank() || fromNumber.isBlank()) {
            log.warn("[DEV MODE / NO TWILIO CREDS] Simulation: Dispatched OTP [{}] to mobile [{}]", otp, recipientPhoneNumber);
            return;
        }

        String url = String.format("https://api.twilio.com/2010-04-01/Accounts/%s/Messages.json", accountSid);
        String authHeader = "Basic " + Base64.getEncoder().encodeToString((accountSid + ":" + authToken).getBytes(StandardCharsets.UTF_8));
        String messageBody = "Your Saitring Manager verification code is: " + otp + ". Valid for 5 minutes. Do not share.";

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("To", recipientPhoneNumber);
        formData.add("From", fromNumber);
        formData.add("Body", messageBody);

        try {
            restClient.post()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, authHeader)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formData)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Successfully dispatched OTP SMS via Twilio to [{}]", recipientPhoneNumber);
        } catch (Exception ex) {
            log.error("Failed to dispatch OTP SMS via Twilio to [{}]: {}", recipientPhoneNumber, ex.getMessage());
        }
    }
}
