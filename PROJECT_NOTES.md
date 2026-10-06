# Engineering Notes

Repository: `java-low-latency-lab`

## 2026-10-01 — Build setup and first JMH baseline

### Goal

Verify that the existing JMH benchmarks can run before expanding the experiments. This repository is self-contained and does not depend on other practice repositories.

### Environment

- OS: Windows; exact version not recorded.
- JDK: Microsoft OpenJDK 21.0.12.1+1-LTS, downloaded through IntelliJ IDEA.
- Java executable: `<JDK_HOME>\bin\java.exe`
- IDE: IntelliJ IDEA 2026.1.3, using its bundled Maven.
- JMH: 1.37.
- CPU and RAM: not recorded yet.
- JVM options for successful run: none.

### Failed approach

Running `org.openjdk.jmh.Main` through Maven `exec:java` found the benchmarks, but both forked JVMs failed with:

```text
Could not find or load main class org.openjdk.jmh.runner.ForkedMain
Caused by: java.lang.ClassNotFoundException: org.openjdk.jmh.runner.ForkedMain
```

Maven printed `BUILD SUCCESS`, but the result table was empty. This was not a successful benchmark run. The forked JVM did not have the required JMH class available on its classpath.

### Working approach

Added Maven Shade packaging to produce `target/benchmarks.jar`, including dependencies and a manifest pointing to `org.openjdk.jmh.Main`. Built the project with Maven and ran the JAR using the explicit JDK path in PowerShell:

```powershell
& "<JDK_HOME>\bin\java.exe" -jar ".\target\benchmarks.jar" PrimitiveVsBoxedBenchmark
```

A custom application Main class is not needed for these benchmarks; JMH supplies the entry point.

### Measurement configuration

- Mode: AverageTime.
- Units: ns/op.
- Threads: 1.
- Forks: 1 per benchmark.
- Warmup: 3 iterations, 10 seconds each.
- Measurement: 5 iterations, 10 seconds each.
- Blackhole mode: compiler, auto-detected.
- Total run time: 2 minutes 40 seconds.

### Baseline results

| Benchmark | Mean (ns/op) | JMH error, 99.9% CI half-width (ns/op) |
| --- | ---: | ---: |
| boxedPath | 0.686 | 0.061 |
| primitivePath | 0.381 | 0.014 |

The boxed path took approximately 1.8 times as long in this particular run. These are measured baseline values, not example numbers from README.

### Interpretation and limitations

The starter methods add one to a fixed value of 123. The boxed result, 124, falls within the Long cache range, so this is not an experiment demonstrating allocation of a new Long on every call. The workload is extremely small and may be strongly optimized by the JIT. These results do not establish a general performance ratio between long and Long, nor do they measure end-to-end message latency or tail latency.

No allocation profiler, JFR recording, or generated assembly inspection was performed. The hypothesis was not recorded before this first run; it must not be reconstructed as a prior prediction afterwards.

### Next tasks

1. Completed: saved the baseline and added working JAR packaging.
2. Completed: README documents the working JAR invocation.
3. Record CPU, RAM, exact OS version, and Maven version for subsequent experiments.
4. Implemented: full-array sums over long[] and Long[] at three sizes; correctness tests passed and the full measurement was completed by the user.
5. Record a hypothesis before the next run, define what one benchmark operation means, and collect results with multiple forks.
6. Inspect allocations with the JMH GC profiler; distinguish storage costs from allocations during the measured operation.


## 2026-10-01 — Expanded primitive/boxed experiment

- Replaced fixed-value addition with summation over identical long[] and Long[] datasets.
- Added sizes 1024, 65536 and 1000000, deterministic seed 42, and values outside the guaranteed Long cache range.
- Setup creates arrays and wrappers before measurement; both methods return a primitive long sum.
- One operation is one complete array traversal. Old baseline numbers describe the previous implementation and are not directly comparable.
- Configured three forks, three 2-second warmup iterations and five 2-second measurement iterations.
- Added correctness tests for matching sums and deterministic setup at all three sizes.
- Recorded the hypothesis, method, run command and limitations in experiments/01-primitive-vs-boxed.md before new measurements.
- Build verification in the Codex session was blocked by AccessDeniedException while accessing dependency JARs. This was a limitation of that verification attempt; the later user run and passing Surefire report are documented below.
- Next: verify build/tests in IntelliJ, record machine details, run with -prof gc, and interpret full-array timings and measured allocation separately from setup memory.

## 2026-10-01 — Array-sum results reviewed

The user completed the full GC-profiled run. All six combinations contain three forks with five measurement iterations each. The Surefire report records three passing tests. Boxed/primitive time ratios were 1.03x, 1.19x and 2.08x as dataset size increased. No measured GC collections occurred; both paths had very small residual allocation. Interpretation and limitations are recorded in experiments/01-primitive-vs-boxed.md. The JSON JVM path was sanitized without changing numerical data. Hardware metadata and independent repeatability checks remain outstanding.

## 2026-10-02 — Array versus ArrayList experiment prepared

Implemented indexed summation over Long[] and ArrayList<Long> containing identical object references. This isolates the container comparison from primitive-versus-boxed representation. Allocation and list capacity preparation happen in setup. Added deterministic reference-sum correctness tests, including empty and single-element cases, and a pre-measurement hypothesis in experiments/02-array-vs-arraylist.md. Three dataset sizes and three forks match the preceding experiment. Build/tests have not yet been verified in this session; no benchmark results are claimed. Next: clean package in IntelliJ, full GC-profiled run, sanitize the JSON JVM path, then record interpretation.

