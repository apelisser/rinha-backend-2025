package com.apelisser.rinha2025.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentApiRequest(
    UUID correlationId,
    BigDecimal amount,
    Instant requestedAt
) {}
