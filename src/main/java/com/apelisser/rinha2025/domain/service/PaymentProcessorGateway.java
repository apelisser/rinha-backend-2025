package com.apelisser.rinha2025.domain.service;

import com.apelisser.rinha2025.domain.enums.PaymentProcessor;
import com.apelisser.rinha2025.infrastructure.paymentprocessor.PaymentProcessorClient;
import com.apelisser.rinha2025.domain.model.PaymentInput;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import static com.apelisser.rinha2025.domain.enums.PaymentProcessor.DEFAULT;
import static com.apelisser.rinha2025.domain.enums.PaymentProcessor.FALLBACK;

@Component
public class PaymentProcessorGateway {

    private final PaymentProcessorClient defaultPaymentProcessor;
    private final PaymentProcessorClient fallbackPaymentProcessor;
    private final ProcessorSelectionService processorSelectionService;

    public PaymentProcessorGateway(
            @Qualifier("defaultPaymentClient") PaymentProcessorClient defaultPaymentProcessor,
            @Qualifier("fallbackPaymentClient") PaymentProcessorClient fallbackPaymentProcessor,
            ProcessorSelectionService processorSelectionService) {
        this.defaultPaymentProcessor = defaultPaymentProcessor;
        this.fallbackPaymentProcessor = fallbackPaymentProcessor;
        this.processorSelectionService = processorSelectionService;
    }

    public PaymentProcessor process(PaymentInput paymentRequest) {
        PaymentProcessor bestChoice = processorSelectionService.getBestProcessor();

        if (bestChoice == null) {
            return null;
        }

        return bestChoice == DEFAULT
            ? this.processWithDefault(paymentRequest)
            : this.processWithFallback(paymentRequest);
    }

    private PaymentProcessor processWithDefault(PaymentInput paymentRequest) {
        defaultPaymentProcessor.processPayment(paymentRequest);
        return DEFAULT;
    }

    private PaymentProcessor processWithFallback(PaymentInput paymentRequest) {
        fallbackPaymentProcessor.processPayment(paymentRequest);
        return FALLBACK;
    }

}
