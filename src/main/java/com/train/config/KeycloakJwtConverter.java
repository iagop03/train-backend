package com.train.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.*;
import java.util.stream.Collectors;

public class KeycloakJwtConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt source) {
        Collection<SimpleGrantedAuthority> authorities = extractAuthorities(source);
        return new UsernamePasswordAuthenticationToken(source, source, authorities);
    }

    private Collection<SimpleGrantedAuthority> extractAuthorities(Jwt jwt) {
        var realmAccess = jwt.getClaimAsMap("realm_access");
        Collection<String> rolesFromRealmAccess = new ArrayList<>();
        
        if (realmAccess != null) {
            rolesFromRealmAccess = (Collection<String>) realmAccess.get("roles");
        }

        var resourceAccess = jwt.getClaimAsMap("resource_access");
        Collection<String> rolesFromResourceAccess = new ArrayList<>();
        
        if (resourceAccess != null) {
            var resource = (Map<String, Object>) resourceAccess.get("train-api");
            if (resource != null) {
                rolesFromResourceAccess = (Collection<String>) resource.get("roles");
            }
        }

        return Stream.concat(rolesFromRealmAccess.stream(), rolesFromResourceAccess.stream())
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toSet());
    }
}
