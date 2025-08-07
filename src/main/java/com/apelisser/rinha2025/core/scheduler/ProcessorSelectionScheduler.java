package com.apelisser.rinha2025.core.scheduler;

import com.apelisser.rinha2025.core.concurrency.TaskPoolManager;
import com.apelisser.rinha2025.domain.service.ProcessorSelectionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class ProcessorSelectionScheduler {

    @Value("${payment-processor-selection.scheduler-interval}")
    private long intervalInMillis;

    private final TaskPoolManager taskManager;
    private final ProcessorSelectionService processorSelectionService;

    public ProcessorSelectionScheduler(TaskPoolManager taskManager, ProcessorSelectionService processorSelectionService) {
        this.taskManager = taskManager;
        this.processorSelectionService = processorSelectionService;
    }

    @EventListener(ApplicationReadyEvent.class)
    void setUp() {
        taskManager.executeRecurring(
            processorSelectionService::updateBestProcessor,
            intervalInMillis,
            TimeUnit.MILLISECONDS);
    }

}
