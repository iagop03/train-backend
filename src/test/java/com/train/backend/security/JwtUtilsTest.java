package com.train.backend.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JwtUtils utility class.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("JWT Utils Tests")
public class JwtUtilsTest {

    private Jwt mockJwt;
    private JwtAuthenticationToken authenticationToken;

    @BeforeEach
    void setUp() {
        Map<String, Object> headers = new HashMap<>();
        headers.put("alg", "RS256");
        headers.put("typ", "JWT");

        Map<String, Object> claims = new HashMap<>();
        UUID testUuid = UUID.randomUUID();
        claims.put("sub", testUuid.toString());
        claims.put("preferred_username", "testuser");
        claims.put("email", "testuser@example.com");
        claims.put("realm_access", Map.of("roles", Set.of("user", "trainer")));

        mockJwt = new Jwt(
            "token-value",
            Instant.now(),
            Instant.now().plusSeconds(3600),
            headers,
            claims
        );

        authenticationToken = new JwtAuthenticationToken(mockJwt);
    }

    @Test
    @DisplayName("Should extract user UUID from JWT subject claim")
    void testExtractUserIdFromJwt() {
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        UUID userId = JwtUtils.extractUserIdFromJwt();

        assertNotNull(userId);
        assertEquals(mockJwt.getSubject(), userId.toString());
    }

    @Test
    @DisplayName("Should throw exception when extracting user ID without JWT")
    void testExtractUserIdWithoutJwt() {
        SecurityContextHolder.clearContext();

        assertThrows(IllegalStateException.class, JwtUtils::extractUserIdFromJwt);
    }

    @Test
    @DisplayName("Should extract username from JWT preferred_username claim")
    void testExtractUsernameFromJwt() {
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        String username = JwtUtils.extractUsernameFromJwt();

        assertEquals("testuser", username);
    }

    @Test
    @DisplayName("Should extract email from JWT email claim")
    void testExtractEmailFromJwt() {
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        String email = JwtUtils.extractEmailFromJwt();

        assertEquals("testuser@example.com", email);
    }

    @Test
    @DisplayName("Should check if user is authenticated")
    void testIsJwtPresent() {
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        assertTrue(JwtUtils.isJwtPresent());
    }

    @Test
    @DisplayName("Should return false when no JWT present")
    void testIsJwtPresentWhenEmpty() {
        SecurityContextHolder.clearContext();

        assertFalse(JwtUtils.isJwtPresent());
    }

    @Test
    @DisplayName("Should retrieve token string value")
    void testGetTokenString() {
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        String token = JwtUtils.getTokenString();

        assertEquals("token-value", token);
    }

    @Test
    @DisplayName("Should extract custom claim from JWT")
    void testExtractClaimFromJwt() {
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        Object emailClaim = JwtUtils.extractClaimFromJwt("email");

        assertEquals("testuser@example.com", emailClaim);
    }

    @Test
    @DisplayName("Should return null for non-existent claim")
    void testExtractNonExistentClaim() {
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        Object claim = JwtUtils.extractClaimFromJwt("non-existent");

        assertNull(claim);
    }

    @Test
    @DisplayName("Should return empty set when no JWT present")
    void testExtractRolesWhenNoJwt() {
        SecurityContextHolder.clearContext();

        Set<String> roles = JwtUtils.extractRolesFromJwt();

        assertTrue(roles.isEmpty());
    }

    @Test
    @DisplayName("Should return false for email when no JWT present")
    void testExtractEmailWithoutJwt() {
        SecurityContextHolder.clearContext();

        String email = JwtUtils.extractEmailFromJwt();

        assertNull(email);
    }

    @Test
    @DisplayName("Should throw exception for invalid UUID in subject claim")
    void testExtractUserIdWithInvalidUuid() {
        Map<String, Object> headers = new HashMap<>();
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "invalid-uuid");

        Jwt invalidJwt = new Jwt(
            "token-value",
            Instant.now(),
            Instant.now().plusSeconds(3600),
            headers,
            claims
        );

        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(invalidJwt));

        assertThrows(IllegalStateException.class, JwtUtils::extractUserIdFromJwt);
    }
}
