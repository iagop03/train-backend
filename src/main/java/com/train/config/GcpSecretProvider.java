package com.train.config;

import com.google.cloud.secretmanager.v1.AccessSecretVersionRequest;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class GcpSecretProvider {

    private final SecretManagerServiceClient secretManagerServiceClient;
    private final EnvironmentConfig environmentConfig;

    @Value("${spring.cloud.gcp.project-id}")
    private String projectId;

    /**
     * Retrieves a secret from GCP Secret Manager
     * @param secretId The ID of the secret
     * @return The secret value
     */
    public String getSecret(String secretId) {
        return getSecret(secretId, "latest");
    }

    /**
     * Retrieves a specific version of a secret from GCP Secret Manager
     * @param secretId The ID of the secret
     * @param versionId The version ID (e.g., "latest", "1", "2")
     * @return The secret value
     */
    public String getSecret(String secretId, String versionId) {
        try {
            log.debug("Fetching secret: {} version: {} from project: {}", secretId, versionId, projectId);

            SecretVersionName secretVersionName = SecretVersionName.of(
                    projectId,
                    secretId,
                    versionId
            );

            AccessSecretVersionRequest request = AccessSecretVersionRequest.newBuilder()
                    .setName(secretVersionName.toString())
                    .build();

            var response = secretManagerServiceClient.accessSecretVersion(request);
            String secretValue = response.getPayload().getData().toStringUtf8();

            log.info("Successfully retrieved secret: {}", secretId);
            return secretValue;

        } catch (Exception e) {
            log.error("Error retrieving secret {} from GCP Secret Manager", secretId, e);
            throw new RuntimeException("Failed to retrieve secret: " + secretId, e);
        }
    }

    /**
     * Retrieves a secret with fallback value if not found
     * @param secretId The ID of the secret
     * @param defaultValue The default value if secret is not found
     * @return The secret value or default value
     */
    public String getSecretWithDefault(String secretId, String defaultValue) {
        try {
            return getSecret(secretId);
        } catch (Exception e) {
            log.warn("Could not retrieve secret {}, using default value. Error: {}", secretId, e.getMessage());
            return defaultValue;
        }
    }
}
