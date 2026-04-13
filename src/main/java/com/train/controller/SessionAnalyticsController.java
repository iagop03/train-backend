package com.train.controller;

import com.train.repository.*;
import com.train.service.SessionAnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/analytics")
@PreAuthorize("isAuthenticated()")
public class SessionAnalyticsController {

    @Autowired
    private SessionAnalyticsService sessionAnalyticsService;

    @GetMapping("/exercises")
    public ResponseEntity<List<ExerciseStatsProjection>> getExerciseStats(
            @RequestParam String userId) {
        return ResponseEntity.ok(sessionAnalyticsService.getExerciseStats(userId));
    }

    @GetMapping("/daily-activity")
    public ResponseEntity<List<DailyActivityProjection>> getDailyActivity(
            @RequestParam String userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        if (startDate != null && endDate != null) {
            return ResponseEntity.ok(sessionAnalyticsService.getDailyActivityCustomRange(userId, startDate, endDate));
        }
        return ResponseEntity.ok(sessionAnalyticsService.getDailyActivityLastMonth(userId));
    }

    @GetMapping("/muscle-groups")
    public ResponseEntity<List<MuscleGroupStatsProjection>> getMuscleGroupStats(
            @RequestParam String userId) {
        return ResponseEntity.ok(sessionAnalyticsService.getMuscleGroupStats(userId));
    }

    @GetMapping("/user-stats")
    public ResponseEntity<Map<String, Object>> getUserStats(
            @RequestParam String userId,
            @RequestParam(required = false, defaultValue = "month") String period) {
        Map<String, Object> response = new HashMap<>();
        List<UserStatsProjection> stats = period.equals("week")
                ? sessionAnalyticsService.getUserStatsLastWeek(userId)
                : sessionAnalyticsService.getUserStatsLastMonth(userId);
        response.put("period", period);
        response.put("stats", stats);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/top-exercises")
    public ResponseEntity<List<TopExercisesProjection>> getTopExercises(
            @RequestParam String userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        if (startDate != null && endDate != null) {
            return ResponseEntity.ok(sessionAnalyticsService.getTopExercisesCustomRange(userId, startDate, endDate));
        }
        return ResponseEntity.ok(sessionAnalyticsService.getTopExercisesLastMonth(userId));
    }

    @GetMapping("/volume-by-muscle")
    public ResponseEntity<List<VolumeByMuscleProjection>> getVolumeByMuscle(
            @RequestParam String userId) {
        return ResponseEntity.ok(sessionAnalyticsService.getVolumeByMuscleGroup(userId));
    }

    @GetMapping("/session-count")
    public ResponseEntity<Map<String, Long>> getSessionCount(
            @RequestParam String userId,
            @RequestParam(required = false, defaultValue = "month") String period) {
        Map<String, Long> response = new HashMap<>();
        long count = period.equals("week")
                ? sessionAnalyticsService.getSessionCountLastWeek(userId)
                : sessionAnalyticsService.getSessionCountLastMonth(userId);
        response.put("period", period);
        response.put("count", count);
        return ResponseEntity.ok(response);
    }
}
