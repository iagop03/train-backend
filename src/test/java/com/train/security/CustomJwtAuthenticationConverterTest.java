package com.train.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CustomJwtAuthenticationConverterTest {

    private CustomJwtAuthenticationConverter converter;

    @BeforeEach
    void setUp() {
        converter = new CustomJwtAuthenticationConverter();
    }

    @Test
    void testConvertJwtWithRealmRoles() {
        // Arrange
        Map<String, Object> headers = new HashMap<>();
        headers.put("alg", "RS256");
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "user123");
        claims.put("preferred_username", "testuser");
        claims.put("scope", "openid profile email");
        
        Map<String, Object> realmAccess = new HashMap<>();
        realmAccess.put("roles", List.of("user", "trainer"));
        claims.put("realm_access", realmAccess);
        
        Jwt jwt = new Jwt("token", Instant.now(), Instant.now().plusSeconds(3600), headers, claims);

        // Act
        AbstractAuthenticationToken token = converter.convert(jwt);

        // Assert
        assertNotNull(token);
        assertEquals("testuser", token.getName());
        
        Collection<? extends GrantedAuthority> authorities = token.getAuthorities();
        assertTrue(authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
        assertTrue(authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_TRAINER")));
    }

    @Test
    void testConvertJwtWithClientRoles() {
        // Arrange
        Map<String, Object> headers = new HashMap<>();
        headers.put("alg", "RS256");
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "user456");
        claims.put("preferred_username", "adminuser");
        
        Map<String, Object> clientRoles = new HashMap<>();
        clientRoles.put("roles", List.of("admin"));
        
        Map<String, Object> resourceAccess = new HashMap<>();
        resourceAccess.put("train-backend", clientRoles);
        claims.put("resource_access", resourceAccess);
        
        Jwt jwt = new Jwt("token", Instant.now(), Instant.now().plusSeconds(3600), headers, claims);

        // Act
        AbstractAuthenticationToken token = converter.convert(jwt);

        // Assert
        assertNotNull(token);
        assertEquals("adminuser", token.getName());
        
        Collection<? extends GrantedAuthority> authorities = token.getAuthorities();
        assertTrue(authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void testConvertJwtFallbackToSub() {
        // Arrange
        Map<String, Object> headers = new HashMap<>();
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "user789");
        
        Jwt jwt = new Jwt("token", Instant.now(), Instant.now().plusSeconds(3600), headers, claims);

        // Act
        AbstractAuthenticationToken token = converter.convert(jwt);

        // Assert
        assertNotNull(token);
        assertEquals("user789", token.getName());
    }
}
