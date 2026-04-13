package com.train.repository;

import com.train.entity.Achievement;
import com.train.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AchievementRepository extends JpaRepository<Achievement, Long> {
    
    @Query("SELECT a FROM Achievement a WHERE a.user = :user ORDER BY a.unlockedDate DESC")
    List<Achievement> findByUserOrderByUnlockedDateDesc(@Param("user") User user);
    
    @Query("SELECT a FROM Achievement a WHERE a.user = :user AND a.isUnlocked = true ORDER BY a.unlockedDate DESC")
    List<Achievement> findUnlockedByUser(@Param("user") User user);
    
    @Query("SELECT a FROM Achievement a WHERE a.user = :user AND a.isUnlocked = false")
    List<Achievement> findLockedByUser(@Param("user") User user);
    
    @Query("SELECT COUNT(a) FROM Achievement a WHERE a.user = :user AND a.isUnlocked = true")
    long countUnlockedByUser(@Param("user") User user);
    
    @Query("SELECT a FROM Achievement a WHERE a.category = :category ORDER BY a.createdAt DESC")
    List<Achievement> findByCategory(@Param("category") String category);
    
    @Query("SELECT DISTINCT a.category FROM Achievement a ORDER BY a.category")
    List<String> findAllCategories();
    
    @Query("SELECT a FROM Achievement a WHERE a.user = :user AND a.category = :category AND a.isUnlocked = true")
    List<Achievement> findByUserAndCategory(@Param("user") User user, @Param("category") String category);
}