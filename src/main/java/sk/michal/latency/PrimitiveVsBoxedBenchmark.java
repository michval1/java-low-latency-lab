package sk.michal.latency;

import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;
import java.util.SplittableRandom;

/**
 * Sum identical values stored as primitives and boxed objects.
 * One operation is a full-array sum, not a single element access.
 * Arrays and wrappers are allocated during setup, outside measurement.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 2)
@Measurement(iterations = 5, time = 2)
@Fork(3)
@State(Scope.Thread)
public class PrimitiveVsBoxedBenchmark {

    @Param({"1024", "65536", "1000000"})
    public int size;

    private long[] primitives;
    private Long[] boxedValues;

    @Setup(Level.Trial)
    public void setup() {
        primitives = new long[size];
        boxedValues = new Long[size];
        SplittableRandom random = new SplittableRandom(42);
        for (int i = 0; i < size; i++) {
            long value = random.nextLong(1000, 1000000);
            primitives[i] = value;
            boxedValues[i] = value;
        }
    }

    @Benchmark
    public long primitivePath() {
        long sum = 0;
        for (long value : primitives) {
            sum += value;
        }
        return sum;
    }

    @Benchmark
    public long boxedPath() {
        long sum = 0;
        for (Long value : boxedValues) {
            sum += value;
        }
        return sum;
    }
}
