package dev.sonu.saitring_employee_manager.owner.service;

import dev.sonu.saitring_employee_manager.common.exception.ConflictException;
import dev.sonu.saitring_employee_manager.common.exception.ResourceNotFoundException;
import dev.sonu.saitring_employee_manager.common.storage.FileStorageService;
import dev.sonu.saitring_employee_manager.owner.dto.OwnerResponse;
import dev.sonu.saitring_employee_manager.owner.dto.UpdateOwnerProfileRequest;
import dev.sonu.saitring_employee_manager.owner.entity.Owner;
import dev.sonu.saitring_employee_manager.owner.repository.OwnerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class OwnerServiceImpl implements OwnerService {

    private static final Logger log = LoggerFactory.getLogger(OwnerServiceImpl.class);

    private final OwnerRepository ownerRepository;
    private final FileStorageService fileStorageService;

    public OwnerServiceImpl(OwnerRepository ownerRepository, FileStorageService fileStorageService) {
        this.ownerRepository = ownerRepository;
        this.fileStorageService = fileStorageService;
    }

    @Override
    @Transactional(readOnly = true)
    public OwnerResponse getProfile(Long ownerId) {
        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Owner profile not found with ID: " + ownerId));
        return OwnerResponse.fromEntity(owner);
    }

    @Override
    @Transactional
    public OwnerResponse updateProfile(Long ownerId, UpdateOwnerProfileRequest request, MultipartFile logoFile) {
        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Owner profile not found with ID: " + ownerId));

        String newEmail = request.getEmail().trim().toLowerCase();
        String newPhone = request.getPhoneNumber().trim();

        if (ownerRepository.existsByEmailAndIdNot(newEmail, ownerId)) {
            throw new ConflictException("Email address is already in use by another account: " + newEmail);
        }

        if (ownerRepository.existsByPhoneNumberAndIdNot(newPhone, ownerId)) {
            throw new ConflictException("Phone number is already in use by another account: " + newPhone);
        }

        owner.setOwnerName(request.getOwnerName().trim());
        owner.setOrganizationName(request.getOrganizationName().trim());
        owner.setEmail(newEmail);
        owner.setPhoneNumber(newPhone);
        owner.setAddress(request.getAddress().trim());

        if (logoFile != null && !logoFile.isEmpty()) {
            String oldLogoUrl = owner.getLogoUrl();
            String newLogoUrl = fileStorageService.uploadLogo(logoFile, ownerId);
            owner.setLogoUrl(newLogoUrl);

            // Clean up old logo asynchronously or best-effort
            if (oldLogoUrl != null && !oldLogoUrl.isBlank()) {
                fileStorageService.deleteFile(oldLogoUrl);
            }
        }

        Owner updatedOwner = ownerRepository.save(owner);
        log.info("Successfully updated profile for owner ID [{}]", ownerId);

        return OwnerResponse.fromEntity(updatedOwner);
    }
}
