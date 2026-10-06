package sk.michal.latency;

import java.util.Objects;

/** Bounded FIFO: exactly one producer and one consumer; null is the empty marker. */
public final class SpscRingBuffer<E> {
    private final Object[] slots;
    private final int mask;
    private volatile long head;
    private volatile long tail;

    public SpscRingBuffer(int capacity) {
        if (capacity < 2 || (capacity & (capacity - 1)) != 0) {
            throw new IllegalArgumentException("Capacity must be a power of two, at least 2");
        }
        slots = new Object[capacity];
        mask = capacity - 1;
    }

    /** Called exclusively by the producer. Returns false when full. */
    public boolean offer(E element) {
        Objects.requireNonNull(element);
        long currentTail = tail;
        if (currentTail - head == slots.length) return false;
        slots[(int) currentTail & mask] = element;
        // Publish only after writing the slot; the consumer observes tail.
        tail = currentTail + 1;
        return true;
    }

    /** Called exclusively by the consumer. Returns null when empty. */
    @SuppressWarnings("unchecked")
    public E poll() {
        long currentHead = head;
        if (currentHead == tail) return null;
        int index = (int) currentHead & mask;
        E element = (E) slots[index];
        slots[index] = null;
        // Release the slot only after reading and clearing it.
        head = currentHead + 1;
        return element;
    }
}
