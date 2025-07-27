package com.apelisser.rinha2025.entity;

import com.apelisser.rinha2025.enums.ProcessorType;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;

@Table("health_check_status")
public record HealthCheckStatus(
    @Id
    ProcessorType processorName,
    boolean isFailing,
    long minResponseTime,
    OffsetDateTime lastChecked
) {}