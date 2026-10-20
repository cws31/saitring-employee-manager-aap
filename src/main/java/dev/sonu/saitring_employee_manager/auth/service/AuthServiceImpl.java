package dev.sonu.saitring_employee_manager.auth.service;

import dev.sonu.saitring_employee_manager.auth.dto.*;
import dev.sonu.saitring_employee_manager.common.exception.ConflictException;
import dev.sonu.saitring_employee_manager.common.exception.UnauthorizedException;
import dev.sonu.saitring_employee_manager.common.storage.FileStorageService;
import dev.sonu.saitring_employee_manager.owner.dto.OwnerResponse;
import dev.sonu.saitring_employee_manager.owner.entity.Owner;
import dev.sonu.saitring_employee_manager.owner.entity.OwnerStatus;
import dev.sonu.saitring_employee_manager.owner.repository.OwnerRepository;
import dev.sonu.saitring_employee_manager.security.JwtTokenProvider;
import dev.sonu.saitring_employee_manager.security.OwnerPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final OwnerRepository ownerRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final FileStorageService fileStorageService;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthServiceImpl(
            OwnerRepository ownerRepository,
            PasswordEncoder passwordEncoder,
            OtpService otpService,
            FileStorageService fileStorageService,
            JwtTokenProvider jwtTokenProvider) {
        this.ownerRepository = ownerRepository;
        this.passwordEncoder = passwordEncoder;
        this.otpService = otpService;
        this.fileStorageService = fileStorageService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    @Transactional
    public OwnerResponse register(RegisterRequest request, MultipartFile logoFile) {
        String cleanEmail = request.getEmail().trim().toLowerCase();
        String cleanMobile = request.getMobileNumber().trim();

        if (ownerRepository.existsByEmail(cleanEmail)) {
            throw new ConflictException("Email is already registered: " + cleanEmail);
        }

        if (ownerRepository.existsByPhoneNumber(cleanMobile)) {
            throw new ConflictException("Mobile number is already registered: " + cleanMobile);
        }

        String logoUrl = null;
        if (logoFile != null && !logoFile.isEmpty()) {
            logoUrl = fileStorageService.uploadLogo(logoFile, null);
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        Owner owner = new Owner(
                request.getOwnerName().trim(),
                request.getOrganizationName().trim(),
                cleanEmail,
                cleanMobile,
                encodedPassword,
                request.getAddress().trim(),
                logoUrl
        );

        Owner savedOwner = ownerRepository.save(owner);
        log.info("Successfully registered new owner tenant id [{}] with org [{}]", savedOwner.getId(), savedOwner.getOrganizationName());

        return OwnerResponse.fromEntity(savedOwner);
    }

    @Override
    @Transactional(readOnly = true)
    public OtpChallengeResponse initiateLogin(LoginRequest request) {
        String effectiveIdentifier = request.getEffectiveIdentifier();

        Owner owner = ownerRepository.findByIdentifier(effectiveIdentifier)
                .orElseThrow(() -> new UnauthorizedException("Invalid email/phone number or password"));

        if (!passwordEncoder.matches(request.getPassword(), owner.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email/phone number or password");
        }

        if (owner.getStatus() != OwnerStatus.ACTIVE) {
            throw new UnauthorizedException("Your account is currently " + owner.getStatus() + ". Please contact support.");
        }

        // Dynamic 2FA Channel determination:
        // If owner logged in with email -> channel is EMAIL (sent via Brevo)
        // If owner logged in with mobile number -> channel is SMS (sent via Twilio)
        OtpDeliveryChannel channel;
        if (request.isEmailFormat() || effectiveIdentifier.equalsIgnoreCase(owner.getEmail())) {
            channel = OtpDeliveryChannel.EMAIL;
        } else {
            channel = OtpDeliveryChannel.SMS;
        }

        return otpService.createAndSendOtpChallenge(owner, channel);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse verifyOtp(VerifyOtpRequest request) {
        OtpChallengeData challenge = otpService.validateOtp(request.getChallengeToken(), request.getOtp());

        Owner owner = ownerRepository.findById(challenge.getOwnerId())
                .orElseThrow(() -> new UnauthorizedException("Owner record no longer exists"));

        if (owner.getStatus() != OwnerStatus.ACTIVE) {
            throw new UnauthorizedException("Account is not active");
        }

        OwnerPrincipal principal = OwnerPrincipal.create(owner);
        String token = jwtTokenProvider.generateToken(principal);

        return new AuthResponse(
                token,
                "Bearer",
                jwtTokenProvider.getExpirationSeconds(),
                OwnerResponse.fromEntity(owner)
        );
    }

    @Override
    public OtpChallengeResponse resendOtp(ResendOtpRequest request) {
        return otpService.resendOtp(request.getChallengeToken());
    }
}
