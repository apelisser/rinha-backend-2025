package com.apelisser.rinha2025.domain.service;

import com.apelisser.rinha2025.domain.model.AggregatedSummary;
import com.apelisser.rinha2025.domain.model.PaymentProcessed;
import com.apelisser.rinha2025.domain.model.PaymentSummaryResponse;
import com.apelisser.rinha2025.domain.model.ProcessorSummary;
import com.apelisser.rinha2025.domain.repository.PaymentRepository;
import com.apelisser.rinha2025.domain.repository.PaymentRepositoryQueries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    @Value("${payment-persistence.copy-threshold}")
    private int copyThreshold;

    @Value("${payment-persistence.individual-threshold}")
    private int individualThreshold;

    private final PaymentRepository paymentRepository;
    private final PaymentRepositoryQueries paymentRepositoryQueries;

    public PaymentService(PaymentRepository paymentRepository, PaymentRepositoryQueries paymentRepositoryQueries) {
        this.paymentRepository = paymentRepository;
        this.paymentRepositoryQueries = paymentRepositoryQueries;
    }

    @Transactional
    public int save(List<PaymentProcessed> payments) {
        if (payments.size() < individualThreshold) {
            log.info("Saving {} payments individually", payments.size());
            return paymentRepositoryQueries.saveIndividually(payments);
        } else if (payments.size() < copyThreshold) {
            log.info("Saving {} payments with batch", payments.size());
            return paymentRepositoryQueries.batchSave(payments);
        } else {
            log.info("Saving {} payments with copy", payments.size());
            return paymentRepositoryQueries.saveWithPostgresCopy(payments);
        }
    }

    @Transactional
    public PaymentSummaryResponse getSummary(Instant from, Instant to) {
        AggregatedSummary summary;
        if (from != null && to != null) {
            summary = paymentRepository.getAggregatedSummaryBetween(from, to);
        } else {
            summary = paymentRepository.getFullAggregatedSummary();
        }

        ProcessorSummary defaultSummary = new ProcessorSummary(
            summary.defaultTotalRequests(),
            summary.defaultTotalAmount()
        );

        ProcessorSummary fallbackSummary = new ProcessorSummary(
            summary.fallbackTotalRequests(),
            summary.fallbackTotalAmount()
        );

        return new PaymentSummaryResponse(defaultSummary, fallbackSummary);
    }

}
