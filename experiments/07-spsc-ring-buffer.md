# Experiment 07: ArrayBlockingQueue versus SPSC ring buffer

## Question and hypothesis (before measurement)

With exactly one producer and one consumer, how do nonblocking offer/poll throughput and successful transfers differ between ArrayBlockingQueue and a bounded SPSC ring?

The specialized ring may have higher successful-transfer throughput because it avoids the general queue's lock. This is a hypothesis. Results may be dominated by failed offers/polls, thread scheduling, cache coherence and counter overhead.

## Correctness and ownership

The ring requires a power-of-two capacity of at least two and rejects null. The producer exclusively owns tail updates; the consumer owns head updates. Volatile tail publication follows slot writes, and volatile head publication follows slot clearing. No overwrite occurs while full; polls return null when empty. Tests cover FIFO ordering, full/empty states, repeated wrap-around and 200000 ordered transfers on two threads. This is a teaching implementation, not a production-validated queue or MPSC/MPMC implementation.

## Benchmark method

Each JMH group has exactly two threads: one producer calling offer and one consumer calling poll. Scope.Group state is shared within that group. Capacities are 1024 and 65536. Each iteration starts with fresh empty queues. Both paths reuse one immutable Long payload created before measurement.

ArrayBlockingQueue uses default fairness and its nonblocking offer/poll API, not put/take. Ring offer/poll also return immediately on full/empty. Methods do not internally retry. JMH invokes them repeatedly. Events counters report accepted offers, successful polls, full returns and empty returns separately.

The primary group throughput counts method attempts, including failures and both sides of a transfer. It is NOT messages transferred per second. Inspect successful polled event counts as the completed-consumption metric, along with offered/full/empty counts and method sub-results. In this configuration JMH EVENTS counters are reported as aggregate counts (#), not ops/s; normalize using measured durations only when explicitly calculating a rate. Counter updates and JMH overhead are included. The producer may run ahead by up to queue capacity; each iteration's reset discards remaining elements. No lossless-stream or latency result is inferred from primary throughput.

## Configuration and run

Throughput, seconds, three forks, three 2-second warmup and five 2-second measurement iterations. Let group annotations configure threads; do not override with an arbitrary thread count.

Build with `clean package` in IntelliJ, then:

```powershell
& "<JDK_HOME>\bin\java.exe" -jar ".\target\benchmarks.jar" QueueBenchmark -prof gc -rf json -rff results/queue-vs-spsc.json
```

Four group/capacity combinations take approximately three to four minutes plus overhead. Sanitize the JSON JVM path before committing.

## Environment and results

The run contains four group/capacity combinations, each with three forks and five measurement iterations per fork. Surefire reports record 37 passing tests overall, including all three ring-buffer tests, with no failures/errors.

| Capacity | ArrayBlockingQueue successful polls (#) | Ring successful polls (#) |
| ---: | ---: | ---: |
| 1024 | 795,263,523 | 1,510,989,076 |
| 65536 | 959,420,760 | 1,583,921,088 |

The EVENTS counters are aggregate counts across measurement samples, not per-second rates. Under the same configured measurement schedule the ring recorded approximately 1.90x and 1.65x as many successful polls. Exact transfer rates require normalization with actual measured durations; the primary throughput still counts attempts, not messages. Both paths show failed polls and offers, which must be considered.

The GC profiler reports 6 and 8 collections for ArrayBlockingQueue combinations and zero for ring combinations. Normalized allocation was approximately 1.10/1.25 B per attempt for ArrayBlockingQueue versus 0.000082/0.00230 B per attempt for the ring. These metrics include the profiled group workload and measurement/setup effects; they do not prove allocation on every queue operation or identify the allocation source. Potential lock contention and measurement effects would require targeted profiling before attributing the allocation difference.

Numerical data is stored in `results/queue-vs-spsc.json`; only the personal JVM executable path was replaced with a placeholder. This run supports further investigation of the specialized SPSC design but is not production or tail-latency validation. Hardware metadata and independent repeatability remain outstanding.

Record CPU/core count, RAM, exact OS/JDK/JVM/Maven, JVM flags and relevant load. No thread affinity is applied.

## Limitations

No p99 latency, CPU utilization, blocking wait behavior, fairness comparison, multiple producers/consumers, padding or cached-index optimization is tested. Producer and consumer indices may share a cache line. Reusing one payload excludes message construction and may differ from diverse real messages. GC profiler and auxiliary counters add overhead. A passing concurrency test is useful evidence but not a formal memory-model proof or long-term stress validation.
