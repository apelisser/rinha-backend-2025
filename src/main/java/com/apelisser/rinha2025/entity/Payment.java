package com.apelisser.rinha2025.entity;

import com.apelisser.rinha2025.enums.PaymentStatus;
import com.apelisser.rinha2025.enums.PaymentProcessor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Table("payment")
public record Payment(
    @Id
    Long id,
    UUID correlationId,
    BigDecimal amount,
    PaymentProcessor processor,
    PaymentStatus status,
    OffsetDateTime requestedAt
) {

    public Payment(UUID correlationId, BigDecimal amount) {
        this(null, correlationId, amount, null, PaymentStatus.PENDING, OffsetDateTime.now());
    }

}
