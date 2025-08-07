package com.apelisser.rinha2025.core.concurrency;

import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Component
public class TaskPoolManager {

    private static final Logger log = LoggerFactory.getLogger(TaskPoolManager.class);

    private final ScheduledExecutorService scheduler;
    private final ExecutorService executor;
    private volatile boolean shutdown = false;

    public TaskPoolManager() {
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.executor = Executors.newVirtualThreadPerTaskExecutor();
    }

    public List<CompletableFuture<Void>> executeOnce(Runnable task, int poolSize) {
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (int i = 0; i < poolSize; i++) {
            CompletableFuture<Void> future = CompletableFuture
                .runAsync(task, executor)
                .exceptionally(throwable -> {
                    log.error("Task failed", throwable);
                    return null;
                });
            futures.add(future);
        }

        return futures;
    }

    public ScheduledFuture<?> executeRecurring(Runnable task, long interval, TimeUnit unit) {
        Runnable safeTask = () -> {
            if (!shutdown) {
                try {
                    task.run();
                } catch (Exception e) {
                    log.error("Recurring task failed", e);
                }
            }
        };

        return scheduler.scheduleWithFixedDelay(safeTask, 0, interval, unit);
    }

    public void startWorkerPool(Supplier<Runnable> taskSupplier, int poolSize, long intervalMs) {
        for (int i = 0; i < poolSize; i++) {
            executor.submit(() -> {
                while (!shutdown && !Thread.currentThread().isInterrupted()) {
                    try {
                        Runnable task = taskSupplier.get();
                        if (task != null) {
                            task.run();
                        }

                        if (intervalMs > 0) {
                            Thread.sleep(intervalMs);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    } catch (Exception e) {
                        log.error("Worker task failed", e);
                    }
                }
            });
        }
    }

    @PreDestroy
    public void shutdown() {
        shutdown = true;
        scheduler.shutdown();
        executor.shutdown();

        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
