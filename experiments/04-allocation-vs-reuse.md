# Experiment 04: per-message allocation versus object reuse

## Question and hypothesis (before measurement)

For a single-threaded synthetic message path, how do allocation and time differ when constructing a Message for each operation versus updating one reusable Message?

The allocating path is expected to allocate approximately one Message per operation. Reuse is expected to have much lower measured allocation. Its timing advantage is uncertain because both paths include indexing, field updates, a volatile publication and arithmetic. GC collections may or may not occur during measurement.

## Method

Setup generates deterministic primitive input arrays (seed 42) at sizes 1024 and 65536. Each operation takes the next input, wraps the cursor at the end, writes order ID, price ticks and quantity, publishes the Message and returns `orderId ^ (priceTicks * quantity)` as a synthetic checksum. This is not actual order matching or business logic.

Both paths assign the Message to the same volatile field, making it escape. This prevents treating a source-code `new` as proof of actual allocation: an optimizer can otherwise eliminate non-escaping objects. Volatile publication is deliberately part of BOTH measured paths and its overhead must be considered when interpreting time. It is a benchmark device, not a claim about a production handoff implementation.

Each thread owns its state. Reusing a mutable published object is not safe if asynchronous readers keep references while it is modified; no concurrent consumer is modeled here. Only the latest published object is retained, not the full stream.

Input generation and reusable object construction occur in setup; allocation in allocatePerMessage is measured. One operation means one message. Tests cover expected values, cursor wrap-around, distinct allocated object identities and stable reused identity.

## Configuration and run

AverageTime, ns/op, one thread, three forks; three 2-second warmup and five 2-second measurement iterations.

Run `clean package` through IntelliJ Maven, then from the project root:

```powershell
& "<JDK_HOME>\bin\java.exe" -jar ".\target\benchmarks.jar" AllocationVsReuseBenchmark -prof gc -rf json -rff results/allocation-vs-reuse.json
```

Replace the placeholder locally. Four combinations take approximately three to four minutes plus overhead. Sanitize the JSON JVM executable path before committing.

## Environment and results

All four combinations completed with three forks and five measurement iterations per fork. Surefire reports record 19 passing tests, including three allocation/reuse cases, with no failures or errors.

| Inputs | Allocate (ns/message) | Reuse (ns/message) | Allocate (B/message) | Reuse (B/message) |
| ---: | ---: | ---: | ---: | ---: |
| 1024 | 4.254 ± 0.025 | 2.041 ± 0.041 | 40.000 | approximately 0.000007 |
| 65536 | 4.389 ± 0.066 | 2.116 ± 0.005 | 40.000 | approximately 0.000007 |

Errors are JMH's 99.9% confidence interval half-widths. Allocation is measured with the GC profiler. The allocating path took about 2.08x and 2.07x as long as reuse in this run, supporting the hypothesis for this workload. Approximately 40 bytes were allocated per operation by the fresh-object path; this is a measured result for the current JVM configuration, not a universal object-size claim.

The reported gc.count aggregate was 350 and 603 for the allocating combinations, versus zero for reuse. These are profiler aggregates across measurement samples, not collections per message and not pause-duration or p99 measurements. The tiny residual allocation in reuse is consistent with measurement background activity; its source was not identified.

Volatile publication, cursor advancement and checksum work are included in both paths. Results do not establish that all non-escaping objects allocate, or that mutable reuse is safe for asynchronous consumers. Numerical measurements are preserved in `results/allocation-vs-reuse.json`; the personal JVM executable path was sanitized. Hardware, OS/Maven metadata and independent repeatability checks remain outstanding.

## Interpretation checklist

Compare ns/op and gc.alloc.rate.norm (B/op) at each input size. Inspect allocation rate and GC count/time. Do not assume object size without checking measured allocation. No collections during a short run does not establish no long-term GC effect. Do not generalize reuse as universally preferable: mutable ownership and lifetime constraints matter. Average time does not show p99 or GC pause distributions.
