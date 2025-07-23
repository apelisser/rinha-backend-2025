package com.apelisser.rinha2025.model;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentInput(
    UUID correlationId,
    BigDecimal amount
) {}
