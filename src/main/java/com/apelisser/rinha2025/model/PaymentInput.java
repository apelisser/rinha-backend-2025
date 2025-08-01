package com.apelisser.rinha2025.model;

import java.time.Instant;
import java.util.UUID;

public class PaymentInput {

    private UUID correlationId;
    private float amount;
    private final Instant requestedAt = Instant.now();

    public UUID getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(UUID correlationId) {
        this.correlationId = correlationId;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public Instant getRequestedAt() {
        return requestedAt;
    }

}