## 2026-10-02 — Array versus ArrayList results reviewed

All eight existing tests passed, including five cases for the new experiment. The JSON contains six complete combinations (three forks, five measurement iterations each). ArrayList/array mean-time ratios are 1.60x, 1.45x and 1.17x. The largest dataset shows overlapping confidence intervals and needs repeatability checks. No measured GC collections occurred. The JSON personal JVM path was replaced with a placeholder without changing numerical values. Results and limitations are documented in Experiment 02. Hardware metadata remains outstanding.

## 2026-10-02 — Lookup experiment expanded

Replaced the fixed key with a deterministic 1024-query batch, three dataset sizes, and sequential/random access patterns. Query keys are preboxed outside measurement. Both methods return the same lookup checksum. OperationsPerInvocation normalizes time and allocation to one lookup. Added eight correctness cases and documented the pre-measurement hypothesis, dense-key requirement, representation differences and repeated-query working-set limitation. Build/tests and measurement remain unverified; no new results are claimed. Full run: twelve combinations, approximately ten minutes plus overhead.

## 2026-10-02 — Lookup results reviewed

All 16 correctness tests passed. The result file contains all 12 combinations with three forks and five measurements per fork. The map has higher mean normalized lookup time in all combinations. Random access costs increased at larger sizes; sequential array queries repeatedly visit a local 1024-entry block. No GC collections were recorded and residual allocation is tiny. Results represent amortized batch lookup costs, not isolated or tail latency. Sanitized the JSON JVM path without changing numerical measurements. Updated Experiment 03 and README; hardware metadata and independent repeatability remain outstanding.

## 2026-10-04 — Allocation versus reuse experiment prepared

Added a single-message benchmark with deterministic inputs, two dataset sizes, fresh allocation versus reuse, equal field updates, cursor advancement, volatile publication and checksum work. Publication makes the message escape in both paths; its overhead is part of the measured operation. Mutable reuse is only modeled with thread-local ownership and no asynchronous consumer. Added tests for values, wrap-around and object identity. Documented the hypothesis before measurement. Build/tests and profiling remain unverified; no new results are claimed.

## 2026-10-04 — Allocation versus reuse results reviewed

All 19 tests passed. Four complete GC-profiled combinations contain three forks and five measurements each. Fresh allocation measured approximately 40 B/message, versus near-zero reuse allocation; allocate/reuse timing ratios were approximately 2.08x and 2.07x. Allocating paths reported GC collections, reuse paths zero. Documented measured publication overhead and ownership limitations in Experiment 04. Sanitized the JSON JVM path without changing numbers. Hardware metadata and independent repeatability remain outstanding.

## 2026-10-05 — ByteBuffer experiment prepared

Added heap/direct absolute long reads and writes at two sizes, with identical BIG_ENDIAN byte order. Setup performs allocation and initial population; measured writes vary their base and include final-element readback. One operation traverses the whole buffer. Added correctness tests for initial data, write sequences and repeated setup. Documented the hypothesis and native-memory/I-O limitations before measurement. Build/tests and results remain unverified. Eight combinations take approximately six to seven minutes plus overhead.

## 2026-10-05 — Buffer results reviewed and decoding prepared

All 22 existing tests passed. Eight buffer combinations completed; heap reads had about 3.6x lower time, while write confidence intervals overlap. Sanitized JSON JVM path and documented results. Prepared text/binary codecs returning equivalent immutable records, fixed 25-byte binary layout, deterministic inputs and validation tests. String creation and binary encoding happen in setup; decoding is measured. New decoding build/tests and performance results remain unverified. Hardware metadata remains outstanding.

## 2026-10-05 — ByteBuffer results reviewed

All eight combinations completed; 22 existing tests passed. Heap read times were approximately 3.6x lower than direct reads. Write intervals overlap at both sizes, so no clear write advantage is claimed. GC counts were zero and measured residual allocation was low; setup allocation and retained native memory are excluded. Updated Experiment 05 and README and sanitized the JSON JVM path. Existing Experiment 06 codec/benchmark/tests were inspected; their build and measurement remain pending.

## 2026-10-06 — Decoding results reviewed and SPSC experiment prepared

Recorded decoding results show approximately 21.6–21.7x lower binary decoding time and 40 versus 320 B/message for these prepared-input implementations. Sanitized the JSON JVM path. Added bounded SPSC FIFO with volatile publication, full/empty behavior and power-of-two indexing. Added FIFO/wrap-around and concurrent ordered-transfer tests. Group benchmark uses one producer and one consumer with successful/failed operation counters; primary throughput is attempts, not completed messages. Build/tests and queue measurements remain pending. Hardware metadata remains outstanding.

## 2026-10-06 — Queue results and pre-commit path check

All 37 tests passed; four complete queue combinations contain three forks with five measurement iterations. JMH EVENTS metrics are aggregate counts (#), so documentation now distinguishes them from ops/s. The ring recorded approximately 1.90x/1.65x as many successful polls under the same configured schedule. GC profiler differences are recorded without attributing their source. Sanitized all four personal JVM paths in queue JSON while preserving numerical data. Hardware metadata and independent repeatability remain outstanding.
