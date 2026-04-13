package com.train.initializer;

import com.train.service.KeycloakIdentityProviderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KeycloakInitializer implements ApplicationRunner {

    private final KeycloakIdentityProviderService identityProviderService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Iniciando configuración de Identity Providers de Keycloak");
        
        try {
            // Configurar Google
            identityProviderService.configureGoogleIdentityProvider();
            identityProviderService.configureGoogleAttributeMappers();
            
            // Configurar Apple
            identityProviderService.configureAppleIdentityProvider();
            identityProviderService.configureAppleAttributeMappers();
            
            log.info("Identity Providers configurados exitosamente");
        } catch (Exception e) {
            log.error("Error durante la inicialización de Identity Providers", e);
        }
    }
}
