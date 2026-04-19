package com.train.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Health check endpoint for monitoring.
 * No authentication required.
 */
@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping("/ping")
    public ResponseEntity<?> ping() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "TrAIn Backend is running");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    public ResponseEntity<?> status() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "HEALTHY");
        response.put("version", "1.0.0");
        response.put("service", "train-backend");
        return ResponseEntity.ok(response);
    }
}
