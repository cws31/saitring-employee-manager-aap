package dev.sonu.saitring_employee_manager.auth.service;

import dev.sonu.saitring_employee_manager.auth.dto.OtpChallengeResponse;
import dev.sonu.saitring_employee_manager.auth.dto.OtpDeliveryChannel;
import dev.sonu.saitring_employee_manager.owner.entity.Owner;

public interface OtpService {
    OtpChallengeResponse createAndSendOtpChallenge(Owner owner, OtpDeliveryChannel channel);
    OtpChallengeData validateOtp(String challengeToken, String enteredOtp);
    OtpChallengeResponse resendOtp(String challengeToken);
}
