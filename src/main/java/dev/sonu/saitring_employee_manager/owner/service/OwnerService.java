package dev.sonu.saitring_employee_manager.owner.service;

import dev.sonu.saitring_employee_manager.owner.dto.OwnerResponse;
import dev.sonu.saitring_employee_manager.owner.dto.UpdateOwnerProfileRequest;
import org.springframework.web.multipart.MultipartFile;

public interface OwnerService {
    OwnerResponse getProfile(Long ownerId);
    OwnerResponse updateProfile(Long ownerId, UpdateOwnerProfileRequest request, MultipartFile logoFile);
}
