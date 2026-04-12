package com.train.controller;

import com.train.config.EnvironmentConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
@Slf4j
@RequiredArgsConstructor
public class HealthController {

    private final EnvironmentConfig environmentConfig;

    @GetMapping
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("environment", environmentConfig.getEnvironment());
        response.put("version", environmentConfig.getVersion());
        response.put("apiBaseUrl", environmentConfig.getApiBaseUrl());
        
        log.info("Health check - Environment: {}, Version: {}", 
                environmentConfig.getEnvironment(), 
                environmentConfig.getVersion());
        
        return ResponseEntity.ok(response);
    }
}
