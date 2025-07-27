package com.apelisser.rinha2025.service;

import com.apelisser.rinha2025.entity.OutboxEvent;
import com.apelisser.rinha2025.entity.Payment;
import com.apelisser.rinha2025.enums.PaymentStatus;
import com.apelisser.rinha2025.enums.PaymentProcessor;
import com.apelisser.rinha2025.model.AggregatedSummary;
import com.apelisser.rinha2025.model.PaymentInput;
import com.apelisser.rinha2025.model.PaymentSummaryResponse;
import com.apelisser.rinha2025.model.ProcessablePaymentEvent;
import com.apelisser.rinha2025.model.ProcessorSummary;
import com.apelisser.rinha2025.repository.OutboxEventRepository;
import com.apelisser.rinha2025.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OutboxEventRepository outboxEventRepository;

    public PaymentService(PaymentRepository paymentRepository, OutboxEventRepository outboxEventRepository) {
        this.paymentRepository = paymentRepository;
        this.outboxEventRepository = outboxEventRepository;
    }

    @Transactional
    public void createIntention(PaymentInput input) {
        Payment payment = new Payment(
            input.correlationId(),
            input.amount());

        Payment paymentIntention = paymentRepository.save(payment);
        outboxEventRepository.save(OutboxEvent.of(paymentIntention.id()));
    }

    @Transactional
    public List<ProcessablePaymentEvent> findAndLockProcessablePayments(int maxQuantity) {
        List<ProcessablePaymentEvent> processableEvents = outboxEventRepository.findAndLockProcessableEvents(maxQuantity);

        if (!processableEvents.isEmpty()) {
            List<Long> paymentIds = processableEvents.stream().map(ProcessablePaymentEvent::paymentId).toList();
            paymentRepository.updateStatus(paymentIds, PaymentStatus.PROCESSING);
        }

        return processableEvents;
    }

    @Transactional
    public void confirmPayment(Long paymentId, PaymentProcessor usedProcessor) {
        paymentRepository.confirm(paymentId, usedProcessor);
        outboxEventRepository.deleteByPaymentId(paymentId);
    }

    public void failPayment(Long paymentId) {
        paymentRepository.updateStatus(paymentId, PaymentStatus.FAILED);
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
