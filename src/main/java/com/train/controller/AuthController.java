package com.train.controller;

import com.train.dto.LoginResponse;
import com.train.service.KeycloakIdentityProviderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
crossorigin(origins = "${CORS_ORIGINS:http://localhost:4200,http://localhost}")
public class AuthController {

    private final KeycloakIdentityProviderService identityProviderService;

    /**
     * Obtiene información del usuario autenticado
     */
    @GetMapping("/me")
    public ResponseEntity<LoginResponse> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        LoginResponse response = LoginResponse.builder()
                .userId(jwt.getClaimAsString("sub"))
                .email(jwt.getClaimAsString("email"))
                .name(jwt.getClaimAsString("name"))
                .givenName(jwt.getClaimAsString("given_name"))
                .familyName(jwt.getClaimAsString("family_name"))
                .picture(jwt.getClaimAsString("picture"))
                .token(jwt.getTokenValue())
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para iniciar sesión con Google
     * Redirige a Keycloak
     */
    @GetMapping("/login/google")
    public ResponseEntity<String> loginWithGoogle() {
        String redirectUrl = buildKeycloakRedirectUrl("google");
        return ResponseEntity.ok(redirectUrl);
    }

    /**
     * Endpoint para iniciar sesión con Apple
     * Redirige a Keycloak
     */
    @GetMapping("/login/apple")
    public ResponseEntity<String> loginWithApple() {
        String redirectUrl = buildKeycloakRedirectUrl("apple");
        return ResponseEntity.ok(redirectUrl);
    }

    /**
     * Construye la URL de redirección a Keycloak
     */
    private String buildKeycloakRedirectUrl(String idpAlias) {
        return String.format("%s/protocol/openid-connect/auth?client_id=%s&response_type=code&scope=openid%%20profile%%20email&redirect_uri=%s&kc_idp_hint=%s",
                System.getenv("KEYCLOAK_AUTH_SERVER_URL"),
                System.getenv("KEYCLOAK_CLIENT_ID"),
                System.getenv("KEYCLOAK_REDIRECT_URI"),
                idpAlias);
    }

    /**
     * Callback de autenticación (manejado por Keycloak)
     */
    @PostMapping("/callback")
    public ResponseEntity<LoginResponse> handleAuthCallback(
            @RequestParam String code,
            @RequestParam String state) {
        log.info("Auth callback recibido - code: {}, state: {}", code, state);
        // El intercambio de código por token se maneja en el cliente
        return ResponseEntity.ok(LoginResponse.builder().build());
    }
}
