package dev.sonu.saitring_employee_manager.auth;

import dev.sonu.saitring_employee_manager.auth.dto.*;
import dev.sonu.saitring_employee_manager.auth.service.AuthServiceImpl;
import dev.sonu.saitring_employee_manager.auth.service.OtpChallengeData;
import dev.sonu.saitring_employee_manager.auth.service.OtpService;
import dev.sonu.saitring_employee_manager.common.exception.ConflictException;
import dev.sonu.saitring_employee_manager.common.exception.UnauthorizedException;
import dev.sonu.saitring_employee_manager.common.storage.FileStorageService;
import dev.sonu.saitring_employee_manager.owner.dto.OwnerResponse;
import dev.sonu.saitring_employee_manager.owner.entity.Owner;
import dev.sonu.saitring_employee_manager.owner.entity.OwnerRole;
import dev.sonu.saitring_employee_manager.owner.entity.OwnerStatus;
import dev.sonu.saitring_employee_manager.owner.repository.OwnerRepository;
import dev.sonu.saitring_employee_manager.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private OwnerRepository ownerRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private OtpService otpService;

    @Mock
    private FileStorageService fileStorageService;

    private final JwtTokenProvider jwtTokenProvider = new JwtTokenProvider("dGVzdC1zZWNyZXQta2V5LXRlc3Qtc2VjcmV0LWtleS0xMjM0NTY3ODkwMTI=", 86400);

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(
                ownerRepository,
                passwordEncoder,
                otpService,
                fileStorageService,
                jwtTokenProvider
        );
    }

    @Test
    @DisplayName("Registration succeeds with valid unique details")
    void registerSuccess() {
        RegisterRequest request = new RegisterRequest(
                "Ramesh Kumar",
                "Ramesh Saitring Works",
                "ramesh@saitring.com",
                "+919876543210",
                "Secret@123",
                "Shop 14, Ring Road, Surat"
        );

        when(ownerRepository.existsByEmail("ramesh@saitring.com")).thenReturn(false);
        when(ownerRepository.existsByPhoneNumber("+919876543210")).thenReturn(false);
        when(passwordEncoder.encode("Secret@123")).thenReturn("hashedPassword");

        Owner savedOwner = new Owner(
                "Ramesh Kumar",
                "Ramesh Saitring Works",
                "ramesh@saitring.com",
                "+919876543210",
                "hashedPassword",
                "Shop 14, Ring Road, Surat",
                null
        );
        savedOwner.setId(1L);
        when(ownerRepository.save(any(Owner.class))).thenReturn(savedOwner);

        OwnerResponse response = authService.register(request, null);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("ramesh@saitring.com", response.getEmail());
        assertEquals("Ramesh Saitring Works", response.getOrganizationName());
        verify(ownerRepository).save(any(Owner.class));
    }

    @Test
    @DisplayName("Registration throws ConflictException when email is already registered")
    void registerRejectsDuplicateEmail() {
        RegisterRequest request = new RegisterRequest(
                "Ramesh Kumar", "Ramesh Saitring", "existing@saitring.com", "+919876543210", "Secret@123", "Surat"
        );

        when(ownerRepository.existsByEmail("existing@saitring.com")).thenReturn(true);

        assertThrows(ConflictException.class, () -> authService.register(request, null));
        verify(ownerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Registration throws ConflictException when mobile number is already registered")
    void registerRejectsDuplicateMobile() {
        RegisterRequest request = new RegisterRequest(
                "Ramesh Kumar", "Ramesh Saitring", "ramesh@saitring.com", "+919876543210", "Secret@123", "Surat"
        );

        when(ownerRepository.existsByEmail("ramesh@saitring.com")).thenReturn(false);
        when(ownerRepository.existsByPhoneNumber("+919876543210")).thenReturn(true);

        assertThrows(ConflictException.class, () -> authService.register(request, null));
        verify(ownerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Login with email initiates dynamic 2FA via EMAIL channel")
    void loginWithEmailDispatchesEmailOtp() {
        LoginRequest request = new LoginRequest("owner@saitring.com", "Password123");

        Owner owner = new Owner(
                "Owner Name", "Acme", "owner@saitring.com", "+919876543210", "hashedPassword", "Addr", null
        );
        owner.setId(1L);

        when(ownerRepository.findByIdentifier("owner@saitring.com")).thenReturn(Optional.of(owner));
        when(passwordEncoder.matches("Password123", "hashedPassword")).thenReturn(true);

        OtpChallengeResponse expectedChallenge = new OtpChallengeResponse(
                "challenge-uuid-123", OtpDeliveryChannel.EMAIL, "o***@saitring.com", 300, "OTP sent to email"
        );
        when(otpService.createAndSendOtpChallenge(owner, OtpDeliveryChannel.EMAIL)).thenReturn(expectedChallenge);

        OtpChallengeResponse actual = authService.initiateLogin(request);

        assertNotNull(actual);
        assertEquals(OtpDeliveryChannel.EMAIL, actual.getDeliveryChannel());
        assertEquals("challenge-uuid-123", actual.getChallengeToken());
        verify(otpService).createAndSendOtpChallenge(owner, OtpDeliveryChannel.EMAIL);
    }

    @Test
    @DisplayName("Login with mobile phone initiates dynamic 2FA via SMS channel")
    void loginWithPhoneDispatchesSmsOtp() {
        LoginRequest request = new LoginRequest("+919876543210", "Password123");

        Owner owner = new Owner(
                "Owner Name", "Acme", "owner@saitring.com", "+919876543210", "hashedPassword", "Addr", null
        );
        owner.setId(1L);

        when(ownerRepository.findByIdentifier("+919876543210")).thenReturn(Optional.of(owner));
        when(passwordEncoder.matches("Password123", "hashedPassword")).thenReturn(true);

        OtpChallengeResponse expectedChallenge = new OtpChallengeResponse(
                "challenge-uuid-456", OtpDeliveryChannel.SMS, "+91*****3210", 300, "OTP sent to mobile"
        );
        when(otpService.createAndSendOtpChallenge(owner, OtpDeliveryChannel.SMS)).thenReturn(expectedChallenge);

        OtpChallengeResponse actual = authService.initiateLogin(request);

        assertNotNull(actual);
        assertEquals(OtpDeliveryChannel.SMS, actual.getDeliveryChannel());
        verify(otpService).createAndSendOtpChallenge(owner, OtpDeliveryChannel.SMS);
    }

    @Test
    @DisplayName("Login rejects incorrect password with UnauthorizedException")
    void loginRejectsWrongPassword() {
        LoginRequest request = new LoginRequest("owner@saitring.com", "WrongPassword");

        Owner owner = new Owner(
                "Owner Name", "Acme", "owner@saitring.com", "+919876543210", "hashedPassword", "Addr", null
        );

        when(ownerRepository.findByIdentifier("owner@saitring.com")).thenReturn(Optional.of(owner));
        when(passwordEncoder.matches("WrongPassword", "hashedPassword")).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> authService.initiateLogin(request));
        verify(otpService, never()).createAndSendOtpChallenge(any(), any());
    }

    @Test
    @DisplayName("Login rejects inactive owner account")
    void loginRejectsInactiveOwner() {
        LoginRequest request = new LoginRequest("owner@saitring.com", "Password123");

        Owner owner = new Owner(
                "Owner Name", "Acme", "owner@saitring.com", "+919876543210", "hashedPassword", "Addr", null
        );
        owner.setStatus(OwnerStatus.SUSPENDED);

        when(ownerRepository.findByIdentifier("owner@saitring.com")).thenReturn(Optional.of(owner));
        when(passwordEncoder.matches("Password123", "hashedPassword")).thenReturn(true);

        assertThrows(UnauthorizedException.class, () -> authService.initiateLogin(request));
    }

    @Test
    @DisplayName("Verify OTP validates challenge and returns JWT AuthResponse")
    void verifyOtpSuccess() {
        VerifyOtpRequest request = new VerifyOtpRequest("challenge-uuid", "123456");

        OtpChallengeData challengeData = new OtpChallengeData(
                1L, "owner@saitring.com", "123456", OtpDeliveryChannel.EMAIL, "owner@saitring.com", 0,
                Instant.now(), Instant.now().plusSeconds(300)
        );
        when(otpService.validateOtp("challenge-uuid", "123456")).thenReturn(challengeData);

        Owner owner = new Owner(
                "Owner Name", "Acme", "owner@saitring.com", "+919876543210", "hash", "Addr", null
        );
        owner.setId(1L);
        when(ownerRepository.findById(1L)).thenReturn(Optional.of(owner));

        AuthResponse response = authService.verifyOtp(request);

        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(86400L, response.getExpiresInSeconds());
        assertEquals(1L, response.getOwner().getId());
    }
}
