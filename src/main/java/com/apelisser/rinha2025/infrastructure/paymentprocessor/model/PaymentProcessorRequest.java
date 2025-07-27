package com.apelisser.rinha2025.infrastructure.paymentprocessor.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record PaymentProcessorRequest(
    UUID correlationId,
    BigDecimal amount,
    OffsetDateTime requestedAt
) {}
