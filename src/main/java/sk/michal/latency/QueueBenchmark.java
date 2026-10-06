package sk.michal.latency;

import org.openjdk.jmh.annotations.*;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 3, time = 2)
@Measurement(iterations = 5, time = 2)
@Fork(3)
public class QueueBenchmark {
    @State(Scope.Group)
    public static class Queues {
        @Param({"1024", "65536"})
        public int capacity;
        SpscRingBuffer<Long> ring;
        ArrayBlockingQueue<Long> blocking;
        final Long payload = 10001L;

        @Setup(Level.Iteration)
        public void setup() {
            ring = new SpscRingBuffer<>(capacity);
            blocking = new ArrayBlockingQueue<>(capacity);
        }
    }

    @AuxCounters(AuxCounters.Type.EVENTS)
    @State(Scope.Thread)
    public static class Events {
        public long offered;
        public long polled;
        public long full;
        public long empty;

        @Setup(Level.Iteration)
        public void reset() {
            offered = polled = full = empty = 0;
        }
    }

    @Benchmark @Group("ring") @GroupThreads(1)
    public boolean ringOffer(Queues queues, Events events) {
        boolean accepted = queues.ring.offer(queues.payload);
        if (accepted) events.offered++; else events.full++;
        return accepted;
    }

    @Benchmark @Group("ring") @GroupThreads(1)
    public Long ringPoll(Queues queues, Events events) {
        Long value = queues.ring.poll();
        if (value != null) events.polled++; else events.empty++;
        return value;
    }

    @Benchmark @Group("blocking") @GroupThreads(1)
    public boolean blockingOffer(Queues queues, Events events) {
        boolean accepted = queues.blocking.offer(queues.payload);
        if (accepted) events.offered++; else events.full++;
        return accepted;
    }

    @Benchmark @Group("blocking") @GroupThreads(1)
    public Long blockingPoll(Queues queues, Events events) {
        Long value = queues.blocking.poll();
        if (value != null) events.polled++; else events.empty++;
        return value;
    }
}
