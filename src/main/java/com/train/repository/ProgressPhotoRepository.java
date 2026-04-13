package com.train.repository;

import com.train.entity.ProgressPhoto;
import com.train.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ProgressPhotoRepository extends JpaRepository<ProgressPhoto, Long> {
    
    @Query("SELECT pp FROM ProgressPhoto pp WHERE pp.user = :user ORDER BY pp.capturedDate DESC")
    List<ProgressPhoto> findByUserOrderByDateDesc(@Param("user") User user);
    
    @Query("SELECT pp FROM ProgressPhoto pp WHERE pp.user = :user AND CAST(pp.capturedDate AS DATE) = :date")
    List<ProgressPhoto> findByUserAndDate(@Param("user") User user, @Param("date") LocalDate date);
    
    @Query("SELECT pp FROM ProgressPhoto pp WHERE pp.user = :user AND pp.capturedDate BETWEEN :startDate AND :endDate ORDER BY pp.capturedDate DESC")
    List<ProgressPhoto> findByUserAndDateRange(@Param("user") User user, @Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);
    
    @Query("SELECT pp FROM ProgressPhoto pp WHERE pp.user = :user AND pp.bodyPart = :bodyPart ORDER BY pp.capturedDate DESC")
    List<ProgressPhoto> findByUserAndBodyPart(@Param("user") User user, @Param("bodyPart") String bodyPart);
    
    @Query("SELECT COUNT(pp) FROM ProgressPhoto pp WHERE pp.user = :user")
    long countByUser(@Param("user") User user);
    
    @Query("SELECT DISTINCT pp.bodyPart FROM ProgressPhoto pp WHERE pp.user = :user ORDER BY pp.bodyPart")
    List<String> findBodyPartsTrackedByUser(@Param("user") User user);
}