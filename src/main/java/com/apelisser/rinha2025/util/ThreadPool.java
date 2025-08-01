package com.apelisser.rinha2025.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;

public final class ThreadPool {

    private ThreadPool() {
        throw new IllegalStateException("Utility class");
    }

    public static List<Thread> initPool(Runnable runnable, int size, int intervalExecution) {
        return initPool(runnable, size, intervalExecution, null);
    }

    public static List<Thread> initPool(Runnable runnable, int size, int intervalExecution, Consumer<Throwable> errorHandler) {
        List<Thread> threads = new ArrayList<>();

        if (size > 0) {
            IntStream.range(0, size).forEach(i -> {
                Runnable finalRunnable = intervalExecution > 0
                    ? buildExecutionRunnable(runnable, intervalExecution, errorHandler)
                    : buildExecutionRunnable(runnable, errorHandler);
                Thread thread = Thread.ofVirtual().start(finalRunnable);
                threads.add(thread);
            });
        }
        return threads;
    }

    private static Runnable buildExecutionRunnable(Runnable runnable, Consumer<Throwable> errorHandler) {
        Runnable interval = () -> {}; // do nothing
        return buildExecutionRunnable(runnable, interval, errorHandler);
    }

    private static Runnable buildExecutionRunnable(Runnable runnable, int intervalExecution, Consumer<Throwable> errorHandler) {
        Runnable interval = () -> ThreadUtil.sleep(intervalExecution);
        return buildExecutionRunnable(runnable, interval, errorHandler);
    }

    private static Runnable buildExecutionRunnable(Runnable task, Runnable interval, Consumer<Throwable> errorHandler) {
        return () -> {
            while (true) {
                try {
                    task.run();
                    interval.run();
                } catch (Exception e) {
                    if (errorHandler != null) {
                        errorHandler.accept(e);
                    }
                }
            }
        };
    }

}
