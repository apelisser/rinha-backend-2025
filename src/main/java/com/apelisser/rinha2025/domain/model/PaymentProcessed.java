package com.apelisser.rinha2025.domain.model;

import com.apelisser.rinha2025.domain.enums.PaymentProcessor;

public class PaymentProcessed {

    private final PaymentInput paymentInput;
    private final PaymentProcessor processor;
    private int retries = 0;

    public PaymentProcessed(PaymentInput paymentInput, PaymentProcessor processor) {
        this.paymentInput = paymentInput;
        this.processor = processor;
    }

    public PaymentInput getPaymentInput() {
        return paymentInput;
    }

    public PaymentProcessor getProcessor() {
        return processor;
    }

    public int getRetries() {
        return retries;
    }

    public void incrementRetries() {
        retries++;
    }

}
