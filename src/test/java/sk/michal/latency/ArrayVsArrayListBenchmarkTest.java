package sk.michal.latency;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.SplittableRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ArrayVsArrayListBenchmarkTest {
    @ParameterizedTest
    @ValueSource(ints = {0, 1, 1024, 65536, 1000000})
    void bothContainersProduceTheExpectedSum(int size) {
        ArrayVsArrayListBenchmark benchmark = new ArrayVsArrayListBenchmark();
        benchmark.size = size;
        benchmark.setup();
        long expected = new SplittableRandom(42).longs(size, 1000, 1000000).sum();
        assertEquals(expected, benchmark.arrayIndexedSum());
        assertEquals(expected, benchmark.arrayListIndexedSum());
    }
}
