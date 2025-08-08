package com.apelisser.rinha2025.domain.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Table("health_check_status")
public record HealthCheckStatus(
    @Id
    boolean defaultProcessor,
    boolean isFailing,
    long minResponseTime,
    Instant lastChecked
) {}