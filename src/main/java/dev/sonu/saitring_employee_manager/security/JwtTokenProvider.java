package dev.sonu.saitring_employee_manager.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    private final SecretKey signingKey;
    private final long expirationSeconds;

    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-seconds:86400}") long expirationSeconds) {
        this.expirationSeconds = expirationSeconds;
        this.signingKey = initKey(secret);
    }

    private SecretKey initKey(String secret) {
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secret);
            if (keyBytes.length < 32) {
                keyBytes = secret.getBytes(StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(OwnerPrincipal principal) {
        Instant now = Instant.now();
        Instant expiryInstant = now.plus(expirationSeconds, ChronoUnit.SECONDS);

        return Jwts.builder()
                .subject(String.valueOf(principal.getId()))
                .claim("email", principal.getEmail())
                .claim("phoneNumber", principal.getPhoneNumber())
                .claim("organizationName", principal.getOrganizationName())
                .claim("authorities", principal.getAuthorities().stream()
                        .map(a -> a.getAuthority())
                        .toList())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiryInstant))
                .signWith(signingKey)
                .compact();
    }

    public Long getOwnerIdFromToken(String token) {
        Claims claims = parseClaims(token);
        return Long.parseLong(claims.getSubject());
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (SignatureException ex) {
            log.warn("Invalid JWT signature: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            log.warn("Invalid JWT format: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            log.warn("Expired JWT token: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.warn("Unsupported JWT token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.warn("JWT claims string is empty: {}", ex.getMessage());
        }
        return false;
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long getExpirationSeconds() {
        return expirationSeconds;
    }
}
