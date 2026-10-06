# Experiment 06: text versus binary message decoding

## Question and hypothesis (before measurement)

How do time and allocation differ between decoding a prepared four-field String and decoding the same values from a prepared 25-byte binary buffer?

Fixed-offset binary decoding is expected to be faster and allocate less than the chosen String.split/Long.parseLong implementation. Both return a new immutable Message record; neither is assumed allocation-free. The hypothesis applies to these implementations, not all text parsers.

## Method and format

Each operation decodes one message with order ID, price ticks, quantity and side. Setup generates equivalent data at sizes 1024 and 65536, with seed 42, alternating BUY/SELL, then prepares Strings and BIG_ENDIAN heap ByteBuffers.

Text format: `10001,10000,10,BUY`. This is controlled comma-separated input, not general quoted CSV. Binary layout: bytes 0–7 order ID, 8–15 price, 16–23 quantity, byte 24 side (0 BUY / 1 SELL), total 25 bytes. Absolute reads preserve buffer position. Validity checks reject invalid side, nonpositive ID/quantity and negative price. Binary decoding also validates limit and byte order.

Both methods advance a cursor and return the decoded immutable record for JMH consumption. String splitting and number parsing are measured; initial encoding, String creation, buffer allocation/wrapping and input generation are not. Binary input buffers are already prepared in setup. This is not UTF-8 byte-to-String conversion, network I/O, or a general production protocol.

Tests cover expected field values, cursor wrap-around, both sides and malformed inputs. Returned records and temporary text-parser allocations are assessed with the GC profiler rather than assumed from source code.

## Configuration and run

AverageTime, ns/message, one thread, three forks, three 2-second warmup iterations and five 2-second measurement iterations.

Build `clean package` through IntelliJ Maven, then:

```powershell
& "<JDK_HOME>\bin\java.exe" -jar ".\target\benchmarks.jar" MessageDecodingBenchmark -prof gc -rf json -rff results/message-decoding.json
```

Replace the placeholder locally. Four combinations take approximately three to four minutes plus overhead. Sanitize the JSON jvm path before committing.

## Environment and results

The recorded run contains four combinations with three forks each. Binary decoding means were 3.642 and 3.760 ns/message at sizes 1024 and 65536, versus 79.074 and 81.274 ns/message for text. Measured allocation was approximately 40 B/message for binary and 320 B/message for text. Thus this prepared-input binary decoder took about 21.6–21.7x less time and allocated one eighth as many bytes in this run. This compares fixed-offset decoding with the chosen split/parse implementation, not all text parsers. Numerical results are in results/message-decoding.json; the personal JVM path was sanitized. Hardware metadata and repeatability remain outstanding.

## Limitations

Prepared Strings and binary buffers have different retained memory layouts; both remain in state. Results include validation and cursor advancement. Binary is a fixed-size format; text is variable-length and uses a convenience parser rather than an optimized zero-allocation parser. No invalid-input performance, encoding performance, network throughput or p99 latency is measured. GC bytes/op does not measure retained input memory or direct/native memory.
