package com.train.controller;

import com.train.security.KeycloakService;
import com.train.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final KeycloakService keycloakService;

    @PostMapping("/users/{userId}/roles/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> assignRoleToUser(
            @PathVariable String userId,
            @PathVariable String role) {
        
        log.info("Admin {} assigning role {} to user {}", 
                SecurityUtils.getCurrentUserLogin(), role, userId);
        
        keycloakService.assignRoleToUser(userId, role);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Role assigned successfully");
        response.put("userId", userId);
        response.put("role", role);
        response.put("assignedBy", SecurityUtils.getCurrentUserLogin().orElse("unknown"));
        response.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/users/{userId}/roles/{role}/remove")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> removeRoleFromUser(
            @PathVariable String userId,
            @PathVariable String role) {
        
        log.info("Admin {} removing role {} from user {}", 
                SecurityUtils.getCurrentUserLogin(), role, userId);
        
        keycloakService.removeRoleFromUser(userId, role);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Role removed successfully");
        response.put("userId", userId);
        response.put("role", role);
        response.put("removedBy", SecurityUtils.getCurrentUserLogin().orElse("unknown"));
        response.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/health")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "healthy",
                "service", "train-backend",
                "timestamp", LocalDateTime.now().toString()
        ));
    }
}
