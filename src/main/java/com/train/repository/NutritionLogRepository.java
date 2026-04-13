package com.train.repository;

import com.train.entity.NutritionLog;
import com.train.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface NutritionLogRepository extends JpaRepository<NutritionLog, Long> {
    
    @Query("SELECT nl FROM NutritionLog nl WHERE nl.user = :user AND CAST(nl.logDate AS DATE) = :date ORDER BY nl.logDate DESC")
    List<NutritionLog> findByUserAndDate(@Param("user") User user, @Param("date") LocalDate date);
    
    @Query("SELECT nl FROM NutritionLog nl WHERE nl.user = :user AND nl.logDate BETWEEN :startDate AND :endDate ORDER BY nl.logDate DESC")
    List<NutritionLog> findByUserAndDateRange(@Param("user") User user, @Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);
    
    @Query("SELECT SUM(nl.calories) FROM NutritionLog nl WHERE nl.user = :user AND CAST(nl.logDate AS DATE) = :date")
    Integer getDailyCalories(@Param("user") User user, @Param("date") LocalDate date);
    
    @Query("SELECT SUM(nl.protein) FROM NutritionLog nl WHERE nl.user = :user AND CAST(nl.logDate AS DATE) = :date")
    Double getDailyProtein(@Param("user") User user, @Param("date") LocalDate date);
    
    @Query("SELECT SUM(nl.carbs) FROM NutritionLog nl WHERE nl.user = :user AND CAST(nl.logDate AS DATE) = :date")
    Double getDailyCarbs(@Param("user") User user, @Param("date") LocalDate date);
    
    @Query("SELECT SUM(nl.fat) FROM NutritionLog nl WHERE nl.user = :user AND CAST(nl.logDate AS DATE) = :date")
    Double getDailyFat(@Param("user") User user, @Param("date") LocalDate date);
    
    @Query("SELECT AVG(nl.calories) FROM NutritionLog nl WHERE nl.user = :user AND nl.logDate BETWEEN :startDate AND :endDate")
    Integer getAverageCaloriesInRange(@Param("user") User user, @Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);
}