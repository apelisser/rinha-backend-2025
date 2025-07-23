package com.apelisser.rinha2025.model;

import java.math.BigDecimal;

public record ProcessorSummary(
    long totalRequests,
    BigDecimal totalAmount
) {}
