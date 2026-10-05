package sk.michal.latency;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/** Controlled four-field format; not a general CSV or exchange protocol parser. */
public final class MarketMessageCodec {
    public static final int BINARY_BYTES = 25;

    private MarketMessageCodec() { }

    public record Message(long orderId, long priceTicks, long quantity, boolean buy) {
        public Message {
            if (orderId <= 0 || priceTicks < 0 || quantity <= 0) {
                throw new IllegalArgumentException("Invalid message values");
            }
        }
    }

    public static Message decodeText(String text) {
        String[] fields = text.split(",", -1);
        if (fields.length != 4) {
            throw new IllegalArgumentException("Expected four fields");
        }
        boolean buy = switch (fields[3]) {
            case "BUY" -> true;
            case "SELL" -> false;
            default -> throw new IllegalArgumentException("Invalid side");
        };
        return new Message(Long.parseLong(fields[0]), Long.parseLong(fields[1]),
                Long.parseLong(fields[2]), buy);
    }

    public static Message decodeBinary(ByteBuffer buffer) {
        if (buffer.limit() != BINARY_BYTES || buffer.order() != ByteOrder.BIG_ENDIAN) {
            throw new IllegalArgumentException("Expected 25-byte BIG_ENDIAN buffer");
        }
        byte side = buffer.get(24);
        if (side != 0 && side != 1) {
            throw new IllegalArgumentException("Invalid side");
        }
        return new Message(buffer.getLong(0), buffer.getLong(8), buffer.getLong(16), side == 0);
    }
}
