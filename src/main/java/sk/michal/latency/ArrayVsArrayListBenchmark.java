package sk.michal.latency;

import org.openjdk.jmh.annotations.*;

import java.util.ArrayList;
import java.util.SplittableRandom;
import java.util.concurrent.TimeUnit;

/** Compares indexed traversal of containers holding the same Long objects. */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 2)
@Measurement(iterations = 5, time = 2)
@Fork(3)
@State(Scope.Thread)
public class ArrayVsArrayListBenchmark {
    @Param({"1024", "65536", "1000000"})
    public int size;

    private Long[] array;
    private ArrayList<Long> list;

    @Setup(Level.Trial)
    public void setup() {
        array = new Long[size];
        list = new ArrayList<>(size);
        SplittableRandom random = new SplittableRandom(42);
        for (int i = 0; i < size; i++) {
            Long value = random.nextLong(1000, 1000000);
            array[i] = value;
            list.add(value);
        }
    }

    @Benchmark
    public long arrayIndexedSum() {
        long sum = 0;
        for (int i = 0, length = array.length; i < length; i++) {
            sum += array[i];
        }
        return sum;
    }

    @Benchmark
    public long arrayListIndexedSum() {
        long sum = 0;
        for (int i = 0, length = list.size(); i < length; i++) {
            sum += list.get(i);
        }
        return sum;
    }
}
