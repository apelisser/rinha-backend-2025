package com.apelisser.rinha2025.core.concurrency;

import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Component
public class SimpleTaskExecutor {

    private final ExecutorService executor;

    public SimpleTaskExecutor() {
        this.executor = Executors.newVirtualThreadPerTaskExecutor();
    }

    public Future<?> submit(Runnable task) {
        return executor.submit(task);
    }

    <T> Future<T> submit(Runnable task, T result) {
        return executor.submit(task, result);
    }

    public <T> CompletableFuture<T> submit(Supplier<T> task) {
        return CompletableFuture.supplyAsync(task, executor);
    }

    @PreDestroy
    public void shutdown() {
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
