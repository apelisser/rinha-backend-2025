package com.apelisser.rinha2025.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

public class PaymentInput {

    @JsonProperty
    private UUID correlationId;

    @JsonProperty
    private float amount;

    @JsonProperty
    private final Instant requestedAt = Instant.now();

    @JsonIgnore
    private int retries = 0;

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

    public int getRetries() {
        return retries;
    }

    public void incrementRetries() {
        retries++;
    }

}
