package com.train.repository;

import com.fasterxml.jackson.annotation.JsonProperty;

public interface ExerciseStatsProjection {
    @JsonProperty("_id")
    String getExerciseId();

    Integer getTotalSessions();

    Double getAvgDuration();

    Double getTotalVolume();
}
