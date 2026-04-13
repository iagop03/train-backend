package com.train.repository;

import com.train.entity.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExerciseRepository extends JpaRepository<Exercise, Long> {
    
    @Query("SELECT e FROM Exercise e WHERE e.name = :name")
    Optional<Exercise> findByName(@Param("name") String name);
    
    @Query("SELECT e FROM Exercise e WHERE e.muscleGroup = :muscleGroup ORDER BY e.name")
    List<Exercise> findByMuscleGroup(@Param("muscleGroup") String muscleGroup);
    
    @Query("SELECT e FROM Exercise e WHERE e.difficulty = :difficulty")
    List<Exercise> findByDifficulty(@Param("difficulty") String difficulty);
    
    @Query("SELECT DISTINCT e.muscleGroup FROM Exercise e ORDER BY e.muscleGroup")
    List<String> findAllMuscleGroups();
    
    @Query("SELECT e FROM Exercise e WHERE LOWER(e.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(e.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Exercise> searchExercises(@Param("searchTerm") String searchTerm);
    
    @Query("SELECT e FROM Exercise e WHERE e.muscleGroup = :muscleGroup AND e.difficulty = :difficulty")
    List<Exercise> findByMuscleGroupAndDifficulty(@Param("muscleGroup") String muscleGroup, @Param("difficulty") String difficulty);
    
    @Query("SELECT e FROM Exercise e WHERE e.isActive = true ORDER BY e.name")
    List<Exercise> findAllActive();
}