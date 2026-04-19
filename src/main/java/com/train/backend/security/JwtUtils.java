package com.train.backend.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Utility class for extracting JWT claims and user information.
 * Methods are static for easy access in controllers and services.
 */
@Component
public class JwtUtils {

    /**
     * Extract the user UUID (subject claim) from the current JWT.
     * @return UUID of the authenticated user
     * @throws IllegalStateException if no valid JWT is present
     */
    public static UUID extractUserIdFromJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            String subject = jwt.getSubject();
            try {
                return UUID.fromString(subject);
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException("Invalid user ID format in JWT: " + subject, e);
            }
        }
        throw new IllegalStateException("No valid JWT found in security context");
    }

    /**
     * Extract all roles from the current JWT.
     * Supports both realm roles and client roles.
     * @return Set of role names without ROLE_ prefix
     */
    public static Set<String> extractRolesFromJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .map(auth -> auth.startsWith("ROLE_") ? auth.substring(5) : auth)
                .collect(Collectors.toSet());
        }
        return Set.of();
    }

    /**
     * Extract a specific claim from the JWT.
     * @param claimName the name of the claim
     * @return the claim value as Object, or null if not found
     */
    public static Object extractClaimFromJwt(String claimName) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getClaim(claimName);
        }
        return null;
    }

    /**
     * Get the preferred username from JWT.
     * @return username claim value
     */
    public static String extractUsernameFromJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getClaimAsString("preferred_username");
        }
        throw new IllegalStateException("No valid JWT found in security context");
    }

    /**
     * Get email from JWT.
     * @return email claim value
     */
    public static String extractEmailFromJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getClaimAsString("email");
        }
        return null;
    }

    /**
     * Check if user has a specific role.
     * @param role role name without ROLE_ prefix
     * @return true if user has the role
     */
    public static boolean hasRole(String role) {
        return extractRolesFromJwt().contains(role);
    }

    /**
     * Get the raw JWT token string.
     * @return the encoded JWT token
     */
    public static String getTokenString() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getTokenValue();
        }
        return null;
    }

    /**
     * Check if a valid JWT is present in the security context.
     * @return true if a valid JWT exists
     */
    public static boolean isJwtPresent() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getPrincipal() instanceof Jwt;
    }
}
