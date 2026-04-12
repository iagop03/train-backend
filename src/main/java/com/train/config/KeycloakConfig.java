package com.train.config;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class KeycloakConfig {

    /**
     * Resolve Keycloak configuration from Spring Boot application properties
     * instead of keycloak.json
     */
    @Bean
    public KeycloakSpringBootConfigResolver keycloakConfigResolver() {
        log.info("Configuring Keycloak with Spring Boot configuration resolver");
        return new KeycloakSpringBootConfigResolver();
    }
}
