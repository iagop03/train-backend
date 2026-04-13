package com.train.repository;

import com.fasterxml.jackson.annotation.JsonProperty;

public interface TopExercisesProjection {
    @JsonProperty("_id")
    String getExerciseId();

    Integer getTotalReps();

    Double getTotalWeight();

    Integer getSessions();
}
