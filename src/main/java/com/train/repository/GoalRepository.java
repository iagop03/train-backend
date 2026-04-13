package com.train.repository;

import com.train.entity.Goal;
import com.train.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {
    
    @Query("SELECT g FROM Goal g WHERE g.user = :user ORDER BY g.createdAt DESC")
    List<Goal> findByUserOrderByCreatedAtDesc(@Param("user") User user);
    
    @Query("SELECT g FROM Goal g WHERE g.user = :user AND g.status = 'ACTIVE'")
    List<Goal> findActiveGoalsByUser(@Param("user") User user);
    
    @Query("SELECT g FROM Goal g WHERE g.user = :user AND g.status = 'COMPLETED'")
    List<Goal> findCompletedGoalsByUser(@Param("user") User user);
    
    @Query("SELECT g FROM Goal g WHERE g.user = :user AND CAST(g.targetDate AS DATE) <= :date AND g.status = 'ACTIVE'")
    List<Goal> findOverdueGoals(@Param("user") User user, @Param("date") LocalDate date);
    
    @Query("SELECT g FROM Goal g WHERE g.user = :user AND g.goalType = :goalType")
    List<Goal> findByUserAndType(@Param("user") User user, @Param("goalType") String goalType);
    
    @Query("SELECT COUNT(g) FROM Goal g WHERE g.user = :user AND g.status = 'COMPLETED'")
    long countCompletedGoals(@Param("user") User user);
    
    @Query("SELECT g FROM Goal g WHERE g.user = :user ORDER BY g.progress DESC")
    List<Goal> findByUserOrderByProgress(@Param("user") User user);
    
    @Query("SELECT g FROM Goal g WHERE g.user = :user AND g.status IN ('ACTIVE', 'IN_PROGRESS')")
    List<Goal> findOngoingGoalsByUser(@Param("user") User user);
}