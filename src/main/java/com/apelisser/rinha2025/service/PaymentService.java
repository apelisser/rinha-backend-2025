package com.apelisser.rinha2025.service;

import com.apelisser.rinha2025.model.AggregatedSummary;
import com.apelisser.rinha2025.model.PaymentProcessed;
import com.apelisser.rinha2025.model.PaymentSummaryResponse;
import com.apelisser.rinha2025.model.ProcessorSummary;
import com.apelisser.rinha2025.repository.PaymentRepository;
import com.apelisser.rinha2025.repository.PaymentRepositoryQueries;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentRepositoryQueries paymentRepositoryQueries;

    public PaymentService(PaymentRepository paymentRepository, PaymentRepositoryQueries paymentRepositoryQueries) {
        this.paymentRepository = paymentRepository;
        this.paymentRepositoryQueries = paymentRepositoryQueries;
    }

    @Transactional
    public int saveProcessedPayments(List<PaymentProcessed> payments) {
        return paymentRepositoryQueries.savePayments(payments);
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
