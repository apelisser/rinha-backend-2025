package com.apelisser.rinha2025.domain.service;

import com.apelisser.rinha2025.core.properties.ProcessorProperties;
import com.apelisser.rinha2025.core.task.SimpleTaskExecutor;
import com.apelisser.rinha2025.domain.enums.PaymentProcessor;
import com.apelisser.rinha2025.domain.model.PaymentInput;
import com.apelisser.rinha2025.domain.model.PaymentProcessed;
import com.apelisser.rinha2025.domain.queue.InputPaymentQueue;
import com.apelisser.rinha2025.domain.queue.ProcessedPaymentQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PaymentProcessorService {

    private static final Logger log = LoggerFactory.getLogger(PaymentProcessorService.class);

    private final PaymentInput[] buffer;

    private final PaymentProcessorGateway paymentProcessorGateway;
    private final InputPaymentQueue inputPaymentQueue;
    private final ProcessedPaymentQueue processedPaymentQueue;
    private final ProcessorProperties processorProps;
    private final SimpleTaskExecutor taskExecutor;

    public PaymentProcessorService(
            PaymentProcessorGateway paymentProcessorGateway,
            InputPaymentQueue inputPaymentQueue,
            ProcessedPaymentQueue processedPaymentQueue,
            ProcessorProperties processorProps,
            SimpleTaskExecutor taskExecutor) {
        this.paymentProcessorGateway = paymentProcessorGateway;
        this.inputPaymentQueue = inputPaymentQueue;
        this.processedPaymentQueue = processedPaymentQueue;
        this.processorProps = processorProps;
        this.taskExecutor = taskExecutor;

        this.buffer = new PaymentInput[processorProps.getMaxSize()];
    }

    public void process(int maxSize) {
        if (maxSize <= 0) {
            return;
        }

        int count = inputPaymentQueue.dequeue(buffer, maxSize);
        if (count > 0) {
            log.info("Processing {} payments", count);
            for (int i = 0; i < count; i++) {
                processPayment(buffer[i]);
            }
        }
    }

    private void processPayment(PaymentInput payment) {
        taskExecutor.submit(() -> {
            try {
                PaymentProcessor processor = paymentProcessorGateway.process(payment);
                if (processor != null) {
                    processedPaymentQueue.enqueue(new PaymentProcessed(payment, processor));
                } else {
                    tryRequeue(payment);
                }
            } catch (Exception e) {
                tryRequeue(payment);
            }
        });
    }

    private void tryRequeue(PaymentInput paymentInput) {
        if (paymentInput.getRetries() < processorProps.getMaxRetries()) {
            log.info("Retrying processing for payment {}", paymentInput.getCorrelationId());
            paymentInput.incrementRetries();
            inputPaymentQueue.enqueue(paymentInput);
        } else {
            log.warn("Dropping payment {}", paymentInput.getCorrelationId());
        }
    }

}
