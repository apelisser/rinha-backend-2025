package com.apelisser.rinha2025.service;

import com.apelisser.rinha2025.enums.ProcessorType;
import com.apelisser.rinha2025.model.ProcessablePaymentEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentProcessorService {

    private static final Logger log = LoggerFactory.getLogger(PaymentProcessorService.class);

    private final PaymentService paymentService;
    private final PaymentProcessorGateway paymentProcessorGateway;

    public PaymentProcessorService(PaymentService paymentService, PaymentProcessorGateway paymentProcessorGateway) {
        this.paymentService = paymentService;
        this.paymentProcessorGateway = paymentProcessorGateway;
    }

    public void process(int maxQuantity) {
        List<ProcessablePaymentEvent> processablePayments = paymentService.findAndLockProcessablePayments(maxQuantity);
        processablePayments.forEach(this::processPayment);
    }

    private void processPayment(ProcessablePaymentEvent processablePayment) {
        try {
            ProcessorType usedProcessor = paymentProcessorGateway.process(processablePayment.toProcessorRequest());
            paymentService.confirmPayment(processablePayment.paymentId(), usedProcessor);
        } catch (Exception e) {
            paymentService.failPayment(processablePayment.paymentId());
            log.error("Error processing payment {}. Error message: {}", processablePayment.paymentId(), e.getMessage());
        }
    }

}
