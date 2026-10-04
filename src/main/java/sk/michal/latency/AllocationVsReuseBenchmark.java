package sk.michal.latency;

import org.openjdk.jmh.annotations.*;

import java.util.SplittableRandom;
import java.util.concurrent.TimeUnit;

/** One operation updates, publishes and processes one synthetic message. */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 2)
@Measurement(iterations = 5, time = 2)
@Fork(3)
@State(Scope.Thread)
public class AllocationVsReuseBenchmark {
    @Param({"1024", "65536"})
    public int size;

    private long[] orderIds;
    private long[] prices;
    private long[] quantities;
    private int cursor;
    private Message reusable;

    // Both paths publish through the same volatile field. This makes the object
    // escape, so the comparison includes actual allocation, not just source 'new'.
    volatile Message published;

    @Setup(Level.Trial)
    public void setup() {
        orderIds = new long[size];
        prices = new long[size];
        quantities = new long[size];
        SplittableRandom random = new SplittableRandom(42);
        for (int i = 0; i < size; i++) {
            orderIds[i] = 10001L + i;
            prices[i] = random.nextLong(9000, 11000);
            quantities[i] = random.nextLong(1, 101);
        }
        cursor = 0;
        reusable = new Message();
        published = null;
    }

    private int nextIndex() {
        int index = cursor;
        cursor = index + 1 == size ? 0 : index + 1;
        return index;
    }

    @Benchmark
    public long allocatePerMessage() {
        int index = nextIndex();
        Message message = new Message();
        message.update(orderIds[index], prices[index], quantities[index]);
        published = message;
        return message.checksum();
    }

    @Benchmark
    public long reuseMessage() {
        int index = nextIndex();
        reusable.update(orderIds[index], prices[index], quantities[index]);
        published = reusable;
        return reusable.checksum();
    }

    static final class Message {
        long orderId;
        long priceTicks;
        long quantity;

        void update(long orderId, long priceTicks, long quantity) {
            this.orderId = orderId;
            this.priceTicks = priceTicks;
            this.quantity = quantity;
        }

        long checksum() {
            return orderId ^ (priceTicks * quantity);
        }
    }
}
