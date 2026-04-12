package com.train.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "app")
@RefreshScope
@Getter
public class EnvironmentConfig {

    @Value("${app.environment:dev}")
    private String environment;

    @Value("${app.api-base-url:http://localhost:8080}")
    private String apiBaseUrl;

    @Value("${app.version:1.0.0}")
    private String version;

    private Cors cors;
    private Jwt jwt;
    private Security security;

    @Getter
    public static class Cors {
        private List<String> allowedOrigins;
        private List<String> allowedMethods;
        private List<String> allowedHeaders;
        private long maxAge;
        private boolean allowCredentials;
    }

    @Getter
    public static class Jwt {
        private boolean validation;
        private int expirationHours;
    }

    @Getter
    public static class Security {
        private boolean requireHttps;
    }

    public boolean isDev() {
        return "dev".equalsIgnoreCase(environment);
    }

    public boolean isStaging() {
        return "staging".equalsIgnoreCase(environment);
    }

    public boolean isProduction() {
        return "production".equalsIgnoreCase(environment) || "prod".equalsIgnoreCase(environment);
    }
}
