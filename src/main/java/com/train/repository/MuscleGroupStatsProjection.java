package com.train.repository;

import com.fasterxml.jackson.annotation.JsonProperty;

public interface MuscleGroupStatsProjection {
    @JsonProperty("_id")
    String getMuscleGroup();

    Integer getCount();

    Double getAvgIntensity();
}
