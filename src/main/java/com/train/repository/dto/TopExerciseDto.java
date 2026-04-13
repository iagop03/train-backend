package com.train.repository.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopExerciseDto {
    
    @Field("_id")
    private String exerciseName;
    
    @Field("totalVolume")
    private Double totalVolume;
    
    @Field("totalReps")
    private Integer totalReps;
    
    @Field("count")
    private Integer count;
}
