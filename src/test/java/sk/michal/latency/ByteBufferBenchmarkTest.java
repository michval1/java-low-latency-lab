package sk.michal.latency;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.SplittableRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ByteBufferBenchmarkTest {
    @ParameterizedTest
    @ValueSource(ints = {1, 1024, 65536})
    void buffersReadIdenticalInputsAndWriteTheExpectedSequence(int size) {
        ByteBufferBenchmark benchmark = new ByteBufferBenchmark();
        benchmark.size = size;
        benchmark.setup();
        long expected = new SplittableRandom(42).longs(size, 1000, 1000000).sum();
        assertEquals(expected, benchmark.heapRead());
        assertEquals(expected, benchmark.directRead());
        for (long base = 1001; base <= 1002; base++) {
            assertEquals(base + size - 1, benchmark.heapWrite());
            assertEquals(base + size - 1, benchmark.directWrite());
            long writtenSum = size * base + (long) size * (size - 1) / 2;
            assertEquals(writtenSum, benchmark.heapRead());
            assertEquals(writtenSum, benchmark.directRead());
        }
        benchmark.setup();
        assertEquals(expected, benchmark.heapRead());
        assertEquals(expected, benchmark.directRead());
    }
}
