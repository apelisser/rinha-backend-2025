package com.apelisser.rinha2025.domain.service;

import com.apelisser.rinha2025.core.properties.ConfirmationProperties;
import com.apelisser.rinha2025.core.concurrency.SimpleTaskExecutor;
import com.apelisser.rinha2025.domain.model.PaymentProcessed;
import com.apelisser.rinha2025.domain.queue.ProcessedPaymentQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentProcessedService {

    private static final Logger log = LoggerFactory.getLogger(PaymentProcessedService.class);

    private final PaymentService paymentService;
    private final ProcessedPaymentQueue processedPaymentQueue;
    private final ConfirmationProperties processorProps;
    private final SimpleTaskExecutor taskExecutor;

    public PaymentProcessedService(
            PaymentService paymentService,
            ProcessedPaymentQueue processedPaymentQueue,
            ConfirmationProperties processorProps,
            SimpleTaskExecutor taskExecutor) {
        this.paymentService = paymentService;
        this.processedPaymentQueue = processedPaymentQueue;
        this.processorProps = processorProps;
        this.taskExecutor = taskExecutor;
    }

    public void process(int maxSize) {
        if (maxSize <= 0) {
            return;
        }

        List<PaymentProcessed> payments = processedPaymentQueue.dequeue(maxSize);
        if (!payments.isEmpty()) {
            processPayments(payments);
        }
    }

    private void processPayments(List<PaymentProcessed> payments) {
        taskExecutor.submit(() -> {
            try {
                paymentService.save(payments);
            } catch (Exception e) {
                tryRequeue(payments);
            }
        });
    }

    private void tryRequeue(List<PaymentProcessed> payments) {
        for (PaymentProcessed payment : payments) {
            if (payment.getRetries() < processorProps.getMaxRetries()) {
                log.info("Retrying persistence of payment {}", payment.getPaymentInput().getCorrelationId());
                payment.incrementRetries();
                taskExecutor.submit(() -> processedPaymentQueue.enqueue(payment));
            } else {
                log.warn("Dropping persistence of payment {}", payment.getPaymentInput().getCorrelationId());
            }
        }
    }

}
