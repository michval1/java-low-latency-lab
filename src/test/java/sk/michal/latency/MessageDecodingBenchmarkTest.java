package sk.michal.latency;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.SplittableRandom;

import static org.junit.jupiter.api.Assertions.*;

class MessageDecodingBenchmarkTest {
    @ParameterizedTest
    @ValueSource(ints = {1, 1024, 65536})
    void decodersProduceExpectedMessagesAndWrapAround(int size) {
        MessageDecodingBenchmark text = new MessageDecodingBenchmark();
        MessageDecodingBenchmark binary = new MessageDecodingBenchmark();
        text.size = binary.size = size;
        text.setup();
        binary.setup();
        SplittableRandom random = new SplittableRandom(42);
        MarketMessageCodec.Message first = null;
        for (int i = 0; i < size; i++) {
            MarketMessageCodec.Message expected = new MarketMessageCodec.Message(10001L + i,
                    random.nextLong(9000, 11000), random.nextLong(1, 101), (i & 1) == 0);
            if (i == 0) first = expected;
            assertEquals(expected, text.textDecode());
            assertEquals(expected, binary.binaryDecode());
        }
        assertEquals(first, text.textDecode());
        assertEquals(first, binary.binaryDecode());
    }

    @ParameterizedTest
    @ValueSource(strings = {"1,2,3", "1,2,3,BUY,extra", "1,2,3,HOLD", "x,2,3,BUY",
            "0,2,3,BUY", "1,-2,3,SELL", "1,2,0,BUY", "1,2,3,"})
    void rejectsMalformedText(String text) {
        assertThrows(IllegalArgumentException.class, () -> MarketMessageCodec.decodeText(text));
    }

    @Test
    void rejectsMalformedBinary() {
        assertThrows(IllegalArgumentException.class,
                () -> MarketMessageCodec.decodeBinary(ByteBuffer.allocate(24)));
        ByteBuffer buffer = ByteBuffer.allocate(25);
        buffer.putLong(0, 1).putLong(8, 2).putLong(16, 3).put(24, (byte) 2);
        assertThrows(IllegalArgumentException.class, () -> MarketMessageCodec.decodeBinary(buffer));
        buffer.put(24, (byte) 0).putLong(16, 0);
        assertThrows(IllegalArgumentException.class, () -> MarketMessageCodec.decodeBinary(buffer));
        buffer.putLong(16, 3).order(ByteOrder.LITTLE_ENDIAN);
        assertThrows(IllegalArgumentException.class, () -> MarketMessageCodec.decodeBinary(buffer));
    }
}
