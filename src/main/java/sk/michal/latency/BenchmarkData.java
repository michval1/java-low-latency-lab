package sk.michal.latency;

/**
 * Small deterministic fixture set for benchmark prototypes.
 *
 * Important:
 * For real JMH measurements, create/populate benchmark state in @Setup and avoid
 * file I/O inside @Benchmark methods. These values merely give you something
 * trading-like to work with before you generate larger arrays.
 */
public final class BenchmarkData {

    public static final long[] ORDER_IDS = {
            10001L, 10002L, 10003L, 10004L,
            10005L, 10006L, 10007L, 10008L
    };

    public static final long[] PRICE_TICKS = {
            10000L, 10001L, 9999L, 10005L,
            10003L, 10010L, 9998L, 10002L
    };

    public static final long[] QUANTITIES = {
            10L, 25L, 50L, 15L,
            80L, 5L, 100L, 30L
    };

    private BenchmarkData() {
    }
}
