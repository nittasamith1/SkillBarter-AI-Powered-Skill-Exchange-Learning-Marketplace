package com.skillbarter.common.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private static final String SECRET = "test-secret-that-is-at-least-256-bits-long-for-hmac-sha-signing-verification-12345";
    private static final long EXPIRATION_MS = 900000; // 15 mins

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, EXPIRATION_MS);
    }

    @Test
    @DisplayName("Generate access token contains valid subject, email, tenantId, and roles claims")
    void testGenerateAndValidateAccessToken() {
        UUID userId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        String email = "student@university.edu";
        List<String> roles = List.of("STUDENT");

        String token = jwtService.generateAccessToken(userId, email, roles, tenantId);
        assertNotNull(token);
        assertTrue(jwtService.isTokenValid(token));

        assertEquals(userId.toString(), jwtService.extractUserId(token));
        assertEquals(email, jwtService.extractEmail(token));
        assertEquals(tenantId.toString(), jwtService.extractTenantId(token));
        assertEquals(roles, jwtService.extractRoles(token));
    }

    @Test
    @DisplayName("Invalid or tampered token returns false from isTokenValid")
    void testInvalidToken() {
        assertFalse(jwtService.isTokenValid("invalid.token.string"));
    }
}
