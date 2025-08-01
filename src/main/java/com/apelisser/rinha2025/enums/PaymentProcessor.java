package com.apelisser.rinha2025.enums;

public enum PaymentProcessor {

    DEFAULT(true),
    FALLBACK(false);

    private final boolean defaultProcessor;

    PaymentProcessor(boolean defaultProcessor) {
        this.defaultProcessor = defaultProcessor;
    }

    public boolean isDefaultProcessor() {
        return defaultProcessor;
    }

}
