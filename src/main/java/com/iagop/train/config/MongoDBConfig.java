package com.iagop.train.config;

import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.connection.ConnectionPoolSettings;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractReactiveMongoDbConfiguration;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

import java.util.concurrent.TimeUnit;

/**
 * MongoDB Atlas Configuration with VPC Peering Support
 * 
 * Configures connection to MongoDB Atlas M0 cluster through VPC peering
 * with appropriate connection pooling and security settings.
 */
@Slf4j
@Configuration
@EnableReactiveMongoRepositories(basePackages = "com.iagop.train.repository")
public class MongoDBConfig extends AbstractReactiveMongoDbConfiguration {

    @Value("${spring.data.mongodb.uri}")
    private String mongoDbUri;

    @Value("${spring.data.mongodb.database}")
    private String database;

    @Value("${mongodb.connection.pool.min-size:10}")
    private int minPoolSize;

    @Value("${mongodb.connection.pool.max-size:100}")
    private int maxPoolSize;

    @Value("${mongodb.connection.pool.max-wait-time-ms:30000}")
    private long maxWaitTimeMs;

    @Value("${mongodb.connection.timeout-ms:10000}")
    private long connectionTimeoutMs;

    @Value("${mongodb.connection.socket-timeout-ms:30000}")
    private long socketTimeoutMs;

    @Value("${mongodb.heartbeat.interval-ms:10000}")
    private long heartbeatIntervalMs;

    @Value("${mongodb.server-selection-timeout-ms:30000}")
    private long serverSelectionTimeoutMs;

    /**
     * Creates MongoDB client with optimized settings for VPC peering
     * through MongoDB Atlas M0 cluster.
     */
    @Bean
    @Override
    public MongoClient reactiveMongoClient() {
        log.info("Initializing MongoDB client with connection pool settings");
        log.debug("Min pool size: {}, Max pool size: {}", minPoolSize, maxPoolSize);

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new com.mongodb.ConnectionString(mongoDbUri))
                .applyToConnectionPoolSettings(builder -> {
                    builder.minSize(minPoolSize)
                           .maxSize(maxPoolSize)
                           .maxWaitTime(maxWaitTimeMs, TimeUnit.MILLISECONDS)
                           .maintenanceFrequency(10, TimeUnit.SECONDS);
                    log.info("Connection pool configured: min={}, max={}", minPoolSize, maxPoolSize);
                })
                .applyToSocketSettings(builder -> {
                    builder.connectTimeout(connectionTimeoutMs, TimeUnit.MILLISECONDS)
                           .readTimeout(socketTimeoutMs, TimeUnit.MILLISECONDS);
                    log.info("Socket settings configured: connectTimeout={}ms, readTimeout={}ms",
                            connectionTimeoutMs, socketTimeoutMs);
                })
                .applyToServerSettings(builder -> {
                    builder.heartbeatFrequency(heartbeatIntervalMs, TimeUnit.MILLISECONDS)
                           .serverSelectionTimeout(serverSelectionTimeoutMs, TimeUnit.MILLISECONDS);
                    log.info("Server settings configured: heartbeat={}ms, serverSelection={}ms",
                            heartbeatIntervalMs, serverSelectionTimeoutMs);
                })
                .build();

        MongoClient client = MongoClients.create(settings);
        log.info("MongoDB client initialized successfully");
        return client;
    }

    /**
     * Returns the database name for MongoDB operations
     */
    @Override
    protected String getDatabaseName() {
        return database;
    }

    /**
     * Additional health check bean to verify MongoDB connectivity
     */
    @Bean
    public MongoDBHealthIndicator mongoDBHealthIndicator(MongoClient mongoClient) {
        return new MongoDBHealthIndicator(mongoClient, getDatabaseName());
    }
}
