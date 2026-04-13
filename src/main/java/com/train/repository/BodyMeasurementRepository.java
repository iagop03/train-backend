package com.train.repository;

import com.train.entity.BodyMeasurement;
import com.train.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BodyMeasurementRepository extends JpaRepository<BodyMeasurement, Long> {
    
    @Query("SELECT bm FROM BodyMeasurement bm WHERE bm.user = :user ORDER BY bm.measurementDate DESC")
    List<BodyMeasurement> findByUserOrderByDateDesc(@Param("user") User user);
    
    @Query("SELECT bm FROM BodyMeasurement bm WHERE bm.user = :user AND CAST(bm.measurementDate AS DATE) = :date")
    Optional<BodyMeasurement> findByUserAndDate(@Param("user") User user, @Param("date") LocalDate date);
    
    @Query("SELECT bm FROM BodyMeasurement bm WHERE bm.user = :user AND bm.measurementDate BETWEEN :startDate AND :endDate ORDER BY bm.measurementDate DESC")
    List<BodyMeasurement> findByUserAndDateRange(@Param("user") User user, @Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);
    
    @Query(value = "SELECT bm FROM BodyMeasurement bm WHERE bm.user = :user ORDER BY bm.measurementDate DESC LIMIT 1")
    Optional<BodyMeasurement> findLatestByUser(@Param("user") User user);
    
    @Query("SELECT bm FROM BodyMeasurement bm WHERE bm.user = :user ORDER BY bm.weight ASC")
    List<BodyMeasurement> findLowestWeightRecords(@Param("user") User user);
    
    @Query("SELECT bm FROM BodyMeasurement bm WHERE bm.user = :user ORDER BY bm.weight DESC")
    List<BodyMeasurement> findHighestWeightRecords(@Param("user") User user);
    
    @Query("SELECT (SELECT bm1.weight FROM BodyMeasurement bm1 WHERE bm1.user = :user ORDER BY bm1.measurementDate DESC LIMIT 1) - (SELECT bm2.weight FROM BodyMeasurement bm2 WHERE bm2.user = :user ORDER BY bm2.measurementDate ASC LIMIT 1) FROM BodyMeasurement bm WHERE bm.user = :user LIMIT 1")
    Double calculateTotalWeightChange(@Param("user") User user);
}