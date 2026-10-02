package sk.michal.latency;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.SplittableRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LookupBenchmarkTest {
    @ParameterizedTest
    @CsvSource({"1,sequential", "1,random", "1024,sequential", "1024,random",
            "65536,sequential", "65536,random", "1000000,sequential", "1000000,random"})
    void lookupsReturnTheExpectedValues(int size, String pattern) {
        LookupBenchmark benchmark = new LookupBenchmark();
        benchmark.size = size;
        benchmark.accessPattern = pattern;
        benchmark.setup();
        SplittableRandom random = new SplittableRandom(42);
        int start = (size - Math.min(size, LookupBenchmark.LOOKUPS_PER_INVOCATION)) / 2;
        long expected = 0;
        for (int i = 0; i < LookupBenchmark.LOOKUPS_PER_INVOCATION; i++) {
            int key = pattern.equals("random") ? random.nextInt(size) : (start + i) % size;
            expected += key * 10L;
        }
        assertEquals(expected, benchmark.arrayLookup());
        assertEquals(expected, benchmark.mapLookup());
        benchmark.setup();
        assertEquals(expected, benchmark.arrayLookup());
        assertEquals(expected, benchmark.mapLookup());
    }
}
