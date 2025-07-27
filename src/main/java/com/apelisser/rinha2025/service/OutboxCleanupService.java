package com.apelisser.rinha2025.service;

import com.apelisser.rinha2025.repository.OutboxEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OutboxCleanupService {

    private final OutboxEventRepository outboxEventRepository;

    public OutboxCleanupService(OutboxEventRepository outboxEventRepository) {
        this.outboxEventRepository = outboxEventRepository;
    }

    @Transactional
    public void cleanAlreadyProcessed() {
        outboxEventRepository.deleteAllAlreadyProcessed();
    }

}
