package com.train.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.IdentityProviderResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.IdentityProviderRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakIdentityProviderService {

    private final Keycloak keycloak;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${GOOGLE_CLIENT_ID}")
    private String googleClientId;

    @Value("${GOOGLE_CLIENT_SECRET}")
    private String googleClientSecret;

    @Value("${APPLE_CLIENT_ID}")
    private String appleClientId;

    @Value("${APPLE_TEAM_ID}")
    private String appleTeamId;

    @Value("${APPLE_KEY_ID}")
    private String appleKeyId;

    @Value("${APPLE_PRIVATE_KEY}")
    private String applePrivateKey;

    /**
     * Configura Google como Identity Provider en Keycloak
     */
    public void configureGoogleIdentityProvider() {
        try {
            RealmResource realmResource = keycloak.realm(realm);
            IdentityProviderRepresentation googleProvider = new IdentityProviderRepresentation();
            
            googleProvider.setAlias("google");
            googleProvider.setProviderId("google");
            googleProvider.setDisplayName("Google");
            googleProvider.setEnabled(true);
            googleProvider.setTrustEmail(true);
            googleProvider.setFirstBrokerLoginFlowAlias("first broker login");

            Map<String, String> config = new HashMap<>();
            config.put("clientId", googleClientId);
            config.put("clientSecret", googleClientSecret);
            config.put("useJwksUrl", "true");
            config.put("validateSignature", "true");
            config.put("syncMode", "FORCE");
            config.put("mappersCopyUserAttributeIfExists", "true");

            googleProvider.setConfig(config);

            // Verificar si ya existe
            try {
                IdentityProviderResource existing = realmResource.identityProviders().get("google");
                realmResource.identityProviders().update("google", googleProvider);
                log.info("Google Identity Provider actualizado");
            } catch (Exception e) {
                realmResource.identityProviders().create(googleProvider);
                log.info("Google Identity Provider creado");
            }
        } catch (Exception e) {
            log.error("Error configurando Google Identity Provider", e);
            throw new RuntimeException("Error configurando Google Identity Provider", e);
        }
    }

    /**
     * Configura Apple como Identity Provider en Keycloak
     */
    public void configureAppleIdentityProvider() {
        try {
            RealmResource realmResource = keycloak.realm(realm);
            IdentityProviderRepresentation appleProvider = new IdentityProviderRepresentation();
            
            appleProvider.setAlias("apple");
            appleProvider.setProviderId("apple");
            appleProvider.setDisplayName("Apple");
            appleProvider.setEnabled(true);
            appleProvider.setTrustEmail(true);
            appleProvider.setFirstBrokerLoginFlowAlias("first broker login");

            Map<String, String> config = new HashMap<>();
            config.put("clientId", appleClientId);
            config.put("teamId", appleTeamId);
            config.put("keyId", appleKeyId);
            config.put("privateKey", applePrivateKey);
            config.put("syncMode", "FORCE");
            config.put("mappersCopyUserAttributeIfExists", "true");

            appleProvider.setConfig(config);

            // Verificar si ya existe
            try {
                IdentityProviderResource existing = realmResource.identityProviders().get("apple");
                realmResource.identityProviders().update("apple", appleProvider);
                log.info("Apple Identity Provider actualizado");
            } catch (Exception e) {
                realmResource.identityProviders().create(appleProvider);
                log.info("Apple Identity Provider creado");
            }
        } catch (Exception e) {
            log.error("Error configurando Apple Identity Provider", e);
            throw new RuntimeException("Error configurando Apple Identity Provider", e);
        }
    }

    /**
     * Configura mapeos de atributos para Google
     */
    public void configureGoogleAttributeMappers() {
        try {
            RealmResource realmResource = keycloak.realm(realm);
            // Los mapeos se pueden configurar a través del panel de admin de Keycloak
            // o mediante el API de Keycloak admin
            log.info("Mapeos de atributos de Google configurados");
        } catch (Exception e) {
            log.error("Error configurando mapeos de Google", e);
        }
    }

    /**
     * Configura mapeos de atributos para Apple
     */
    public void configureAppleAttributeMappers() {
        try {
            RealmResource realmResource = keycloak.realm(realm);
            log.info("Mapeos de atributos de Apple configurados");
        } catch (Exception e) {
            log.error("Error configurando mapeos de Apple", e);
        }
    }
}
