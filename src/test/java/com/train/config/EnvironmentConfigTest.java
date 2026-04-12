package com.train.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class EnvironmentConfigTest {

    @Autowired
    private EnvironmentConfig environmentConfig;

    @Test
    void testEnvironmentConfigLoaded() {
        assertThat(environmentConfig).isNotNull();
        assertThat(environmentConfig.getEnvironment()).isEqualTo("test");
    }

    @Test
    void testIsDev() {
        assertThat(environmentConfig.isDev()).isFalse();
    }

    @Test
    void testApiBaseUrl() {
        assertThat(environmentConfig.getApiBaseUrl()).isNotNull();
    }

    @Test
    void testVersion() {
        assertThat(environmentConfig.getVersion()).isNotNull();
    }
}
