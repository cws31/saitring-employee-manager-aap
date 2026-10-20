package dev.sonu.saitring_employee_manager.auth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RedisOtpStorageService implements OtpStorageService {

    private static final Logger log = LoggerFactory.getLogger(RedisOtpStorageService.class);
    private static final String KEY_PREFIX = "otp:challenge:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final Map<String, OtpChallengeData> inMemoryFallback = new ConcurrentHashMap<>();

    @Autowired
    public RedisOtpStorageService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void storeChallenge(String challengeToken, OtpChallengeData data, Duration ttl) {
        String key = KEY_PREFIX + challengeToken;
        try {
            String json = objectMapper.writeValueAsString(data);
            redisTemplate.opsForValue().set(key, json, ttl);
        } catch (Exception ex) {
            log.warn("Redis storage error, using in-memory fallback for challenge [{}]: {}", challengeToken, ex.getMessage());
            inMemoryFallback.put(challengeToken, data);
        }
    }

    @Override
    public Optional<OtpChallengeData> getChallenge(String challengeToken) {
        String key = KEY_PREFIX + challengeToken;
        try {
            String json = redisTemplate.opsForValue().get(key);
            if (json != null && !json.isBlank()) {
                OtpChallengeData data = objectMapper.readValue(json, OtpChallengeData.class);
                return Optional.of(data);
            }
        } catch (Exception ex) {
            log.warn("Redis retrieval error for challenge [{}], checking in-memory fallback: {}", challengeToken, ex.getMessage());
        }

        OtpChallengeData fallbackData = inMemoryFallback.get(challengeToken);
        if (fallbackData != null) {
            if (fallbackData.getExpiresAt().isBefore(Instant.now())) {
                inMemoryFallback.remove(challengeToken);
                return Optional.empty();
            }
            return Optional.of(fallbackData);
        }

        return Optional.empty();
    }

    @Override
    public void updateChallenge(String challengeToken, OtpChallengeData data, Duration remainingTtl) {
        storeChallenge(challengeToken, data, remainingTtl.isNegative() || remainingTtl.isZero() ? Duration.ofSeconds(1) : remainingTtl);
    }

    @Override
    public void removeChallenge(String challengeToken) {
        String key = KEY_PREFIX + challengeToken;
        try {
            redisTemplate.delete(key);
        } catch (Exception ex) {
            log.warn("Redis deletion error for key [{}]: {}", key, ex.getMessage());
        }
        inMemoryFallback.remove(challengeToken);
    }
}
