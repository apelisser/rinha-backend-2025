package com.apelisser.rinha2025.service;

import com.apelisser.rinha2025.enums.PaymentProcessor;
import com.apelisser.rinha2025.model.PaymentInput;
import com.apelisser.rinha2025.model.PaymentProcessed;
import com.apelisser.rinha2025.queue.InputPaymentQueue;
import com.apelisser.rinha2025.queue.ProcessedPaymentQueue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

@Service
public class PaymentProcessorService {

    public enum ProcessorType {
        SEQUENTIAL, PARALLEL, PARALLEL_VT, PARALLEL_VT_NO_WAIT
    }

    private final ExecutorService virtualThreadExecutor;
    private final Consumer<List<PaymentInput>> processablePaymentConsumer;

    private final PaymentProcessorGateway paymentProcessorGateway;
    private final InputPaymentQueue inputPaymentQueue;
    private final ProcessedPaymentQueue processedPaymentQueue;

    public PaymentProcessorService(
            PaymentProcessorGateway paymentProcessorGateway,
            @Value("${payment-processor.concurrency.type}") ProcessorType processorType,
        InputPaymentQueue inputPaymentQueue, ProcessedPaymentQueue processedPaymentQueue) {
        this.paymentProcessorGateway = paymentProcessorGateway;

        this.virtualThreadExecutor = this.needInitializeExecutor(processorType)
            ? Executors.newVirtualThreadPerTaskExecutor()
            : null;

        this.processablePaymentConsumer = this.selectConsumer(processorType);
        this.inputPaymentQueue = inputPaymentQueue;
        this.processedPaymentQueue = processedPaymentQueue;
    }

    public void process(int maxQuantity) {
        List<PaymentInput> processablePayments = inputPaymentQueue.dequeue(maxQuantity);
        processablePaymentConsumer.accept(processablePayments);
    }

    public void process() {
        List<PaymentInput> processablePayments = inputPaymentQueue.dequeue();
        processablePaymentConsumer.accept(processablePayments);
    }

    private void processPayment(PaymentInput paymentInput) {
        try {
            PaymentProcessor usedProcessor = paymentProcessorGateway.process(paymentInput);
            processedPaymentQueue.enqueue(new PaymentProcessed(paymentInput, usedProcessor));
        } catch (Exception e) {
            inputPaymentQueue.enqueue(paymentInput);
        }
    }

    private boolean needInitializeExecutor(ProcessorType processorType) {
        return processorType == ProcessorType.PARALLEL_VT || processorType == ProcessorType.PARALLEL_VT_NO_WAIT;
    }

    private Consumer<List<PaymentInput>> selectConsumer(ProcessorType processorType) {
        return switch (processorType) {
            case SEQUENTIAL -> this.sequentialConsumer();
            case PARALLEL -> this.parallelStreamConsumer();
            case PARALLEL_VT -> this.virtualThreadsConsumer();
            case PARALLEL_VT_NO_WAIT -> this.virtualThreadsNoWaitConsumer();
        };
    }

    public Consumer<List<PaymentInput>> sequentialConsumer() {
        return processablePayments -> processablePayments.forEach(this::processPayment);
    }

    public Consumer<List<PaymentInput>> parallelStreamConsumer() {
        return processablePayments -> processablePayments.parallelStream().forEach(this::processPayment);
    }

    public Consumer<List<PaymentInput>> virtualThreadsConsumer() {
        return processablePayments -> {
            List<Future<?>> futures = new ArrayList<>(processablePayments.size());
            for (PaymentInput payment : processablePayments) {
                Future<?> future = virtualThreadExecutor.submit(() -> processPayment(payment));
                futures.add(future);
            }

            for (Future<?> future : futures) {
                try {
                    future.get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    public Consumer<List<PaymentInput>> virtualThreadsNoWaitConsumer() {
        return processablePayments -> {
            for (PaymentInput payment : processablePayments) {
                virtualThreadExecutor.submit(() -> processPayment(payment));
            }
        };
    }

}
