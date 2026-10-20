package dev.sonu.saitring_employee_manager.security;

import dev.sonu.saitring_employee_manager.owner.entity.Owner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private static final String SECRET = "dGVzdC1zZWNyZXQta2V5LXRlc3Qtc2VjcmV0LWtleS0xMjM0NTY3ODkwMTI=";

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(SECRET, 3600);
    }

    @Test
    @DisplayName("Generate token and extract owner ID correctly")
    void generateAndValidateToken() {
        Owner owner = new Owner("Ajay", "Ajay Scaffolding", "ajay@scaffolding.com", "+919876543211", "hash", "Mumbai", null);
        owner.setId(42L);

        OwnerPrincipal principal = OwnerPrincipal.create(owner);
        String token = jwtTokenProvider.generateToken(principal);

        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals(42L, jwtTokenProvider.getOwnerIdFromToken(token));
    }

    @Test
    @DisplayName("Validate returns false for invalid token")
    void validateRejectsInvalidToken() {
        assertFalse(jwtTokenProvider.validateToken("invalid.token.structure"));
        assertFalse(jwtTokenProvider.validateToken(""));
        assertFalse(jwtTokenProvider.validateToken(null));
    }
}
