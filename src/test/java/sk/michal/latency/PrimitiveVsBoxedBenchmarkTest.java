package sk.michal.latency;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PrimitiveVsBoxedBenchmarkTest {
    @ParameterizedTest
    @ValueSource(ints = {1024, 65536, 1000000})
    void bothRepresentationsProduceTheSameDeterministicSum(int size) {
        PrimitiveVsBoxedBenchmark benchmark = new PrimitiveVsBoxedBenchmark();
        benchmark.size = size;
        benchmark.setup();
        long expected = benchmark.primitivePath();
        assertTrue(expected >= 1000L * size && expected < 1000000L * size);
        assertEquals(expected, benchmark.boxedPath());
        benchmark.setup();
        assertEquals(expected, benchmark.primitivePath());
        assertEquals(expected, benchmark.boxedPath());
    }
}
