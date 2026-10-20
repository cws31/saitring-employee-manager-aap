package dev.sonu.saitring_employee_manager.owner;

import dev.sonu.saitring_employee_manager.common.exception.ConflictException;
import dev.sonu.saitring_employee_manager.common.exception.ResourceNotFoundException;
import dev.sonu.saitring_employee_manager.common.storage.FileStorageService;
import dev.sonu.saitring_employee_manager.owner.dto.OwnerResponse;
import dev.sonu.saitring_employee_manager.owner.dto.UpdateOwnerProfileRequest;
import dev.sonu.saitring_employee_manager.owner.entity.Owner;
import dev.sonu.saitring_employee_manager.owner.repository.OwnerRepository;
import dev.sonu.saitring_employee_manager.owner.service.OwnerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OwnerServiceTest {

    @Mock
    private OwnerRepository ownerRepository;

    @Mock
    private FileStorageService fileStorageService;

    private OwnerServiceImpl ownerService;

    @BeforeEach
    void setUp() {
        ownerService = new OwnerServiceImpl(ownerRepository, fileStorageService);
    }

    @Test
    @DisplayName("getProfile returns OwnerResponse for valid ID")
    void getProfileSuccess() {
        Owner owner = new Owner("Suresh", "Suresh Scaffolding", "suresh@scaffolding.com", "+919876500000", "hash", "Pune", "https://logo.png");
        owner.setId(5L);

        when(ownerRepository.findById(5L)).thenReturn(Optional.of(owner));

        OwnerResponse response = ownerService.getProfile(5L);

        assertNotNull(response);
        assertEquals(5L, response.getId());
        assertEquals("Suresh", response.getOwnerName());
        assertEquals("Suresh Scaffolding", response.getOrganizationName());
    }

    @Test
    @DisplayName("getProfile throws ResourceNotFoundException when owner ID not found")
    void getProfileNotFound() {
        when(ownerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> ownerService.getProfile(99L));
    }

    @Test
    @DisplayName("updateProfile successfully updates fields and uploads new logo")
    void updateProfileSuccess() {
        Owner owner = new Owner("Suresh", "Suresh Scaffolding", "suresh@scaffolding.com", "+919876500000", "hash", "Pune", "https://old-logo.png");
        owner.setId(5L);

        when(ownerRepository.findById(5L)).thenReturn(Optional.of(owner));
        when(ownerRepository.existsByEmailAndIdNot("newemail@scaffolding.com", 5L)).thenReturn(false);
        when(ownerRepository.existsByPhoneNumberAndIdNot("+919876599999", 5L)).thenReturn(false);

        MockMultipartFile logoFile = new MockMultipartFile("logo", "logo.png", "image/png", new byte[]{1, 2, 3});
        when(fileStorageService.uploadLogo(logoFile, 5L)).thenReturn("https://supabase.co/logos/new-logo.png");
        when(ownerRepository.save(any(Owner.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateOwnerProfileRequest request = new UpdateOwnerProfileRequest(
                "Suresh Patil", "Patil Saitring & Formwork", "newemail@scaffolding.com", "+919876599999", "New Pune Road"
        );

        OwnerResponse response = ownerService.updateProfile(5L, request, logoFile);

        assertNotNull(response);
        assertEquals("Suresh Patil", response.getOwnerName());
        assertEquals("Patil Saitring & Formwork", response.getOrganizationName());
        assertEquals("newemail@scaffolding.com", response.getEmail());
        assertEquals("+919876599999", response.getPhoneNumber());
        assertEquals("https://supabase.co/logos/new-logo.png", response.getLogoUrl());

        verify(fileStorageService).deleteFile("https://old-logo.png");
    }

    @Test
    @DisplayName("updateProfile throws ConflictException when email is taken by another owner")
    void updateProfileRejectsTakenEmail() {
        Owner owner = new Owner("Suresh", "Suresh Scaffolding", "suresh@scaffolding.com", "+919876500000", "hash", "Pune", null);
        owner.setId(5L);

        when(ownerRepository.findById(5L)).thenReturn(Optional.of(owner));
        when(ownerRepository.existsByEmailAndIdNot("taken@scaffolding.com", 5L)).thenReturn(true);

        UpdateOwnerProfileRequest request = new UpdateOwnerProfileRequest(
                "Suresh", "Suresh Scaffolding", "taken@scaffolding.com", "+919876500000", "Pune"
        );

        assertThrows(ConflictException.class, () -> ownerService.updateProfile(5L, request, null));
        verify(ownerRepository, never()).save(any());
    }
}
