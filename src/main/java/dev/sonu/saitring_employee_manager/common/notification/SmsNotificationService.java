package dev.sonu.saitring_employee_manager.common.notification;

public interface SmsNotificationService {
    void sendOtpSms(String recipientPhoneNumber, String otp);
}
