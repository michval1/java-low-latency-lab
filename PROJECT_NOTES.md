# Engineering notes

## JMH runner

The first runner used Maven exec:java. JMH found the benchmark methods, but its forked JVM could not load org.openjdk.jmh.runner.ForkedMain. Maven still printed BUILD SUCCESS with an empty result table.

Packaging a standalone JAR with Maven Shade fixed the runner classpath. The JAR manifest points to org.openjdk.jmh.Main. Successful runs must have completed measurement iterations and actual result rows; Maven's exit message alone is insufficient.

## Primitive and boxed storage

The original benchmark added one to 123. Its boxed result was within the Long cache range, so it did not test a new wrapper allocation per operation. The baseline remains in results/ for reference; its workload differs from the current array sums.

The revised experiment prepares long[] and Long[] with identical deterministic values outside the guaranteed cache range. Both loops return a primitive sum. Construction is excluded; measured time includes traversal and unboxing where applicable.

## Array and ArrayList

Both containers reference the same Long objects. Comparing long[] directly with ArrayList<Long> would combine primitive/boxed representation with container access. This experiment instead compares indexed access while holding the values constant.

The largest case had overlapping confidence intervals. It needs an independent repeat run before treating the point estimate as a reliable difference.

## Lookup keys

The original lookup repeatedly used key 511. It was replaced with a 1024-query batch and sequential/random patterns. Map query keys are boxed in setup. OperationsPerInvocation normalizes time and allocation per lookup.

The repeated stream accesses a limited working set, even when the structure holds a million entries. Sequential queries cover a local block. Results therefore do not describe whole-dataset random traffic or cold caches. Direct indexing also requires a suitable dense integer key space.

## Message allocation

A new Message and a reused Message perform the same field updates and checksum calculation. Both paths publish through a volatile field, making the object escape and including publication overhead equally.

Fresh allocation measured about 40 B/message in this JVM configuration. Reuse is modeled with single-thread ownership. A concurrent consumer retaining the reused object would require a different lifetime/ownership design.

## Buffers

Heap and direct buffers have equal capacity, byte order and values. Allocation and initial page touching happen in setup. Absolute offsets avoid position management. Writes change their input sequence and include a final readback.

The heap read result was faster in these runs, but the cause was not established with assembly or hardware counters. Write intervals overlap. No file/socket I/O or buffer allocation cost was measured.

## Message formats

The controlled text format uses three decimal numbers and BUY/SELL. The binary format uses three big-endian longs plus one side byte. Both decoders validate fields and return an immutable record.

Text splitting and parsing are measured; preparing Strings and binary buffers is not. This compares the chosen implementations, not all possible text parsers or end-to-end network decoding.

## SPSC queue

The producer owns tail updates and the consumer owns head updates. Volatile index publication orders slot writes, reads and clearing. The ring rejects null and requires a power-of-two capacity. Tests include full/empty states, wrap-around and 200000 ordered transfers between two threads.

The benchmark compares nonblocking offer/poll with one producer and one consumer. Primary throughput counts attempts, including failures; EVENTS counters are aggregate counts. Successful polls are reported separately. A passing transfer test does not replace longer stress testing or a formal concurrency proof.

## Current verification

The latest available Surefire reports contain 37 passing cases with no failures or errors. All seven experiments have structured results and individual write-ups. Personal JVM paths are replaced with placeholders; numerical measurements are retained.

Hardware details are in BENCHMARK_ENVIRONMENT.md. Exact OS/Maven versions, load, power settings and independent repeatability remain incomplete. Timing alone has not established specific cache, JIT or allocation mechanisms.

## Next steps

1. Repeat the largest array/ArrayList case using a separate result file.
2. Record precise environment metadata alongside new runs.
3. Investigate selected differences with profiling.
4. Extend SPSC concurrency validation before adding optimizations.
