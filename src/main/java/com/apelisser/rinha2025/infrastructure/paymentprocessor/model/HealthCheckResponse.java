package com.apelisser.rinha2025.infrastructure.paymentprocessor.model;

public record HealthCheckResponse (
    boolean failing,
    int minResponseTime
) {}
