package com.train.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.flyway.FlywayProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Flyway database migration configuration.
 * Manages versioned SQL migrations for PostgreSQL Cloud SQL.
 */
@Configuration
@EnableConfigurationProperties(FlywayProperties.class)
public class FlywayConfig {

    @Value("${spring.flyway.enabled:true}")
    private boolean flywayEnabled;

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${spring.datasource.username}")
    private String datasourceUsername;

    @Value("${spring.datasource.password}")
    private String datasourcePassword;

    /**
     * Configure Flyway bean for automatic migration execution.
     * Flyway will automatically run on application startup.
     */
    @Bean(initMethod = "migrate")
    public Flyway flyway() {
        if (!flywayEnabled) {
            return null;
        }

        return Flyway.configure()
                .dataSource(datasourceUrl, datasourceUsername, datasourcePassword)
                .locations("classpath:db/migration")
                .baselineOnMigrate(false)
                .validateOnMigrate(true)
                .cleanDisabled(true)
                .encoding("UTF-8")
                .placeholders(
                        "app_user", datasourceUsername,
                        "app_schema", "public"
                )
                .load();
    }
}
