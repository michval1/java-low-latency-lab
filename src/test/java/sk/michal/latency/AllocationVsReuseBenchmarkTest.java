package sk.michal.latency;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.SplittableRandom;

import static org.junit.jupiter.api.Assertions.*;

class AllocationVsReuseBenchmarkTest {
    @ParameterizedTest
    @ValueSource(ints = {1, 1024, 65536})
    void bothPathsProcessIdenticalMessagesAndWrapAround(int size) {
        AllocationVsReuseBenchmark allocating = new AllocationVsReuseBenchmark();
        AllocationVsReuseBenchmark reusing = new AllocationVsReuseBenchmark();
        allocating.size = reusing.size = size;
        allocating.setup();
        reusing.setup();
        SplittableRandom random = new SplittableRandom(42);
        AllocationVsReuseBenchmark.Message previousNew = null;
        AllocationVsReuseBenchmark.Message reused = null;
        long firstChecksum = 0;
        for (int i = 0; i < size; i++) {
            long price = random.nextLong(9000, 11000);
            long quantity = random.nextLong(1, 101);
            long expected = (10001L + i) ^ (price * quantity);
            if (i == 0) firstChecksum = expected;
            assertEquals(expected, allocating.allocatePerMessage());
            assertEquals(expected, reusing.reuseMessage());
            assertEquals(price, reusing.published.priceTicks);
            assertEquals(quantity, reusing.published.quantity);
            assertEquals(10001L + i, reusing.published.orderId);
            assertNotSame(previousNew, allocating.published);
            if (reused != null) assertSame(reused, reusing.published);
            previousNew = allocating.published;
            reused = reusing.published;
        }
        assertEquals(firstChecksum, allocating.allocatePerMessage());
        assertEquals(firstChecksum, reusing.reuseMessage());
        assertSame(reused, reusing.published);
    }
}
