package com.apelisser.rinha2025.core.worker;

import com.apelisser.rinha2025.core.properties.ConfirmationProperties;
import com.apelisser.rinha2025.core.task.TaskPoolManager;
import com.apelisser.rinha2025.domain.service.PaymentProcessedService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentProcessedWorker {

    private final PaymentProcessedService paymentConfirmationService;
    private final TaskPoolManager taskPoolManager;
    private final ConfirmationProperties confirmationProps;

    public PaymentProcessedWorker(PaymentProcessedService paymentConfirmationService,
            TaskPoolManager taskPoolManager, ConfirmationProperties confirmationProps) {
        this.paymentConfirmationService = paymentConfirmationService;
        this.taskPoolManager = taskPoolManager;
        this.confirmationProps = confirmationProps;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void setUp() {
        if (confirmationProps.getMaxSize() <= 0) {
            return;
        }

        taskPoolManager.startWorkerPool(
            this::processor,
            confirmationProps.getNumberOfWorkers(),
            confirmationProps.getWorkerIntervalMillis());
    }

    private Runnable processor() {
        return () -> paymentConfirmationService.process(confirmationProps.getMaxSize());
    }

}
