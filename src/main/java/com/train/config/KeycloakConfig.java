package com.train.config;

import lombok.RequiredArgsConstructor;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
@EnableConfigurationProperties
public class KeycloakConfig {

    @Bean
    public Keycloak keycloak(KeycloakProperties properties) {
        return KeycloakBuilder.builder()
                .serverUrl(properties.authServerUrl)
                .realm(properties.realm)
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .clientId(properties.clientId)
                .clientSecret(properties.clientSecret)
                .resteasyClient(
                        new org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient43Engine()
                )
                .build();
    }

    @Component
    @ConfigurationProperties(prefix = "keycloak")
    public static class KeycloakProperties {
        public String realm;
        public String authServerUrl;
        public String clientId;
        public String clientSecret;

        // Getters and Setters
        public String getRealm() { return realm; }
        public void setRealm(String realm) { this.realm = realm; }

        public String getAuthServerUrl() { return authServerUrl; }
        public void setAuthServerUrl(String authServerUrl) { this.authServerUrl = authServerUrl; }

        public String getClientId() { return clientId; }
        public void setClientId(String clientId) { this.clientId = clientId; }

        public String getClientSecret() { return clientSecret; }
        public void setClientSecret(String clientSecret) { this.clientSecret = clientSecret; }
    }
}
