package com.apelisser.rinha2025.config.scheduler;

import com.apelisser.rinha2025.service.PaymentProcessorService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OutboxScheduler {

    @Value("${outbox-processor.pull.max-amount}")
    private int maxQuantity;

    private final PaymentProcessorService paymentProcessorService;

    public OutboxScheduler(PaymentProcessorService paymentProcessorService) {
        this.paymentProcessorService = paymentProcessorService;
    }

    @Scheduled(
        fixedDelayString = "${outbox-processor.scheduler.interval.ms}",
        initialDelayString = "${outbox-processor.scheduler.initial-delay.ms}")
    private void execute() {
        paymentProcessorService.process(maxQuantity);
    }

}
