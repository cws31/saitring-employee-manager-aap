package dev.sonu.saitring_employee_manager.common.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class BrevoEmailService implements EmailNotificationService {

    private static final Logger log = LoggerFactory.getLogger(BrevoEmailService.class);
    private static final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";

    private final String apiKey;
    private final String senderEmail;
    private final String senderName;
    private final RestClient restClient;

    public BrevoEmailService(
            @Value("${app.brevo.api-key:}") String apiKey,
            @Value("${app.brevo.sender-email:sema.servicesupport@gmail.com}") String senderEmail,
            @Value("${app.brevo.sender-name:Saitring Security}") String senderName) {
        this.apiKey = apiKey != null ? apiKey.trim() : "";
        this.senderEmail = senderEmail;
        this.senderName = senderName;
        this.restClient = RestClient.builder()
                .baseUrl(BREVO_API_URL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public void sendOtpEmail(String recipientEmail, String recipientName, String otp) {
        if (apiKey.isBlank()) {
            log.warn("[DEV MODE / NO BREVO KEY] Simulation: Sent OTP [{}] to email [{}]", otp, recipientEmail);
            return;
        }

        String safeRecipientName = (recipientName != null && !recipientName.isBlank()) ? recipientName : "Valued Owner";
        String htmlContent = buildHtmlTemplate(safeRecipientName, otp);

        Map<String, Object> payload = Map.of(
                "sender", Map.of("name", senderName, "email", senderEmail),
                "to", List.of(Map.of("email", recipientEmail, "name", safeRecipientName)),
                "subject", "Your Saitring Manager Security Code: " + otp,
                "htmlContent", htmlContent
        );

        try {
            restClient.post()
                    .header("api-key", apiKey)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Successfully dispatched OTP email via Brevo to [{}]", recipientEmail);
        } catch (Exception ex) {
            log.error("Failed to dispatch OTP email via Brevo to [{}]: {}", recipientEmail, ex.getMessage());
            // In dev or sandbox environments where sender domain is unverified, do not crash the auth flow
        }
    }

    private String buildHtmlTemplate(String recipientName, String otp) {
        return """
                <div style="font-family: Arial, sans-serif; max-width: 540px; margin: 0 auto; padding: 24px; border: 1px solid #e2e8f0; border-radius: 8px; background-color: #ffffff;">
                    <div style="text-align: center; margin-bottom: 24px;">
                        <h2 style="color: #1e293b; margin: 0;">Saitring Employee Manager</h2>
                        <p style="color: #64748b; font-size: 14px; margin-top: 4px;">Two-Factor Authentication Verification</p>
                    </div>
                    <p style="color: #334155; font-size: 16px;">Hello <strong>%s</strong>,</p>
                    <p style="color: #475569; font-size: 14px; line-height: 1.6;">
                        Use the verification code below to complete your login. This code is valid for <strong>5 minutes</strong>.
                    </p>
                    <div style="text-align: center; margin: 28px 0;">
                        <span style="font-size: 32px; font-weight: bold; letter-spacing: 6px; color: #0284c7; background-color: #f0f9ff; padding: 12px 24px; border-radius: 6px; display: inline-block;">
                            %s
                        </span>
                    </div>
                    <p style="color: #94a3b8; font-size: 12px; line-height: 1.5; margin-top: 24px;">
                        If you did not request this code, please ignore this email or review your account security immediately. Do not share this OTP with anyone.
                    </p>
                </div>
                """.formatted(recipientName, otp);
    }
}
