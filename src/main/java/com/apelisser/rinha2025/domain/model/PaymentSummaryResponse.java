package com.apelisser.rinha2025.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PaymentSummaryResponse(

    @JsonProperty("default")
    ProcessorSummary defaultProcessor,

    @JsonProperty("fallback")
    ProcessorSummary fallbackProcessor

) {}
