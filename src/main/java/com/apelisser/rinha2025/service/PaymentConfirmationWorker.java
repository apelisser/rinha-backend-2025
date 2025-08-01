package com.apelisser.rinha2025.service;

import com.apelisser.rinha2025.model.PaymentProcessed;
import com.apelisser.rinha2025.queue.ProcessedPaymentQueue;
import com.apelisser.rinha2025.util.ThreadUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentConfirmationWorker {

    @Value("${payment-confirmation.interval-retries-execution.ms}")
    private int intervalRetriesExecution;

    @Value("${payment-confirmation.number-of-retries}")
    private int attempts;

    private final PaymentService paymentService;
    private final ProcessedPaymentQueue processedPaymentQueue;

    public PaymentConfirmationWorker(PaymentService paymentService, ProcessedPaymentQueue processedPaymentQueue) {
        this.paymentService = paymentService;
        this.processedPaymentQueue = processedPaymentQueue;
    }

    public void process() {
        List<PaymentProcessed> payments = processedPaymentQueue.dequeue();
        this.confirmWithRetries(payments);
    }

    public void process(int maxQuantity) {
        List<PaymentProcessed> payments = processedPaymentQueue.dequeue(maxQuantity);
        this.confirmWithRetries(payments);
    }

    private void confirmWithRetries(List<PaymentProcessed> payments) {
        if (payments.isEmpty()) {
            return;
        }

        int internalAttempts = attempts <= 0 ? 1 : attempts + 1;

        while (internalAttempts > 0) {
            ThreadUtil.sleep(intervalRetriesExecution);
            try {
                paymentService.saveProcessedPayments(payments);
                internalAttempts = 0;
            } catch (Exception e) {
                internalAttempts--;
                if (internalAttempts == 0) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
