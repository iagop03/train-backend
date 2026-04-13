package com.train.repository;

import com.train.entity.Program;
import com.train.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProgramRepository extends JpaRepository<Program, Long> {
    
    @Query("SELECT p FROM Program p WHERE p.user = :user ORDER BY p.createdAt DESC")
    List<Program> findByUserOrderByCreatedAtDesc(@Param("user") User user);
    
    @Query("SELECT p FROM Program p WHERE p.user = :user AND p.isActive = true")
    Optional<Program> findActiveByUser(@Param("user") User user);
    
    @Query("SELECT p FROM Program p WHERE p.user = :user AND p.difficulty = :difficulty")
    List<Program> findByUserAndDifficulty(@Param("user") User user, @Param("difficulty") String difficulty);
    
    @Query("SELECT p FROM Program p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Program> searchPrograms(@Param("searchTerm") String searchTerm);
    
    @Query("SELECT COUNT(p) FROM Program p WHERE p.user = :user")
    long countByUser(@Param("user") User user);
    
    @Query("SELECT p FROM Program p WHERE p.isPublic = true ORDER BY p.createdAt DESC")
    List<Program> findPublicPrograms();
    
    @Query("SELECT p FROM Program p WHERE p.user = :user AND p.duration = :duration")
    List<Program> findByUserAndDuration(@Param("user") User user, @Param("duration") Integer duration);
}