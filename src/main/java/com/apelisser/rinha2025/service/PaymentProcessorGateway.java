package com.apelisser.rinha2025.service;

import com.apelisser.rinha2025.enums.ProcessorType;
import com.apelisser.rinha2025.infrastructure.paymentprocessor.PaymentProcessorClient;
import com.apelisser.rinha2025.infrastructure.paymentprocessor.model.PaymentProcessorRequest;
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

    public ProcessorType process(PaymentProcessorRequest paymentRequest) {
        ProcessorType bestChoice = processorSelectionService.getBestProcessor();

        if (bestChoice == null) {
            bestChoice = ProcessorType.DEFAULT;
        }

        if (bestChoice == ProcessorType.DEFAULT) {
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

    private ProcessorType processWithDefault(PaymentProcessorRequest paymentRequest) {
        defaultPaymentProcessor.processPayment(paymentRequest);
        return ProcessorType.DEFAULT;
    }

    private ProcessorType processWithFallback(PaymentProcessorRequest paymentRequest) {
        fallbackPaymentProcessor.processPayment(paymentRequest);
        return ProcessorType.FALLBACK;
    }

}
