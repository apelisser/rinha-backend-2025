package com.apelisser.rinha2025.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("payment")
public record Payment (
    @Id
    UUID correlationId,
    float amount,
    Instant requestedAt,
    boolean defaultProcessor
) {}
