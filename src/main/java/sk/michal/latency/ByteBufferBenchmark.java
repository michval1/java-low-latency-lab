package sk.michal.latency;

import org.openjdk.jmh.annotations.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.SplittableRandom;
import java.util.concurrent.TimeUnit;

/** One operation traverses an entire buffer, reading or writing long values. */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 2)
@Measurement(iterations = 5, time = 2)
@Fork(3)
@State(Scope.Thread)
public class ByteBufferBenchmark {
    @Param({"1024", "65536"})
    public int size;

    private ByteBuffer heap;
    private ByteBuffer direct;
    private long heapWriteSequence;
    private long directWriteSequence;

    @Setup(Level.Trial)
    public void setup() {
        int bytes = Math.multiplyExact(size, Long.BYTES);
        heap = ByteBuffer.allocate(bytes).order(ByteOrder.BIG_ENDIAN);
        direct = ByteBuffer.allocateDirect(bytes).order(ByteOrder.BIG_ENDIAN);
        SplittableRandom random = new SplittableRandom(42);
        for (int i = 0; i < size; i++) {
            long value = random.nextLong(1000, 1000000);
            heap.putLong(i * Long.BYTES, value);
            direct.putLong(i * Long.BYTES, value);
        }
        heapWriteSequence = directWriteSequence = 1000;
    }

    @Benchmark
    public long heapRead() {
        return sum(heap);
    }

    @Benchmark
    public long directRead() {
        return sum(direct);
    }

    private long sum(ByteBuffer buffer) {
        long sum = 0;
        for (int i = 0; i < size; i++) {
            sum += buffer.getLong(i * Long.BYTES);
        }
        return sum;
    }

    @Benchmark
    public long heapWrite() {
        return fill(heap, ++heapWriteSequence);
    }

    @Benchmark
    public long directWrite() {
        return fill(direct, ++directWriteSequence);
    }

    private long fill(ByteBuffer buffer, long base) {
        for (int i = 0; i < size; i++) {
            buffer.putLong(i * Long.BYTES, base + i);
        }
        // Readback is part of both write measurements; tests check every value
        // through the full-buffer sum after writing.
        return buffer.getLong((size - 1) * Long.BYTES);
    }
}
