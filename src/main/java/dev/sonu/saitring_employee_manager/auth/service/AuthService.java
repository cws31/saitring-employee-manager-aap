package dev.sonu.saitring_employee_manager.auth.service;

import dev.sonu.saitring_employee_manager.auth.dto.*;
import dev.sonu.saitring_employee_manager.owner.dto.OwnerResponse;
import org.springframework.web.multipart.MultipartFile;

public interface AuthService {
    OwnerResponse register(RegisterRequest request, MultipartFile logoFile);
    OtpChallengeResponse initiateLogin(LoginRequest request);
    AuthResponse verifyOtp(VerifyOtpRequest request);
    OtpChallengeResponse resendOtp(ResendOtpRequest request);
}
