package com.apelisser.rinha2025.domain.queue;

import org.jctools.queues.MpscArrayQueue;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractQueue<T> {

    private final MpscArrayQueue<T> queue;

    protected AbstractQueue(int queueSize) {
        queue = new MpscArrayQueue<>(queueSize);
    }

    public boolean enqueue(T element) {
        return queue.offer(element);
    }

    public List<T> dequeue(int maxSize) {
        int estimatedSize = Math.clamp(queue.size(), 1, maxSize);
        List<T> elements = new ArrayList<>(estimatedSize);

        T element;
        int collected = 0;

        while (collected < maxSize && (element = queue.poll()) != null) {
            elements.add(element);
            collected++;
        }

        return elements;
    }

    public int dequeue(T[] buffer, int maxSize) {
        T element;
        int collected = 0;

        while (collected < maxSize && (element = queue.poll()) != null) {
            buffer[collected++] = element;
        }

        return collected;
    }

}
