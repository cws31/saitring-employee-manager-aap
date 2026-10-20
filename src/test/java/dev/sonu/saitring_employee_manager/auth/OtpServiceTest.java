package dev.sonu.saitring_employee_manager.auth;

import dev.sonu.saitring_employee_manager.auth.dto.OtpChallengeResponse;
import dev.sonu.saitring_employee_manager.auth.dto.OtpDeliveryChannel;
import dev.sonu.saitring_employee_manager.auth.service.OtpChallengeData;
import dev.sonu.saitring_employee_manager.auth.service.OtpServiceImpl;
import dev.sonu.saitring_employee_manager.auth.service.OtpStorageService;
import dev.sonu.saitring_employee_manager.common.exception.BadRequestException;
import dev.sonu.saitring_employee_manager.common.notification.EmailNotificationService;
import dev.sonu.saitring_employee_manager.common.notification.SmsNotificationService;
import dev.sonu.saitring_employee_manager.owner.entity.Owner;
import dev.sonu.saitring_employee_manager.owner.repository.OwnerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtpServiceTest {

    @Mock
    private OtpStorageService otpStorageService;

    @Mock
    private EmailNotificationService emailNotificationService;

    @Mock
    private SmsNotificationService smsNotificationService;

    @Mock
    private OwnerRepository ownerRepository;

    private OtpServiceImpl otpService;

    @BeforeEach
    void setUp() {
        otpService = new OtpServiceImpl(
                otpStorageService,
                emailNotificationService,
                smsNotificationService,
                ownerRepository
        );
    }

    @Test
    @DisplayName("Create OTP challenge for EMAIL dispatches email and stores in cache")
    void createAndSendOtpChallengeEmail() {
        Owner owner = new Owner("Ramesh", "Ramesh Saitring", "ramesh@saitring.com", "+919876543210", "hash", "Addr", null);
        owner.setId(10L);

        OtpChallengeResponse response = otpService.createAndSendOtpChallenge(owner, OtpDeliveryChannel.EMAIL);

        assertNotNull(response);
        assertEquals(OtpDeliveryChannel.EMAIL, response.getDeliveryChannel());
        assertNotNull(response.getChallengeToken());
        assertTrue(response.getMaskedDestination().contains("@"));

        verify(otpStorageService).storeChallenge(eq(response.getChallengeToken()), any(OtpChallengeData.class), any(Duration.class));
        verify(emailNotificationService).sendOtpEmail(eq("ramesh@saitring.com"), eq("Ramesh"), anyString());
        verifyNoInteractions(smsNotificationService);
    }

    @Test
    @DisplayName("Create OTP challenge for SMS dispatches SMS and stores in cache")
    void createAndSendOtpChallengeSms() {
        Owner owner = new Owner("Ramesh", "Ramesh Saitring", "ramesh@saitring.com", "+919876543210", "hash", "Addr", null);
        owner.setId(10L);

        OtpChallengeResponse response = otpService.createAndSendOtpChallenge(owner, OtpDeliveryChannel.SMS);

        assertNotNull(response);
        assertEquals(OtpDeliveryChannel.SMS, response.getDeliveryChannel());
        assertNotNull(response.getChallengeToken());

        verify(otpStorageService).storeChallenge(eq(response.getChallengeToken()), any(OtpChallengeData.class), any(Duration.class));
        verify(smsNotificationService).sendOtpSms(eq("+919876543210"), anyString());
        verifyNoInteractions(emailNotificationService);
    }

    @Test
    @DisplayName("Validate OTP succeeds and removes challenge from storage")
    void validateOtpSuccess() {
        String token = "valid-token";
        OtpChallengeData data = new OtpChallengeData(
                10L, "ramesh@saitring.com", "654321", OtpDeliveryChannel.EMAIL, "ramesh@saitring.com", 0,
                Instant.now(), Instant.now().plusSeconds(300)
        );

        when(otpStorageService.getChallenge(token)).thenReturn(Optional.of(data));

        OtpChallengeData result = otpService.validateOtp(token, "654321");

        assertNotNull(result);
        assertEquals(10L, result.getOwnerId());
        verify(otpStorageService).removeChallenge(token);
    }

    @Test
    @DisplayName("Validate OTP fails on mismatched code and increments attempts")
    void validateOtpFailsOnWrongCode() {
        String token = "valid-token";
        OtpChallengeData data = new OtpChallengeData(
                10L, "ramesh@saitring.com", "654321", OtpDeliveryChannel.EMAIL, "ramesh@saitring.com", 0,
                Instant.now(), Instant.now().plusSeconds(300)
        );

        when(otpStorageService.getChallenge(token)).thenReturn(Optional.of(data));

        BadRequestException ex = assertThrows(BadRequestException.class, () -> otpService.validateOtp(token, "000000"));
        assertTrue(ex.getMessage().contains("2 attempt(s) remaining"));

        ArgumentCaptor<OtpChallengeData> captor = ArgumentCaptor.forClass(OtpChallengeData.class);
        verify(otpStorageService).updateChallenge(eq(token), captor.capture(), any(Duration.class));
        assertEquals(1, captor.getValue().getAttempts());
    }

    @Test
    @DisplayName("Validate OTP throws BadRequestException when challenge expired")
    void validateOtpExpired() {
        String token = "expired-token";
        OtpChallengeData data = new OtpChallengeData(
                10L, "ramesh@saitring.com", "654321", OtpDeliveryChannel.EMAIL, "ramesh@saitring.com", 0,
                Instant.now().minusSeconds(400), Instant.now().minusSeconds(100)
        );

        when(otpStorageService.getChallenge(token)).thenReturn(Optional.of(data));

        assertThrows(BadRequestException.class, () -> otpService.validateOtp(token, "654321"));
        verify(otpStorageService).removeChallenge(token);
    }
}
