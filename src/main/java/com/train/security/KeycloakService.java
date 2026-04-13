package com.train.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakService {

    private final Keycloak keycloak;

    @Value("${keycloak.realm}")
    private String realm;

    public Optional<UserRepresentation> getUserByUsername(String username) {
        try {
            RealmResource realmResource = keycloak.realm(realm);
            UsersResource usersResource = realmResource.users();
            
            List<UserRepresentation> users = usersResource.search(username, true);
            if (users.isEmpty()) {
                log.warn("User not found: {}", username);
                return Optional.empty();
            }
            
            return Optional.of(users.get(0));
        } catch (Exception e) {
            log.error("Error fetching user: {}", username, e);
            return Optional.empty();
        }
    }

    public Optional<UserRepresentation> getUserByEmail(String email) {
        try {
            RealmResource realmResource = keycloak.realm(realm);
            UsersResource usersResource = realmResource.users();
            
            List<UserRepresentation> users = usersResource.search(null, null, null, email, 0, 1);
            if (users.isEmpty()) {
                log.warn("User not found with email: {}", email);
                return Optional.empty();
            }
            
            return Optional.of(users.get(0));
        } catch (Exception e) {
            log.error("Error fetching user by email: {}", email, e);
            return Optional.empty();
        }
    }

    public List<String> getUserRoles(String userId) {
        try {
            RealmResource realmResource = keycloak.realm(realm);
            UsersResource usersResource = realmResource.users();
            
            return usersResource.get(userId)
                    .roles()
                    .realmLevel()
                    .listAll()
                    .stream()
                    .map(RoleRepresentation::getName)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching user roles for userId: {}", userId, e);
            return Collections.emptyList();
        }
    }

    public boolean hasRole(String userId, String role) {
        return getUserRoles(userId).contains(role);
    }

    public void assignRoleToUser(String userId, String roleName) {
        try {
            RealmResource realmResource = keycloak.realm(realm);
            RoleRepresentation role = realmResource.roles().get(roleName).toRepresentation();
            realmResource.users().get(userId).roles().realmLevel().add(Collections.singletonList(role));
            log.info("Role {} assigned to user {}", roleName, userId);
        } catch (Exception e) {
            log.error("Error assigning role {} to user {}", roleName, userId, e);
        }
    }

    public void removeRoleFromUser(String userId, String roleName) {
        try {
            RealmResource realmResource = keycloak.realm(realm);
            RoleRepresentation role = realmResource.roles().get(roleName).toRepresentation();
            realmResource.users().get(userId).roles().realmLevel().remove(Collections.singletonList(role));
            log.info("Role {} removed from user {}", roleName, userId);
        } catch (Exception e) {
            log.error("Error removing role {} from user {}", roleName, userId, e);
        }
    }
}
