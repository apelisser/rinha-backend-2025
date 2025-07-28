package com.apelisser.rinha2025.service;

import com.apelisser.rinha2025.enums.PaymentProcessor;
import com.apelisser.rinha2025.model.ProcessablePaymentEvent;
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

    @Value("${payment-processor.confirmation.number-of-retries}")
    private int numberOfRetries;

    private final ExecutorService virtualThreadExecutor;
    private final Consumer<List<ProcessablePaymentEvent>> processablePaymentConsumer;

    private final PaymentService paymentService;
    private final PaymentProcessorGateway paymentProcessorGateway;
    private final PaymentConfirmationService paymentConfirmationService;

    public PaymentProcessorService(
            PaymentService paymentService,
            PaymentProcessorGateway paymentProcessorGateway,
            @Value("${payment-processor.concurrency.type}") ProcessorType processorType,
            PaymentConfirmationService paymentConfirmationService) {
        this.paymentService = paymentService;
        this.paymentProcessorGateway = paymentProcessorGateway;

        this.virtualThreadExecutor = this.needInitializeExecutor(processorType)
            ? Executors.newVirtualThreadPerTaskExecutor()
            : null;

        this.processablePaymentConsumer = this.selectConsumer(processorType);
        this.paymentConfirmationService = paymentConfirmationService;
    }

    public void process(int maxQuantity) {
        List<ProcessablePaymentEvent> processablePayments = paymentService.findAndLockProcessablePayments(maxQuantity);
        processablePaymentConsumer.accept(processablePayments);
    }

    private void processPayment(ProcessablePaymentEvent processablePayment) {
        try {
            PaymentProcessor usedProcessor = paymentProcessorGateway.process(processablePayment.toProcessorRequest());
            this.confirmPayment(processablePayment.paymentId(), usedProcessor);
        } catch (Exception e) {
            paymentService.failPayment(processablePayment.paymentId());
        }
    }

    private void confirmPayment(Long paymentId, PaymentProcessor usedProcessor) {
        try {
            paymentConfirmationService.confirmPayment(paymentId, usedProcessor);
        } catch (Exception e) {
            if (numberOfRetries > 0) {
                paymentConfirmationService.confirmPaymentAsyncWithRetries(paymentId, usedProcessor, numberOfRetries);
            }
        }
    }

    private boolean needInitializeExecutor(ProcessorType processorType) {
        return processorType == ProcessorType.PARALLEL_VT || processorType == ProcessorType.PARALLEL_VT_NO_WAIT;
    }

    private Consumer<List<ProcessablePaymentEvent>> selectConsumer(ProcessorType processorType) {
        return switch (processorType) {
            case SEQUENTIAL -> this.sequentialConsumer();
            case PARALLEL -> this.parallelStreamConsumer();
            case PARALLEL_VT -> this.virtualThreadsConsumer();
            case PARALLEL_VT_NO_WAIT -> this.virtualThreadsNoWaitConsumer();
        };
    }

    public Consumer<List<ProcessablePaymentEvent>> sequentialConsumer() {
        return processablePayments -> processablePayments.forEach(this::processPayment);
    }

    public Consumer<List<ProcessablePaymentEvent>> parallelStreamConsumer() {
        return processablePayments -> processablePayments.parallelStream().forEach(this::processPayment);
    }

    public Consumer<List<ProcessablePaymentEvent>> virtualThreadsConsumer() {
        return processablePayments -> {
            List<Future<?>> futures = new ArrayList<>(processablePayments.size());
            for (ProcessablePaymentEvent payment : processablePayments) {
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

    public Consumer<List<ProcessablePaymentEvent>> virtualThreadsNoWaitConsumer() {
        return processablePayments -> {
            for (ProcessablePaymentEvent payment : processablePayments) {
                virtualThreadExecutor.submit(() -> processPayment(payment));
            }
        };
    }

}
