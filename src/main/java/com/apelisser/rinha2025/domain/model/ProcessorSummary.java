package com.apelisser.rinha2025.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record ProcessorSummary(
    @JsonProperty
    long totalRequests,

    @JsonProperty
    BigDecimal totalAmount
) {}
