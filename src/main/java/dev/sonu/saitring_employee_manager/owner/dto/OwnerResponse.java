package dev.sonu.saitring_employee_manager.owner.dto;

import dev.sonu.saitring_employee_manager.owner.entity.Owner;
import dev.sonu.saitring_employee_manager.owner.entity.OwnerRole;
import dev.sonu.saitring_employee_manager.owner.entity.OwnerStatus;

import java.time.Instant;

public class OwnerResponse {
    private Long id;
    private String ownerName;
    private String organizationName;
    private String email;
    private String phoneNumber;
    private String address;
    private String logoUrl;
    private OwnerStatus status;
    private OwnerRole role;
    private Instant createdAt;

    public OwnerResponse() {
    }

    public OwnerResponse(Long id, String ownerName, String organizationName, String email,
                         String phoneNumber, String address, String logoUrl, OwnerStatus status,
                         OwnerRole role, Instant createdAt) {
        this.id = id;
        this.ownerName = ownerName;
        this.organizationName = organizationName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.logoUrl = logoUrl;
        this.status = status;
        this.role = role;
        this.createdAt = createdAt;
    }

    public static OwnerResponse fromEntity(Owner owner) {
        return new OwnerResponse(
                owner.getId(),
                owner.getOwnerName(),
                owner.getOrganizationName(),
                owner.getEmail(),
                owner.getPhoneNumber(),
                owner.getAddress(),
                owner.getLogoUrl(),
                owner.getStatus(),
                owner.getRole(),
                owner.getCreatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public OwnerStatus getStatus() {
        return status;
    }

    public void setStatus(OwnerStatus status) {
        this.status = status;
    }

    public OwnerRole getRole() {
        return role;
    }

    public void setRole(OwnerRole role) {
        this.role = role;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
