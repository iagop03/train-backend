package com.iagop.train.service.mongodb;

import com.mongodb.reactivestreams.client.MongoClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Service for MongoDB Atlas connection management
 * Handles VPC peering connection validation and diagnostics
 */
@Slf4j
@Service
public class MongoDBConnectionService {

    private final MongoClient mongoClient;
    private final String database;

    public MongoDBConnectionService(MongoClient mongoClient, 
                                   org.springframework.beans.factory.annotation.Value("${spring.data.mongodb.database}") String database) {
        this.mongoClient = mongoClient;
        this.database = database;
    }

    /**
     * Validates MongoDB Atlas connection through VPC peering
     */
    public Mono<Boolean> validateConnection() {
        return Mono.fromCallable(() -> {
                    var db = mongoClient.getDatabase(database);
                    var pingCommand = new org.bson.Document("ping", 1);
                    
                    return Mono.from(db.runCommand(pingCommand))
                            .timeout(Duration.ofSeconds(10))
                            .map(doc -> {
                                log.info("MongoDB Atlas connection verified: {}", doc);
                                return true;
                            })
                            .onErrorResume(e -> {
                                log.error("MongoDB Atlas connection failed: {}", e.getMessage());
                                return Mono.just(false);
                            });
                })
                .flatMap(mono -> mono)
                .doOnError(e -> log.error("Connection validation error: {}", e.getMessage(), e));
    }

    /**
     * Gets server status information
     */
    public Mono<org.bson.Document> getServerStatus() {
        return Mono.fromCallable(() -> {
                    var adminDb = mongoClient.getDatabase("admin");
                    var serverStatusCommand = new org.bson.Document("serverStatus", 1);
                    
                    return Mono.from(adminDb.runCommand(serverStatusCommand))
                            .timeout(Duration.ofSeconds(10))
                            .doOnNext(doc -> log.debug("Server status: {}", doc));
                })
                .flatMap(mono -> mono)
                .doOnError(e -> log.error("Failed to get server status: {}", e.getMessage()));
    }

    /**
     * Checks MongoDB Atlas cluster info including version
     */
    public Mono<org.bson.Document> getClusterInfo() {
        return Mono.fromCallable(() -> {
                    var adminDb = mongoClient.getDatabase("admin");
                    var buildInfoCommand = new org.bson.Document("buildInfo", 1);
                    
                    return Mono.from(adminDb.runCommand(buildInfoCommand))
                            .timeout(Duration.ofSeconds(10))
                            .doOnNext(doc -> log.info("MongoDB version: {}", doc.getString("version")));
                })
                .flatMap(mono -> mono)
                .doOnError(e -> log.error("Failed to get cluster info: {}", e.getMessage()));
    }
}
