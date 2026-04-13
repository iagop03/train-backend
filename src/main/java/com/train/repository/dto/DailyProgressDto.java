package com.train.repository.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyProgressDto {
    
    @Field("_id")
    private String date;
    
    @Field("totalVolume")
    private Double totalVolume;
    
    @Field("totalReps")
    private Integer totalReps;
    
    @Field("workoutCount")
    private Integer workoutCount;
}
