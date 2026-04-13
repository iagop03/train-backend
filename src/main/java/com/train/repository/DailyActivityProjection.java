package com.train.repository;

import com.fasterxml.jackson.annotation.JsonProperty;

public interface DailyActivityProjection {
    @JsonProperty("_id")
    String getDate();

    Integer getSessionCount();

    Integer getTotalMinutes();

    Double getAvgIntensity();
}
