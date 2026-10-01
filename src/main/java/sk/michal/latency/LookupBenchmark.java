package sk.michal.latency;

import org.openjdk.jmh.annotations.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Compare indexed array lookup with hash lookup for a dense integer key space.
 *
 * TODO:
 * - populate larger data sets,
 * - benchmark random keys,
 * - explain why this comparison is only fair for certain problem shapes.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3)
@Measurement(iterations = 5)
@Fork(1)
@State(Scope.Thread)
public class LookupBenchmark {

    private final long[] array = new long[1024];
    private final Map<Integer, Long> map = new HashMap<>();
    private int key = 511;

    @Setup
    public void setup() {
        for (int i = 0; i < array.length; i++) {
            array[i] = i * 10L;
            map.put(i, i * 10L);
        }
    }

    @Benchmark
    public long arrayLookup() {
        return array[key];
    }

    @Benchmark
    public long mapLookup() {
        return map.get(key);
    }
}
