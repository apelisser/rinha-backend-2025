package com.apelisser.rinha2025.config.scheduler;

import com.apelisser.rinha2025.service.ProcessorSelectionService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CalculateBestProcessorScheduler {

    private final ProcessorSelectionService processorSelectionService;

    public CalculateBestProcessorScheduler(ProcessorSelectionService processorSelectionService) {
        this.processorSelectionService = processorSelectionService;
    }

    @Scheduled(
        fixedDelayString = "${best-processor-selection.scheduler.interval.ms}",
        initialDelayString = "${best-processor-selection.scheduler.initial-delay.ms}")
    public void execute() {
        processorSelectionService.updateBestProcessor();
    }


}
