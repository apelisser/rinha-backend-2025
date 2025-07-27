package com.apelisser.rinha2025.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;

@Table("outbox_event")
public record OutboxEvent(
    @Id
    Long id,
    Long paymentId,
    OffsetDateTime createdAt
) {

    public OutboxEvent(Long paymentId) {
        this(null, paymentId, OffsetDateTime.now());
    }

    public static OutboxEvent of(Long paymentId) {
        return new OutboxEvent(null, paymentId, OffsetDateTime.now());
    }

}
