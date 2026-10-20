package dev.sonu.saitring_employee_manager.owner.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UpdateOwnerProfileRequest {

    @NotBlank(message = "Owner name is required")
    @Size(min = 2, max = 100, message = "Owner name must be between 2 and 100 characters")
    private String ownerName;

    @NotBlank(message = "Organization name is required")
    @Size(min = 2, max = 150, message = "Organization name must be between 2 and 150 characters")
    private String organizationName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Phone number must be valid with 10 to 15 digits")
    private String phoneNumber;

    @NotBlank(message = "Address is required")
    @Size(max = 1000, message = "Address cannot exceed 1000 characters")
    private String address;

    public UpdateOwnerProfileRequest() {
    }

    public UpdateOwnerProfileRequest(String ownerName, String organizationName, String email, String phoneNumber, String address) {
        this.ownerName = ownerName;
        this.organizationName = organizationName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
