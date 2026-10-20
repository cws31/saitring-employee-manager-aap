package dev.sonu.saitring_employee_manager.auth.service;

import dev.sonu.saitring_employee_manager.auth.dto.OtpChallengeResponse;
import dev.sonu.saitring_employee_manager.auth.dto.OtpDeliveryChannel;
import dev.sonu.saitring_employee_manager.common.exception.BadRequestException;
import dev.sonu.saitring_employee_manager.common.notification.EmailNotificationService;
import dev.sonu.saitring_employee_manager.common.notification.SmsNotificationService;
import dev.sonu.saitring_employee_manager.owner.entity.Owner;
import dev.sonu.saitring_employee_manager.owner.repository.OwnerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class OtpServiceImpl implements OtpService {

    private static final Logger log = LoggerFactory.getLogger(OtpServiceImpl.class);
    private static final int OTP_EXPIRY_SECONDS = 300; // 5 minutes
    private static final int MAX_ATTEMPTS = 3;

    private final OtpStorageService otpStorageService;
    private final EmailNotificationService emailNotificationService;
    private final SmsNotificationService smsNotificationService;
    private final OwnerRepository ownerRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public OtpServiceImpl(
            OtpStorageService otpStorageService,
            EmailNotificationService emailNotificationService,
            SmsNotificationService smsNotificationService,
            OwnerRepository ownerRepository) {
        this.otpStorageService = otpStorageService;
        this.emailNotificationService = emailNotificationService;
        this.smsNotificationService = smsNotificationService;
        this.ownerRepository = ownerRepository;
    }

    @Override
    public OtpChallengeResponse createAndSendOtpChallenge(Owner owner, OtpDeliveryChannel channel) {
        String otp = generateNumericOtp();
        String challengeToken = UUID.randomUUID().toString();
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(OTP_EXPIRY_SECONDS);

        String destination = channel == OtpDeliveryChannel.EMAIL ? owner.getEmail() : owner.getPhoneNumber();
        String identifier = channel == OtpDeliveryChannel.EMAIL ? owner.getEmail() : owner.getPhoneNumber();

        OtpChallengeData challengeData = new OtpChallengeData(
                owner.getId(),
                identifier,
                otp,
                channel,
                destination,
                0,
                now,
                expiresAt
        );

        otpStorageService.storeChallenge(challengeToken, challengeData, Duration.ofSeconds(OTP_EXPIRY_SECONDS));

        // Dispatch OTP dynamically based on channel
        dispatchOtp(channel, destination, owner.getOwnerName(), otp);

        String maskedDestination = maskDestination(channel, destination);
        String channelMessage = channel == OtpDeliveryChannel.EMAIL
                ? "OTP sent to your registered email address"
                : "OTP sent to your registered mobile number";

        log.info("Initiated 2FA challenge [{}] for owner id [{}] via [{}]", challengeToken, owner.getId(), channel);

        return new OtpChallengeResponse(
                challengeToken,
                channel,
                maskedDestination,
                OTP_EXPIRY_SECONDS,
                channelMessage
        );
    }

    @Override
    public OtpChallengeData validateOtp(String challengeToken, String enteredOtp) {
        OtpChallengeData challenge = otpStorageService.getChallenge(challengeToken)
                .orElseThrow(() -> new BadRequestException("OTP challenge has expired or is invalid. Please login again."));

        if (challenge.getExpiresAt().isBefore(Instant.now())) {
            otpStorageService.removeChallenge(challengeToken);
            throw new BadRequestException("OTP has expired. Please request a new OTP.");
        }

        if (challenge.getAttempts() >= MAX_ATTEMPTS) {
            otpStorageService.removeChallenge(challengeToken);
            throw new BadRequestException("Maximum OTP verification attempts exceeded. Please login again.");
        }

        if (!challenge.getOtp().equals(enteredOtp.trim())) {
            int newAttempts = challenge.getAttempts() + 1;
            challenge.setAttempts(newAttempts);

            Duration remainingTtl = Duration.between(Instant.now(), challenge.getExpiresAt());
            if (newAttempts >= MAX_ATTEMPTS) {
                otpStorageService.removeChallenge(challengeToken);
                throw new BadRequestException("Invalid OTP code. Maximum verification attempts reached.");
            } else {
                otpStorageService.updateChallenge(challengeToken, challenge, remainingTtl);
                int remaining = MAX_ATTEMPTS - newAttempts;
                throw new BadRequestException("Invalid OTP code. " + remaining + " attempt(s) remaining.");
            }
        }

        // Successfully verified, consume and remove to prevent replay attacks
        otpStorageService.removeChallenge(challengeToken);
        log.info("Successfully validated 2FA challenge [{}] for owner id [{}]", challengeToken, challenge.getOwnerId());
        return challenge;
    }

    @Override
    public OtpChallengeResponse resendOtp(String challengeToken) {
        OtpChallengeData existing = otpStorageService.getChallenge(challengeToken)
                .orElseThrow(() -> new BadRequestException("Existing challenge has expired. Please log in again."));

        Duration timeSinceCreated = Duration.between(existing.getCreatedAt(), Instant.now());
        if (timeSinceCreated.getSeconds() < 30) {
            long waitSeconds = 30 - timeSinceCreated.getSeconds();
            throw new BadRequestException("Please wait " + waitSeconds + " seconds before requesting another OTP.");
        }

        Owner owner = ownerRepository.findById(existing.getOwnerId())
                .orElseThrow(() -> new BadRequestException("Owner record no longer exists"));

        // Invalidate old challenge
        otpStorageService.removeChallenge(challengeToken);

        // Issue new challenge
        return createAndSendOtpChallenge(owner, existing.getChannel());
    }

    private void dispatchOtp(OtpDeliveryChannel channel, String destination, String recipientName, String otp) {
        if (channel == OtpDeliveryChannel.EMAIL) {
            emailNotificationService.sendOtpEmail(destination, recipientName, otp);
        } else {
            smsNotificationService.sendOtpSms(destination, otp);
        }
    }

    private String generateNumericOtp() {
        int code = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(code);
    }

    private String maskDestination(OtpDeliveryChannel channel, String destination) {
        if (destination == null || destination.isBlank()) {
            return "******";
        }
        if (channel == OtpDeliveryChannel.EMAIL) {
            int atIndex = destination.indexOf('@');
            if (atIndex > 2) {
                return destination.charAt(0) + "***" + destination.substring(atIndex - 1);
            }
            return destination.replaceAll("(?<=.)[^@](?=[^@]*?@)|(?:(?<=@.)|(?!^)\\G(?=[^@]*$)).(?=.*\\.)", "*");
        } else {
            int len = destination.length();
            if (len > 4) {
                return destination.substring(0, Math.min(3, len - 4)) + "*****" + destination.substring(len - 4);
            }
            return "****";
        }
    }
}
