package dev.sonu.saitring_employee_manager.auth.dto;

import jakarta.validation.constraints.NotBlank;
import org.springframework.util.StringUtils;

public class LoginRequest {

    private String identifier;
    private String email;
    private String phoneNumber;

    @NotBlank(message = "Password is required")
    private String password;

    public LoginRequest() {
    }

    public LoginRequest(String identifier, String password) {
        this.identifier = identifier;
        this.password = password;
    }

    public String getEffectiveIdentifier() {
        if (StringUtils.hasText(identifier)) {
            return identifier.trim();
        }
        if (StringUtils.hasText(email)) {
            return email.trim();
        }
        if (StringUtils.hasText(phoneNumber)) {
            return phoneNumber.trim();
        }
        return "";
    }

    public boolean isEmailFormat() {
        String eff = getEffectiveIdentifier();
        return eff.contains("@");
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
