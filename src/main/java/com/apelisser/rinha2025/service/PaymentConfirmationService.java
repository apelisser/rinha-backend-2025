package com.apelisser.rinha2025.service;

import com.apelisser.rinha2025.enums.PaymentProcessor;
import com.apelisser.rinha2025.util.ThreadUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class PaymentConfirmationService {

    @Value("${payment-processor.confirmation.interval-retries-execution.ms}")
    private int intervalRetriesExecution;

    private final PaymentService paymentService;

    public PaymentConfirmationService(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    public void confirmPayment(Long paymentId, PaymentProcessor processor) {
        paymentService.confirmPayment(paymentId, processor);
    }

    @Async
    public void confirmPaymentAsyncWithRetries(Long paymentId, PaymentProcessor processor, int attempts) {
        int internalAttempts = attempts <= 0 ? 1 : attempts;

        while (internalAttempts > 0) {
            ThreadUtil.sleep(intervalRetriesExecution);
            try {
                this.confirmPayment(paymentId, processor);
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
