package com.train.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Configuración de Testcontainers para tests de integración.
 * Proporciona contenedores de PostgreSQL y MongoDB reutilizables.
 */
@TestConfiguration
public class TestcontainersConfiguration {

    @Bean
    public PostgreSQLContainer<?> postgresqlContainer() {
        PostgreSQLContainer<?> container = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:15-alpine")
        )
            .withDatabaseName("train_test")
            .withUsername("trainuser")
            .withPassword("trainpass")
            .withReuse(true);
        
        container.start();
        return container;
    }

    @Bean
    public MongoDBContainer mongodbContainer() {
        MongoDBContainer container = new MongoDBContainer(
            DockerImageName.parse("mongo:7.0")
        )
            .withReuse(true);
        
        container.start();
        return container;
    }
}