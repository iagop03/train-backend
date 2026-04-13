package com.train.repository;

import com.train.entity.WorkoutExercise;
import com.train.entity.Workout;
import com.train.entity.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkoutExerciseRepository extends JpaRepository<WorkoutExercise, Long> {
    
    @Query("SELECT we FROM WorkoutExercise we WHERE we.workout = :workout ORDER BY we.sequenceOrder")
    List<WorkoutExercise> findByWorkoutOrderBySequence(@Param("workout") Workout workout);
    
    @Query("SELECT we FROM WorkoutExercise we WHERE we.exercise = :exercise ORDER BY we.createdAt DESC")
    List<WorkoutExercise> findByExerciseOrderByDate(@Param("exercise") Exercise exercise);
    
    @Query("SELECT COUNT(we) FROM WorkoutExercise we WHERE we.workout = :workout")
    long countByWorkout(@Param("workout") Workout workout);
    
    @Query("SELECT SUM(we.sets * we.reps) FROM WorkoutExercise we WHERE we.workout = :workout")
    Integer getTotalRepsInWorkout(@Param("workout") Workout workout);
    
    @Query("SELECT SUM(we.weight * we.sets * we.reps) FROM WorkoutExercise we WHERE we.workout = :workout")
    Double getTotalVolumeInWorkout(@Param("workout") Workout workout);
    
    @Query("SELECT we FROM WorkoutExercise we WHERE we.exercise = :exercise ORDER BY we.weight DESC LIMIT 1")
    WorkoutExercise findMaxWeightForExercise(@Param("exercise") Exercise exercise);
    
    @Query("SELECT AVG(we.weight) FROM WorkoutExercise we WHERE we.exercise = :exercise")
    Double getAverageWeightForExercise(@Param("exercise") Exercise exercise);
}