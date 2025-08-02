package com.apelisser.rinha2025.queue;

import com.apelisser.rinha2025.config.ProcessorProperties;
import com.apelisser.rinha2025.model.PaymentInput;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Service
public class InputPaymentQueue {

    private final BlockingQueue<PaymentInput> inputQueue;

    public InputPaymentQueue(ProcessorProperties processorProperties) {
        inputQueue = processorProperties.isFixedQueueSize()
            ? new ArrayBlockingQueue<>(processorProperties.getQueueSize(), true)
            : new LinkedBlockingQueue<>();
    }

    public boolean enqueue(PaymentInput paymentInput) {
        return inputQueue.offer(paymentInput);
    }

    public List<PaymentInput> dequeue() {
        List<PaymentInput> processedPayments = new ArrayList<>();
        inputQueue.drainTo(processedPayments);
        return processedPayments;
    }

    public List<PaymentInput> dequeue(int maxQuantity) {
        List<PaymentInput> processedPayments = new ArrayList<>();
        inputQueue.drainTo(processedPayments, maxQuantity);
        return processedPayments;
    }

}
