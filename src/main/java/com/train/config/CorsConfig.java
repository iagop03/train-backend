package com.train.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class CorsConfig {

    private final EnvironmentConfig environmentConfig;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Set allowed origins from environment config
        if (environmentConfig.getCors() != null && environmentConfig.getCors().getAllowedOrigins() != null) {
            configuration.setAllowedOrigins(environmentConfig.getCors().getAllowedOrigins());
            log.info("CORS allowed origins: {}", environmentConfig.getCors().getAllowedOrigins());
        } else {
            // Fallback based on environment
            if (environmentConfig.isDev()) {
                configuration.setAllowedOrigins(Arrays.asList(
                        "http://localhost:4200",
                        "http://localhost:3000",
                        "http://127.0.0.1:4200",
                        "http://127.0.0.1:3000"
                ));
            } else if (environmentConfig.isStaging()) {
                configuration.setAllowedOrigins(Arrays.asList(
                        "https://staging.train-ai-gym.com",
                        "https://app-staging.train-ai-gym.com"
                ));
            } else if (environmentConfig.isProduction()) {
                configuration.setAllowedOrigins(Arrays.asList(
                        "https://train-ai-gym.com",
                        "https://app.train-ai-gym.com"
                ));
            }
        }

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
