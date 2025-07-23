package com.apelisser.rinha2025.model;

public record PaymentSummaryResponse(
    ProcessorSummary defaultProcessor,
    ProcessorSummary fallbackProcessor
) {}
