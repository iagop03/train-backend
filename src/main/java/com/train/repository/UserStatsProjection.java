package com.train.repository;

public interface UserStatsProjection {
    Integer getTotalSessions();

    Integer getTotalMinutes();

    Double getAvgIntensity();

    Integer getMaxIntensity();

    Integer getMinIntensity();
}
