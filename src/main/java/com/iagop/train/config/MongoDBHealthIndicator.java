package com.iagop.train.config;

import com.mongodb.reactivestreams.client.MongoClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

/**
 * Custom Health Indicator for MongoDB Atlas connection status
 * Verifies connectivity through VPC peering
 */
@Slf4j
@Component
public class MongoDBHealthIndicator implements HealthIndicator {

    private final MongoClient mongoClient;
    private final String databaseName;
    private static final long HEALTH_CHECK_TIMEOUT_MS = 5000;

    public MongoDBHealthIndicator(MongoClient mongoClient, String databaseName) {
        this.mongoClient = mongoClient;
        this.databaseName = databaseName;
    }

    @Override
    public Health health() {
        try {
            Mono<Boolean> healthCheck = Mono.fromCallable(() -> {
                        var database = mongoClient.getDatabase(databaseName);
                        var document = new org.bson.Document("ping", 1);
                        
                        return Mono.from(database.runCommand(document))
                                .timeout(Duration.ofMillis(HEALTH_CHECK_TIMEOUT_MS))
                                .block() != null;
                    })
                    .subscribeOn(Schedulers.boundedElastic())
                    .timeout(Duration.ofMillis(HEALTH_CHECK_TIMEOUT_MS + 1000));

            Boolean isHealthy = healthCheck.block();
            
            if (Boolean.TRUE.equals(isHealthy)) {
                log.debug("MongoDB health check passed");
                return Health.up()
                        .withDetail("database", databaseName)
                        .withDetail("status", "Connected to MongoDB Atlas via VPC peering")
                        .build();
            } else {
                log.warn("MongoDB health check failed: no response");
                return Health.down()
                        .withDetail("database", databaseName)
                        .withDetail("error", "No response from MongoDB server")
                        .build();
            }
        } catch (Exception e) {
            log.error("MongoDB health check error: {}", e.getMessage(), e);
            return Health.down()
                    .withDetail("database", databaseName)
                    .withException(e)
                    .build();
        }
    }
}
