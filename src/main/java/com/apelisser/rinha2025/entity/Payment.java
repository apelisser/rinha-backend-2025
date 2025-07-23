package com.apelisser.rinha2025.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Table("payment")
public record Payment(
    @Id
    Long id,
    UUID correlationId,
    BigDecimal amount,
    boolean defaultProcessor,
    Instant requestedAt
) {

    public Payment(UUID correlationId, BigDecimal amount, boolean defaultProcessor, Instant requestedAt) {
        this(null, correlationId, amount, defaultProcessor, requestedAt);
    }

}
