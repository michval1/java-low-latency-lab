package sk.michal.latency;

import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

/**
 * Starter benchmark comparing primitive and boxed arithmetic/storage.
 *
 * TODO:
 * Expand this benchmark so that it models a realistic hot-path operation.
 * Be careful not to benchmark dead code or constant-folded work.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3)
@Measurement(iterations = 5)
@Fork(1)
@State(Scope.Thread)
public class PrimitiveVsBoxedBenchmark {

    private long primitive = 123L;
    private Long boxed = 123L;

    @Benchmark
    public long primitivePath() {
        return primitive + 1L;
    }

    @Benchmark
    public Long boxedPath() {
        return boxed + 1L;
    }
}
