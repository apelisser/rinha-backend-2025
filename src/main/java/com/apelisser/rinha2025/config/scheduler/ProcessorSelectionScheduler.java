package com.apelisser.rinha2025.config.scheduler;

import com.apelisser.rinha2025.service.ProcessorSelectionService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ProcessorSelectionScheduler {

    private final ProcessorSelectionService processorSelectionService;

    public ProcessorSelectionScheduler(ProcessorSelectionService processorSelectionService) {
        this.processorSelectionService = processorSelectionService;
    }

    @Scheduled(
        fixedDelayString = "${payment-processor-selection.scheduler.interval.ms}",
        initialDelayString = "${payment-processor-selection.scheduler.initial-delay.ms}")
    public void execute() {
        processorSelectionService.updateBestProcessor();
    }


}
