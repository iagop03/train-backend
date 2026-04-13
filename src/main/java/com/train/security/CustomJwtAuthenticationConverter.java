package com.train.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class CustomJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        log.debug("Converting JWT token: {}", jwt.getTokenValue().substring(0, 20) + "...");
        
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        
        // Extract authorities from 'scope' claim
        authorities.addAll(jwtGrantedAuthoritiesConverter.convert(jwt));
        
        // Extract roles from realm_access.roles
        authorities.addAll(extractRealmRoles(jwt));
        
        // Extract client roles
        authorities.addAll(extractClientRoles(jwt));
        
        log.debug("Extracted authorities: {}", authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));
        
        String principalName = jwt.getClaimAsString("preferred_username");
        if (principalName == null) {
            principalName = jwt.getClaimAsString("sub");
        }
        
        return new JwtAuthenticationToken(jwt, authorities, principalName);
    }

    private Collection<GrantedAuthority> extractRealmRoles(Jwt jwt) {
        try {
            Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
            if (realmAccess == null) {
                return Collections.emptyList();
            }
            
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) realmAccess.get("roles");
            if (roles == null) {
                return Collections.emptyList();
            }
            
            return roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Error extracting realm roles: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private Collection<GrantedAuthority> extractClientRoles(Jwt jwt) {
        try {
            Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
            if (resourceAccess == null) {
                return Collections.emptyList();
            }
            
            @SuppressWarnings("unchecked")
            Map<String, Object> clientRoles = (Map<String, Object>) resourceAccess.get("train-backend");
            if (clientRoles == null) {
                return Collections.emptyList();
            }
            
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) clientRoles.get("roles");
            if (roles == null) {
                return Collections.emptyList();
            }
            
            return roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Error extracting client roles: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
