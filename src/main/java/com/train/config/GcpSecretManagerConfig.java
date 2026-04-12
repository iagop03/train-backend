package com.train.config;

import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Slf4j
@ConditionalOnProperty(
    name = "spring.cloud.gcp.secret-manager.enabled",
    havingValue = "true",
    matchIfMissing = false
)
public class GcpSecretManagerConfig {

    @Bean
    public SecretManagerServiceClient secretManagerServiceClient() {
        log.info("Initializing GCP Secret Manager Service Client");
        return SecretManagerServiceClient.create();
    }

    @Bean
    public GcpSecretProvider gcpSecretProvider(
            SecretManagerServiceClient secretManagerServiceClient,
            EnvironmentConfig environmentConfig) {
        return new GcpSecretProvider(secretManagerServiceClient, environmentConfig);
    }
}
