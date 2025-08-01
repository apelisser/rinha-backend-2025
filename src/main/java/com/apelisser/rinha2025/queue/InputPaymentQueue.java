package com.apelisser.rinha2025.queue;

import com.apelisser.rinha2025.model.PaymentInput;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Service
public class InputPaymentQueue {

    private static final BlockingQueue<PaymentInput> INPUT_QUEUE = new LinkedBlockingQueue<>();

    public boolean enqueue(PaymentInput paymentInput) {
        return INPUT_QUEUE.offer(paymentInput);
    }

    public List<PaymentInput> dequeue() {
        List<PaymentInput> processedPayments = new ArrayList<>();
        INPUT_QUEUE.drainTo(processedPayments);
        return processedPayments;
    }

    public List<PaymentInput> dequeue(int maxQuantity) {
        List<PaymentInput> processedPayments = new ArrayList<>();
        INPUT_QUEUE.drainTo(processedPayments, maxQuantity);
        return processedPayments;
    }

}
