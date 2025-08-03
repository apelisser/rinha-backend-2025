package com.apelisser.rinha2025.domain.model;

import java.math.BigDecimal;

public record AggregatedSummary(
   long defaultTotalRequests,
   BigDecimal defaultTotalAmount,
   long fallbackTotalRequests,
   BigDecimal fallbackTotalAmount
) {}
