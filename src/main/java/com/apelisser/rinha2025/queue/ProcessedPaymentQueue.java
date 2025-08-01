package com.apelisser.rinha2025.queue;

import com.apelisser.rinha2025.model.PaymentProcessed;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class ProcessedPaymentQueue {

    private static final BlockingQueue<PaymentProcessed> PAYMENT_PROCESSED_QUEUE = new LinkedBlockingQueue<>();

    public boolean enqueue(PaymentProcessed paymentProcessed) {
        return PAYMENT_PROCESSED_QUEUE.offer(paymentProcessed);
    }

    public List<PaymentProcessed> dequeue() {
        List<PaymentProcessed> processedPayments = new ArrayList<>();
        PAYMENT_PROCESSED_QUEUE.drainTo(processedPayments);
        return processedPayments;
    }

    public List<PaymentProcessed> dequeue(int maxQuantity) {
        List<PaymentProcessed> processedPayments = new ArrayList<>();
        PAYMENT_PROCESSED_QUEUE.drainTo(processedPayments, maxQuantity);
        return processedPayments;
    }

}
