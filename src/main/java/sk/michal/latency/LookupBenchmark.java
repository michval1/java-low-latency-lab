package sk.michal.latency;

import org.openjdk.jmh.annotations.*;

import java.util.HashMap;
import java.util.Map;
import java.util.SplittableRandom;
import java.util.concurrent.TimeUnit;

/**
 * Compare indexed array lookup with hash lookup for a dense integer key space.
 *
 * Keys are prepared before measurement. Both methods execute the same batch.
 * This compares complete representations, including map value unboxing.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 2)
@Measurement(iterations = 5, time = 2)
@Fork(3)
@State(Scope.Thread)
public class LookupBenchmark {

    public static final int LOOKUPS_PER_INVOCATION = 1024;

    @Param({"1024", "65536", "1000000"})
    public int size;

    @Param({"sequential", "random"})
    public String accessPattern;

    private long[] array;
    private Map<Integer, Long> map;
    private int[] keys;
    private Integer[] boxedKeys;

    @Setup
    public void setup() {
        if (size <= 0) {
            throw new IllegalArgumentException("size must be positive");
        }
        if (!"sequential".equals(accessPattern) && !"random".equals(accessPattern)) {
            throw new IllegalArgumentException("Unknown access pattern");
        }
        array = new long[size];
        map = new HashMap<>((int) Math.ceil(size / 0.75));
        for (int i = 0; i < array.length; i++) {
            array[i] = i * 10L;
            map.put(i, i * 10L);
        }
        keys = new int[LOOKUPS_PER_INVOCATION];
        boxedKeys = new Integer[LOOKUPS_PER_INVOCATION];
        SplittableRandom random = new SplittableRandom(42);
        int start = (size - Math.min(size, LOOKUPS_PER_INVOCATION)) / 2;
        for (int i = 0; i < keys.length; i++) {
            int key = "random".equals(accessPattern)
                    ? random.nextInt(size)
                    : (start + i) % size;
            keys[i] = key;
            boxedKeys[i] = key;
        }
    }

    @Benchmark
    @OperationsPerInvocation(LOOKUPS_PER_INVOCATION)
    public long arrayLookup() {
        long sum = 0;
        for (int key : keys) {
            sum += array[key];
        }
        return sum;
    }

    @Benchmark
    @OperationsPerInvocation(LOOKUPS_PER_INVOCATION)
    public long mapLookup() {
        long sum = 0;
        for (Integer key : boxedKeys) {
            sum += map.get(key);
        }
        return sum;
    }
}
