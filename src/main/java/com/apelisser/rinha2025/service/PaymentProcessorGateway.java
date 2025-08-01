package com.apelisser.rinha2025.service;

import com.apelisser.rinha2025.enums.PaymentProcessor;
import com.apelisser.rinha2025.infrastructure.paymentprocessor.PaymentProcessorClient;
import com.apelisser.rinha2025.model.PaymentInput;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

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
            bestChoice = PaymentProcessor.DEFAULT;
        }

        if (bestChoice == PaymentProcessor.DEFAULT) {
            try {
                return processWithDefault(paymentRequest);
            } catch (Exception e) {
                return processWithFallback(paymentRequest);
            }
        } else {
            try {
                return processWithFallback(paymentRequest);
            } catch (Exception e) {
                return processWithDefault(paymentRequest);
            }
        }
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
