package dev.sonu.saitring_employee_manager.auth.service;

import java.time.Duration;
import java.util.Optional;

public interface OtpStorageService {
    void storeChallenge(String challengeToken, OtpChallengeData data, Duration ttl);
    Optional<OtpChallengeData> getChallenge(String challengeToken);
    void updateChallenge(String challengeToken, OtpChallengeData data, Duration remainingTtl);
    void removeChallenge(String challengeToken);
}
