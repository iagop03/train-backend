package com.train.controller;

import com.train.model.UserProgress;
import com.train.model.WeeklyProgress;
import com.train.service.ProgressService;
import com.train.repository.dto.WeeklyVolumeDto;
import com.train.repository.dto.DailyProgressDto;
import com.train.repository.dto.WorkoutTypeBreakdownDto;
import com.train.repository.dto.TopExerciseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/progress")
@Slf4j
public class ProgressController {
    
    @Autowired
    private ProgressService progressService;
    
    @PostMapping("/save")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserProgress> saveProgress(@RequestBody UserProgress progress) {
        log.info("Saving progress for user: {}", progress.getUserId());
        UserProgress saved = progressService.saveProgress(progress);
        return ResponseEntity.ok(saved);
    }
    
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<UserProgress>> getUserProgress(@PathVariable String userId) {
        log.info("Fetching progress for user: {}", userId);
        List<UserProgress> progress = progressService.getUserProgress(userId);
        return ResponseEntity.ok(progress);
    }
    
    @GetMapping("/user/{userId}/range")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<UserProgress>> getUserProgressByRange(
            @PathVariable String userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("Fetching progress for user: {} between {} and {}", userId, startDate, endDate);
        List<UserProgress> progress = progressService.getUserProgressByDateRange(userId, startDate, endDate);
        return ResponseEntity.ok(progress);
    }
    
    @GetMapping("/user/{userId}/weekly/{weekDate}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<WeeklyProgress> getWeeklyProgress(
            @PathVariable String userId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekDate) {
        log.info("Fetching weekly progress for user: {} for week {}", userId, weekDate);
        WeeklyProgress progress = progressService.getWeeklyProgress(userId, weekDate);
        return ResponseEntity.ok(progress);
    }
    
    @GetMapping("/user/{userId}/daily-trend")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<DailyProgressDto>> getDailyTrend(
            @PathVariable String userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("Fetching daily trend for user: {}", userId);
        List<DailyProgressDto> trend = progressService.getDailyProgressTrend(userId, startDate, endDate);
        return ResponseEntity.ok(trend);
    }
    
    @GetMapping("/user/{userId}/weekly-volume")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<WeeklyVolumeDto>> getWeeklyVolume(
            @PathVariable String userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("Fetching weekly volume for user: {}", userId);
        List<WeeklyVolumeDto> volume = progressService.getWeeklyVolumeAggregation(userId, startDate, endDate);
        return ResponseEntity.ok(volume);
    }
    
    @GetMapping("/user/{userId}/workout-type-breakdown")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<WorkoutTypeBreakdownDto>> getWorkoutTypeBreakdown(
            @PathVariable String userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("Fetching workout type breakdown for user: {}", userId);
        List<WorkoutTypeBreakdownDto> breakdown = progressService.getWorkoutTypeBreakdown(userId, startDate, endDate);
        return ResponseEntity.ok(breakdown);
    }
    
    @GetMapping("/user/{userId}/top-exercises")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<TopExerciseDto>> getTopExercises(
            @PathVariable String userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("Fetching top exercises for user: {}", userId);
        List<TopExerciseDto> exercises = progressService.getTopExercises(userId, startDate, endDate);
        return ResponseEntity.ok(exercises);
    }
    
    @GetMapping("/user/{userId}/volume-growth")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Double> getVolumeGrowth(
            @PathVariable String userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate currentWeek,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate previousWeek) {
        log.info("Calculating volume growth for user: {}", userId);
        Double growth = progressService.calculateVolumeGrowth(userId, currentWeek, previousWeek);
        return ResponseEntity.ok(growth);
    }
    
    @GetMapping("/user/{userId}/stats")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, Object>> getProgressStats(
            @PathVariable String userId,
            @RequestParam(defaultValue = "30") Integer days) {
        log.info("Fetching progress stats for user: {} last {} days", userId, days);
        Map<String, Object> stats = progressService.getProgressStats(userId, days);
        return ResponseEntity.ok(stats);
    }
}
