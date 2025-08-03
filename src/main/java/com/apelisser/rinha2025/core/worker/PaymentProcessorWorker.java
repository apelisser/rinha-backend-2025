package com.apelisser.rinha2025.core.worker;

import com.apelisser.rinha2025.core.properties.ProcessorProperties;
import com.apelisser.rinha2025.core.task.TaskPoolManager;
import com.apelisser.rinha2025.core.util.ThreadUtil;
import com.apelisser.rinha2025.domain.service.PaymentProcessorService;
import com.apelisser.rinha2025.domain.service.health_check.HealthStatusHolder;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentProcessorWorker {

    private final TaskPoolManager taskPoolManager;
    private final ProcessorProperties processorProps;
    private final PaymentProcessorService paymentProcessorService;
    private final HealthStatusHolder healthStatusHolder;

    public PaymentProcessorWorker(TaskPoolManager taskPoolManager, ProcessorProperties processorProps,
            PaymentProcessorService paymentProcessorService, HealthStatusHolder healthStatusHolder) {
        this.taskPoolManager = taskPoolManager;
        this.processorProps = processorProps;
        this.paymentProcessorService = paymentProcessorService;
        this.healthStatusHolder = healthStatusHolder;
    }

    @EventListener(ApplicationReadyEvent.class)
    private void setUp() {
        if (processorProps.getMaxSize() <= 0) {
            return;
        }

        taskPoolManager.startWorkerPool(
            this::processor,
            processorProps.getNumberOfWorkers(),
            processorProps.getWorkerIntervalMillis());
    }

    private Runnable processor() {
        return () -> {
            if (allProcessorsIsFailing()) {
                return;
            }

            int size = processorProps.getMaxSize();

            if (canReduceThroughput()) {
                size = calculateSize(processorProps.getMaxSize());

                if (processorProps.getExtraWorkerIntervalMillis() > 0) {
                    ThreadUtil.sleep(processorProps.getExtraWorkerIntervalMillis());
                }
            }

            paymentProcessorService.process(size);
        };
    }

    private int calculateSize(int maxSize) {
        if (processorProps.getReductionPercentage() >= 1) {
            return 0;
        }

        if (processorProps.getReductionPercentage() > 0) {
            float reduction = processorProps.getReductionPercentage();
            int size = maxSize - (int) (maxSize * reduction);
            return Math.min(1, size);
        }

        return maxSize;
    }

    private boolean allProcessorsIsFailing() {
        return healthStatusHolder.isDefaultFailing()
            && healthStatusHolder.isFallbackFailing();
    }

    private boolean canReduceThroughput() {
        return onlyDefaultProcessorIsFailing()
            && processorProps.isFallbackEnabled();
    }

    private boolean onlyDefaultProcessorIsFailing() {
        return healthStatusHolder.isDefaultFailing()
            && !healthStatusHolder.isFallbackFailing();
    }

}
