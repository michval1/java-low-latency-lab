package sk.michal.latency;

import org.openjdk.jmh.annotations.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.SplittableRandom;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 2)
@Measurement(iterations = 5, time = 2)
@Fork(3)
@State(Scope.Thread)
public class MessageDecodingBenchmark {
    @Param({"1024", "65536"})
    public int size;

    private String[] textMessages;
    private ByteBuffer[] binaryMessages;
    private int cursor;

    @Setup(Level.Trial)
    public void setup() {
        textMessages = new String[size];
        binaryMessages = new ByteBuffer[size];
        SplittableRandom random = new SplittableRandom(42);
        for (int i = 0; i < size; i++) {
            long orderId = 10001L + i;
            long price = random.nextLong(9000, 11000);
            long quantity = random.nextLong(1, 101);
            boolean buy = (i & 1) == 0;
            textMessages[i] = orderId + "," + price + "," + quantity + "," + (buy ? "BUY" : "SELL");
            ByteBuffer buffer = ByteBuffer.allocate(MarketMessageCodec.BINARY_BYTES)
                    .order(ByteOrder.BIG_ENDIAN);
            buffer.putLong(0, orderId);
            buffer.putLong(8, price);
            buffer.putLong(16, quantity);
            buffer.put(24, (byte) (buy ? 0 : 1));
            binaryMessages[i] = buffer;
        }
        cursor = 0;
    }

    private int nextIndex() {
        int index = cursor;
        cursor = index + 1 == size ? 0 : index + 1;
        return index;
    }

    @Benchmark
    public MarketMessageCodec.Message textDecode() {
        return MarketMessageCodec.decodeText(textMessages[nextIndex()]);
    }

    @Benchmark
    public MarketMessageCodec.Message binaryDecode() {
        return MarketMessageCodec.decodeBinary(binaryMessages[nextIndex()]);
    }
}
