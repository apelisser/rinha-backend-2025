package com.apelisser.rinha2025.service;

import com.apelisser.rinha2025.enums.PaymentProcessor;
import com.apelisser.rinha2025.infrastructure.paymentprocessor.PaymentProcessorClient;
import com.apelisser.rinha2025.model.PaymentInput;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Optional;

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

    public Optional<PaymentProcessor> process(PaymentInput paymentRequest) {
        return processorSelectionService.getBestProcessor()
            .map(bestChoice -> bestChoice == PaymentProcessor.DEFAULT
                ? this.processWithDefault(paymentRequest)
                : this.processWithFallback(paymentRequest));
    }

    private PaymentProcessor processWithDefault(PaymentInput paymentRequest) {
        defaultPaymentProcessor.processPayment(paymentRequest);
        return PaymentProcessor.DEFAULT;
    }

    private PaymentProcessor processWithFallback(PaymentInput paymentRequest) {
        fallbackPaymentProcessor.processPayment(paymentRequest);
        return PaymentProcessor.FALLBACK;
    }

}
