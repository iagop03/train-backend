package com.train.repository;

import com.train.entity.Workout;
import com.train.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkoutRepository extends JpaRepository<Workout, Long> {
    
    @Query("SELECT w FROM Workout w WHERE w.user = :user ORDER BY w.startTime DESC")
    List<Workout> findByUserOrderByStartTimeDesc(@Param("user") User user);
    
    @Query("SELECT w FROM Workout w WHERE w.user = :user AND CAST(w.startTime AS DATE) = :date")
    List<Workout> findByUserAndDate(@Param("user") User user, @Param("date") LocalDate date);
    
    @Query("SELECT w FROM Workout w WHERE w.user = :user AND w.startTime BETWEEN :startDate AND :endDate ORDER BY w.startTime DESC")
    List<Workout> findByUserAndDateRange(@Param("user") User user, @Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);
    
    @Query("SELECT w FROM Workout w WHERE w.user = :user AND w.workoutType = :workoutType")
    List<Workout> findByUserAndType(@Param("user") User user, @Param("workoutType") String workoutType);
    
    @Query("SELECT COUNT(w) FROM Workout w WHERE w.user = :user")
    long countByUser(@Param("user") User user);
    
    @Query("SELECT SUM(w.duration) FROM Workout w WHERE w.user = :user AND w.startTime BETWEEN :startDate AND :endDate")
    Integer getTotalDurationInRange(@Param("user") User user, @Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);
    
    @Query("SELECT SUM(w.caloriesBurned) FROM Workout w WHERE w.user = :user AND CAST(w.startTime AS DATE) = :date")
    Integer getTotalCaloriesByDate(@Param("user") User user, @Param("date") LocalDate date);
    
    @Query("SELECT w FROM Workout w WHERE w.user = :user ORDER BY w.caloriesBurned DESC")
    List<Workout> findMostIntenseWorkouts(@Param("user") User user);
    
    @Query(value = "SELECT w FROM Workout w WHERE w.user = :user ORDER BY w.startTime DESC LIMIT 1")
    Optional<Workout> findLastWorkout(@Param("user") User user);
}