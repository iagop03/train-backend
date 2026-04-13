package com.train.repository.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyVolumeDto {
    
    @Field("_id")
    private Integer week;
    
    @Field("totalVolume")
    private Double totalVolume;
    
    @Field("totalReps")
    private Integer totalReps;
    
    @Field("totalSets")
    private Integer totalSets;
    
    @Field("workoutCount")
    private Integer workoutCount;
    
    @Field("avgDuration")
    private Double avgDuration;
}
