package com.apelisser.rinha2025.service;

import com.apelisser.rinha2025.enums.ProcessorType;
import com.apelisser.rinha2025.model.PaymentApiRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class PaymentProcessorGateway {

    private static final Logger log = LoggerFactory.getLogger(PaymentProcessorGateway.class);

    private final PaymentProcessorClient defaultPaymentProcessor;
    private final PaymentProcessorClient fallbackPaymentProcessor;

    public PaymentProcessorGateway(
            @Qualifier("defaultPaymentClient") PaymentProcessorClient defaultPaymentProcessor,
            @Qualifier("fallbackPaymentClient") PaymentProcessorClient fallbackPaymentProcessor) {
        this.defaultPaymentProcessor = defaultPaymentProcessor;
        this.fallbackPaymentProcessor = fallbackPaymentProcessor;
    }

    @CircuitBreaker(name = "default-processor", fallbackMethod = "processWithFallback")
    public ProcessorType process(PaymentApiRequest paymentRequest) {
        log.debug("Processing payment using default processor..");
        return this.processWithDefault(paymentRequest);
    }

    @CircuitBreaker(name = "fallback-processor")
    private ProcessorType processWithFallback(PaymentApiRequest paymentRequest, Throwable throwable) {
        log.debug("Processing payment using fallback processor..");
        return this.processWithFallback(paymentRequest);
    }

    private ProcessorType processWithDefault(PaymentApiRequest paymentRequest) {
        defaultPaymentProcessor.processPayment(paymentRequest);
        return ProcessorType.DEFAULT;
    }

    private ProcessorType processWithFallback(PaymentApiRequest paymentRequest) {
        fallbackPaymentProcessor.processPayment(paymentRequest);
        return ProcessorType.FALLBACK;
    }

}
