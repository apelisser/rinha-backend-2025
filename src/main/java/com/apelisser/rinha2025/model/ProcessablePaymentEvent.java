package com.apelisser.rinha2025.model;

import com.apelisser.rinha2025.enums.PaymentStatus;
import com.apelisser.rinha2025.infrastructure.paymentprocessor.model.PaymentProcessorRequest;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ProcessablePaymentEvent(
    Long outboxEventId,
    Long paymentId,
    UUID correlationId,
    BigDecimal amount,
    PaymentStatus status,
    OffsetDateTime requestedAt
) {
    public PaymentProcessorRequest toProcessorRequest() {
         return new PaymentProcessorRequest(this.correlationId, this.amount, this.requestedAt);
    }
}
