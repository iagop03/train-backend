package com.train.controller;

import com.train.security.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @GetMapping("/profile")
    @PreAuthorize("hasAnyRole('USER', 'TRAINER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getUserProfile() {
        Map<String, Object> profile = new HashMap<>();
        
        SecurityUtils.getCurrentUserId().ifPresent(id -> profile.put("id", id));
        SecurityUtils.getCurrentUserLogin().ifPresent(login -> profile.put("username", login));
        SecurityUtils.getCurrentUserEmail().ifPresent(email -> profile.put("email", email));
        
        SecurityUtils.getJwt().ifPresent(jwt -> {
            profile.put("firstName", jwt.getClaimAsString("given_name"));
            profile.put("lastName", jwt.getClaimAsString("family_name"));
        });
        
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/profile")
    @PreAuthorize("hasAnyRole('USER', 'TRAINER', 'ADMIN')")
    public ResponseEntity<Map<String, String>> updateUserProfile() {
        return ResponseEntity.ok(Map.of(
                "message", "Profile update endpoint",
                "note", "User profile update should be done through Keycloak Admin API"
        ));
    }

    @GetMapping("/workouts")
    @PreAuthorize("hasAnyRole('USER', 'TRAINER', 'ADMIN')")
    public ResponseEntity<Map<String, String>> getUserWorkouts() {
        return ResponseEntity.ok(Map.of(
                "message", "User workouts retrieved",
                "userId", SecurityUtils.getCurrentUserId().orElse("unknown")
        ));
    }
}
