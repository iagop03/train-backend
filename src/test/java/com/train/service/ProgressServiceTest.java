package com.train.service;

import com.train.model.UserProgress;
import com.train.model.WeeklyProgress;
import com.train.repository.UserProgressRepository;
import com.train.repository.dto.DailyProgressDto;
import com.train.repository.dto.TopExerciseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.mongodb.core.MongoTemplate;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

public class ProgressServiceTest {
    
    @Mock
    private UserProgressRepository progressRepository;
    
    @Mock
    private MongoTemplate mongoTemplate;
    
    @InjectMocks
    private ProgressService progressService;
    
    private String testUserId = "test-user-123";
    private UserProgress testProgress;
    
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        testProgress = new UserProgress();
        testProgress.setId("progress-1");
        testProgress.setUserId(testUserId);
        testProgress.setDate(LocalDateTime.now());
        testProgress.setWorkoutType("Strength");
        testProgress.setTotalVolume(1000.0);
        testProgress.setTotalReps(50);
        testProgress.setTotalSets(10);
        testProgress.setDurationMinutes(60);
    }
    
    @Test
    public void testSaveProgress() {
        when(progressRepository.save(any(UserProgress.class))).thenReturn(testProgress);
        
        UserProgress saved = progressService.saveProgress(testProgress);
        
        assertNotNull(saved);
        assertEquals(testUserId, saved.getUserId());
        assertEquals(1000.0, saved.getTotalVolume());
    }
    
    @Test
    public void testGetUserProgress() {
        List<UserProgress> progressList = new ArrayList<>();
        progressList.add(testProgress);
        
        when(progressRepository.findByUserIdOrderByDateDesc(testUserId)).thenReturn(progressList);
        
        List<UserProgress> result = progressService.getUserProgress(testUserId);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testProgress.getId(), result.get(0).getId());
    }
    
    @Test
    public void testGetUserProgressByDateRange() {
        List<UserProgress> progressList = new ArrayList<>();
        progressList.add(testProgress);
        
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        
        when(progressRepository.findByUserIdAndDateRange(testUserId, startDate, endDate))
            .thenReturn(progressList);
        
        List<UserProgress> result = progressService.getUserProgressByDateRange(testUserId, startDate, endDate);
        
        assertNotNull(result);
        assertEquals(1, result.size());
    }
    
    @Test
    public void testGetWeeklyProgress() {
        List<UserProgress> weeklyRecords = new ArrayList<>();
        weeklyRecords.add(testProgress);
        
        when(progressRepository.findByUserIdAndDateRange(anyString(), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(weeklyRecords);
        
        WeeklyProgress weekly = progressService.getWeeklyProgress(testUserId, LocalDate.now());
        
        assertNotNull(weekly);
        assertEquals(testUserId, weekly.getUserId());
        assertEquals(1, weekly.getWorkoutsCount());
        assertEquals(1000.0, weekly.getTotalWeeklyVolume());
    }
    
    @Test
    public void testGetProgressStats() {
        List<UserProgress> progressList = new ArrayList<>();
        progressList.add(testProgress);
        
        when(progressRepository.findByUserIdAndDateRange(anyString(), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(progressList);
        
        var stats = progressService.getProgressStats(testUserId, 30);
        
        assertNotNull(stats);
        assertEquals(1, stats.get("totalWorkouts"));
        assertEquals(1000.0, stats.get("totalVolume"));
        assertEquals(30, stats.get("days"));
    }
}
