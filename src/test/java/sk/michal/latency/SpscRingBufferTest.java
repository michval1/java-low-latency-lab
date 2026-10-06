package sk.michal.latency;

import org.junit.jupiter.api.Test;

import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

class SpscRingBufferTest {
    @Test
    void validatesCapacityAndRejectsNull() {
        for (int capacity : new int[]{-1, 0, 1, 3}) {
            assertThrows(IllegalArgumentException.class, () -> new SpscRingBuffer<>(capacity));
        }
        assertThrows(NullPointerException.class, () -> new SpscRingBuffer<>(2).offer(null));
    }

    @Test
    void preservesOrderAtFullEmptyAndRepeatedWrapAround() {
        SpscRingBuffer<Integer> queue = new SpscRingBuffer<>(2);
        for (int round = 0; round < 100; round++) {
            assertNull(queue.poll());
            assertTrue(queue.offer(round * 2));
            assertTrue(queue.offer(round * 2 + 1));
            assertFalse(queue.offer(-1));
            assertEquals(round * 2, queue.poll());
            assertEquals(round * 2 + 1, queue.poll());
        }
        assertNull(queue.poll());
    }

    @Test
    void transfersOrderedValuesAcrossTwoThreads() throws Exception {
        SpscRingBuffer<Integer> queue = new SpscRingBuffer<>(1024);
        int count = 200000;
        ExecutorService workers = Executors.newFixedThreadPool(2);
        try {
            Future<?> producer = workers.submit(() -> {
                for (int i = 0; i < count; i++) {
                    while (!queue.offer(i)) {
                        if (Thread.currentThread().isInterrupted()) return;
                        Thread.onSpinWait();
                    }
                }
            });
            Future<?> consumer = workers.submit(() -> {
                for (int expected = 0; expected < count; expected++) {
                    Integer value;
                    while ((value = queue.poll()) == null) {
                        if (Thread.currentThread().isInterrupted()) return;
                        Thread.onSpinWait();
                    }
                    assertEquals(expected, value.intValue());
                }
            });
            producer.get(10, TimeUnit.SECONDS);
            consumer.get(10, TimeUnit.SECONDS);
            assertNull(queue.poll());
        } finally {
            workers.shutdownNow();
            assertTrue(workers.awaitTermination(3, TimeUnit.SECONDS));
        }
    }
}
