package com.apelisser.rinha2025.service;

import com.apelisser.rinha2025.enums.ProcessorType;
import com.apelisser.rinha2025.model.AggregatedSummary;
import com.apelisser.rinha2025.model.PaymentApiRequest;
import com.apelisser.rinha2025.model.PaymentInput;
import com.apelisser.rinha2025.model.PaymentSummaryResponse;
import com.apelisser.rinha2025.model.ProcessorSummary;
import com.apelisser.rinha2025.entity.Payment;
import com.apelisser.rinha2025.repository.PaymentRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class PaymentService {

    private final PaymentProcessorGateway paymentProcessorGateway;
    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentProcessorGateway paymentProcessorGateway, PaymentRepository paymentRepository) {
        this.paymentProcessorGateway = paymentProcessorGateway;
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public void processPayment(PaymentInput input) {
        PaymentApiRequest apiRequest = new PaymentApiRequest(
            input.correlationId(),
            input.amount(),
            Instant.now()
        );

        ProcessorType process = paymentProcessorGateway.process(apiRequest);

        Payment payment = new Payment(
            apiRequest.correlationId(),
            apiRequest.amount(),
            ProcessorType.DEFAULT == process,
            apiRequest.requestedAt()
        );

        paymentRepository.save(payment);
    }

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
