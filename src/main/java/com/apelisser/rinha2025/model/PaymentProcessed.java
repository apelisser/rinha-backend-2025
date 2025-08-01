package com.apelisser.rinha2025.model;

import com.apelisser.rinha2025.enums.PaymentProcessor;

public record PaymentProcessed (
    PaymentInput paymentInput,
    PaymentProcessor processedAt
) { }
