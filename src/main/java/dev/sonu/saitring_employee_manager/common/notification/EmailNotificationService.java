package dev.sonu.saitring_employee_manager.common.notification;

public interface EmailNotificationService {
    void sendOtpEmail(String recipientEmail, String recipientName, String otp);
}
