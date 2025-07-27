package com.apelisser.rinha2025.config.scheduler;

import com.apelisser.rinha2025.service.OutboxCleanupService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CleanupOutboxScheduler {

    private final OutboxCleanupService outboxCleanupService;

    public CleanupOutboxScheduler(OutboxCleanupService outboxCleanupService) {
        this.outboxCleanupService = outboxCleanupService;
    }

    @Scheduled(
        fixedDelayString = "${outbox-cleaner.scheduler.interval.ms}",
        initialDelayString = "${outbox-cleaner.scheduler.initial-delay.ms}")
    public void execute() {
        outboxCleanupService.cleanAlreadyProcessed();
    }

}
