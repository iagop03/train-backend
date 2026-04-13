package com.train.repository;

import com.fasterxml.jackson.annotation.JsonProperty;

public interface VolumeByMuscleProjection {
    @JsonProperty("_id")
    String getMuscleGroup();

    Double getTotalVolume();
}
