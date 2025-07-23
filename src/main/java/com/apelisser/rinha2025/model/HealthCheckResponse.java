package com.apelisser.rinha2025.model;

public record HealthCheckResponse (
    boolean failing,
    int minResponseTime
) {}
