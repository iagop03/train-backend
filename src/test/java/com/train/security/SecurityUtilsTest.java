package com.train.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SecurityUtilsTest {

    private Jwt jwt;
    private JwtAuthenticationToken authenticationToken;

    @BeforeEach
    void setUp() {
        // Create JWT
        Map<String, Object> headers = new HashMap<>();
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "user123");
        claims.put("email", "user@example.com");
        
        jwt = new Jwt("token", Instant.now(), Instant.now().plusSeconds(3600), headers, claims);
        
        // Create authentication
        Collection<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority("ROLE_TRAINER")
        );
        
        authenticationToken = new JwtAuthenticationToken(jwt, authorities, "testuser");
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testGetCurrentUserLogin() {
        assertTrue(SecurityUtils.getCurrentUserLogin().isPresent());
        assertEquals("testuser", SecurityUtils.getCurrentUserLogin().get());
    }

    @Test
    void testGetCurrentUserId() {
        assertTrue(SecurityUtils.getCurrentUserId().isPresent());
        assertEquals("user123", SecurityUtils.getCurrentUserId().get());
    }

    @Test
    void testGetCurrentUserEmail() {
        assertTrue(SecurityUtils.getCurrentUserEmail().isPresent());
        assertEquals("user@example.com", SecurityUtils.getCurrentUserEmail().get());
    }

    @Test
    void testGetJwt() {
        assertTrue(SecurityUtils.getJwt().isPresent());
        assertEquals(jwt.getTokenValue(), SecurityUtils.getJwt().get().getTokenValue());
    }

    @Test
    void testHasRole() {
        assertTrue(SecurityUtils.hasRole("USER"));
        assertTrue(SecurityUtils.hasRole("TRAINER"));
        assertFalse(SecurityUtils.hasRole("ADMIN"));
    }

    @Test
    void testHasAnyRole() {
        assertTrue(SecurityUtils.hasAnyRole("USER", "ADMIN"));
        assertTrue(SecurityUtils.hasAnyRole("TRAINER", "ADMIN"));
        assertFalse(SecurityUtils.hasAnyRole("ADMIN", "MODERATOR"));
    }
}
