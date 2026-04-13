package com.train.controller;

import com.train.security.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/workouts")
public class WorkoutController {

    @GetMapping
    @PreAuthorize("hasRole('TRAINER')")
    public ResponseEntity<Map<String, Object>> getAllWorkouts() {
        log.info("Fetching all workouts for trainer: {}", SecurityUtils.getCurrentUserLogin());
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "All workouts retrieved");
        response.put("trainerId", SecurityUtils.getCurrentUserId().orElse("unknown"));
        response.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('TRAINER')")
    public ResponseEntity<Map<String, Object>> getWorkoutById(@PathVariable String id) {
        log.info("Fetching workout {} for trainer: {}", id, SecurityUtils.getCurrentUserLogin());
        
        Map<String, Object> response = new HashMap<>();
        response.put("workoutId", id);
        response.put("trainerId", SecurityUtils.getCurrentUserId().orElse("unknown"));
        response.put("message", "Workout retrieved successfully");
        
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('TRAINER')")
    public ResponseEntity<Map<String, Object>> createWorkout() {
        log.info("Creating new workout for trainer: {}", SecurityUtils.getCurrentUserLogin());
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Workout created successfully");
        response.put("trainerId", SecurityUtils.getCurrentUserId().orElse("unknown"));
        response.put("workoutId", "WRK_" + System.currentTimeMillis());
        response.put("createdAt", LocalDateTime.now());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TRAINER')")
    public ResponseEntity<Map<String, Object>> updateWorkout(@PathVariable String id) {
        log.info("Updating workout {} for trainer: {}", id, SecurityUtils.getCurrentUserLogin());
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Workout updated successfully");
        response.put("workoutId", id);
        response.put("trainerId", SecurityUtils.getCurrentUserId().orElse("unknown"));
        response.put("updatedAt", LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TRAINER')")
    public ResponseEntity<Map<String, String>> deleteWorkout(@PathVariable String id) {
        log.info("Deleting workout {} for trainer: {}", id, SecurityUtils.getCurrentUserLogin());
        
        return ResponseEntity.ok(Map.of(
                "message", "Workout deleted successfully",
                "workoutId", id
        ));
    }
}
