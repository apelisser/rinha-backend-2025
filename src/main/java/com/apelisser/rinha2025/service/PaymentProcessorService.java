package com.apelisser.rinha2025.service;

import com.apelisser.rinha2025.config.ProcessorProperties;
import com.apelisser.rinha2025.enums.PaymentProcessor;
import com.apelisser.rinha2025.model.PaymentInput;
import com.apelisser.rinha2025.model.PaymentProcessed;
import com.apelisser.rinha2025.queue.InputPaymentQueue;
import com.apelisser.rinha2025.queue.ProcessedPaymentQueue;
import com.apelisser.rinha2025.service.health_check.HealthStatusHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
    private final HealthStatusHolder healthStatusHolder;
    private final ProcessorProperties processorProperties;

    public PaymentProcessorService(
            PaymentProcessorGateway paymentProcessorGateway,
            InputPaymentQueue inputPaymentQueue,
            ProcessedPaymentQueue processedPaymentQueue,
            HealthStatusHolder healthStatusHolder,
            ProcessorProperties processorProperties) {
        this.paymentProcessorGateway = paymentProcessorGateway;

        this.virtualThreadExecutor = this.needInitializeExecutor(processorProperties.getProcessorType())
            ? Executors.newVirtualThreadPerTaskExecutor()
            : null;

        this.processablePaymentConsumer = this.selectConsumer(processorProperties.getProcessorType());
        this.inputPaymentQueue = inputPaymentQueue;
        this.processedPaymentQueue = processedPaymentQueue;
        this.healthStatusHolder = healthStatusHolder;
        this.processorProperties = processorProperties;
    }

    public void process(int maxQuantity) {
        if (allProcessorsIsFailing()) return;
        List<PaymentInput> processablePayments = inputPaymentQueue.dequeue(this.calculateMaxQuantity(maxQuantity));
        processablePaymentConsumer.accept(processablePayments);
    }

    public void process() {
        if (allProcessorsIsFailing()) return;
        List<PaymentInput> processablePayments = inputPaymentQueue.dequeue();
        processablePaymentConsumer.accept(processablePayments);
    }

    private int calculateMaxQuantity(int maxQuantity) {
        if (this.needReduceQuantity()) {
            float reduction = processorProperties.getReductionPercentage();
            return maxQuantity - (int) (maxQuantity * reduction);
        }
        return maxQuantity;
    }

    private boolean needReduceQuantity() {
        return this.onlyDefaultProcessorIsFailing()
            && processorProperties.isFallbackEnabled()
            && processorProperties.hasReductionWhenDefaultIsOut();
    }

    private void processPayment(PaymentInput paymentInput) {
        try {
            Optional<PaymentProcessor> usedProcessor = paymentProcessorGateway.process(paymentInput);

            if (usedProcessor.isPresent()) {
                processedPaymentQueue.enqueue(new PaymentProcessed(paymentInput, usedProcessor.get()));
            } else {
                this.requeuePayment(paymentInput);
            }
        } catch (Exception e) {
            this.requeuePayment(paymentInput);
        }
    }

    private void requeuePayment(PaymentInput paymentInput) {
        if (paymentInput.getRetries() < processorProperties.getMaxRetries()) {
            paymentInput.incrementRetries();
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

    private boolean onlyDefaultProcessorIsFailing() {
        return healthStatusHolder.isDefaultFailing()
            && !healthStatusHolder.isFallbackFailing();
    }

    private boolean allProcessorsIsFailing() {
        return healthStatusHolder.isDefaultFailing()
            && healthStatusHolder.isFallbackFailing();
    }

}
