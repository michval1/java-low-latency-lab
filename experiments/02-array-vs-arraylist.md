# Experiment 02: Long[] versus ArrayList<Long>

## Question

What difference appears when sequential indexed summation uses a Long[] versus an ArrayList<Long> holding the same objects?

## Hypothesis (recorded before measurement)

The array may be slightly faster due to simpler indexed access. The difference may also be small or indistinguishable because the JIT can inline ArrayList.get and optimize bounds checks. Larger workloads may be dominated by access to the shared Long objects. These are hypotheses, not results.

## Method

- Both containers refer to the exact same Long objects, in the same order.
- Both loops use integer indexing, unbox each value, accumulate into long and return the full sum.
- One operation means a full-container traversal, not one element access.
- Sizes: 1024, 65536, 1000000; deterministic seed 42; values in [1000, 1000000).
- Setup constructs both containers, pre-sizes the list and creates wrappers outside measurement.
- No container construction, resizing, insertion, iterator creation or deliberate object allocation occurs inside the measured loops.
- Comparing long[] with ArrayList<Long> would mix representation and container differences; Experiment 01 already covers primitive/boxed storage.
- Correctness tests compare both sums with a deterministic reference sum, including empty and single-element input.

## Configuration

AverageTime in ns/op, one thread, three forks, three 2-second warmup iterations, five 2-second measurement iterations.

## Build and run

Run `clean package` through IntelliJ's Maven tool window, then from the project root:

```bash
java -jar target/benchmarks.jar ArrayVsArrayListBenchmark -prof gc -rf json -rff results/array-vs-arraylist.json
```

PowerShell with an IDE-managed JDK:

```powershell
& "<JDK_HOME>\bin\java.exe" -jar ".\target\benchmarks.jar" ArrayVsArrayListBenchmark -prof gc -rf json -rff results/array-vs-arraylist.json
```

Replace the placeholder locally. The full run takes approximately five minutes plus startup/profiling overhead. Before committing JSON, replace its personal `jvm` executable path with `<JDK_HOME>/bin/java.exe` while preserving numerical data.

## Environment

Record CPU, RAM, OS version, JDK/JVM, Maven version and JVM flags for the actual run.

## Results

The completed run contains six combinations, each with three forks and five measurement iterations per fork. JDK: OpenJDK 21.0.12.1+1-LTS. The Surefire reports record five passing tests for this experiment and three for Experiment 01, with zero failures or errors.

| Elements | Array (ns/op) | ArrayList (ns/op) | List / array |
| ---: | ---: | ---: | ---: |
| 1,024 | 196.854 ± 3.109 | 315.231 ± 15.455 | 1.60x |
| 65,536 | 15,594.578 ± 468.622 | 22,640.013 ± 392.152 | 1.45x |
| 1,000,000 | 444,744.095 ± 44,333.203 | 521,741.927 ± 48,551.018 | 1.17x |

Errors are JMH's 99.9% confidence interval half-widths. One operation sums the full container. Data is stored in `results/array-vs-arraylist.json`; only the personal JVM executable path was replaced with a placeholder. Numerical measurements were preserved.

## Interpretation

ArrayList had a higher mean full-traversal time at each size in this run. The first two sizes show separated reported confidence intervals. At one million elements the intervals overlap and variability is appreciable; the 1.17x point estimate should not be presented as an established repeatable difference. Overlap alone is not a formal statistical test. The independent repeat below provides additional evidence for this size.

This supports the directional hypothesis most clearly for the first two sizes. The measurements alone do not establish whether inlining, bounds checks or cache behavior caused the observed differences. Shared wrapper objects keep boxing and object identity consistent across containers.

All combinations reported zero GC collections during measurement. Normalized allocation ranged from approximately 0.000674 to 1.790 B/op, while allocation rates stayed around 0.0034 MB/sec. The small residual allocation is consistent with background measurement activity; its exact origin was not identified. Neither measured loop intentionally allocates. Setup storage and construction costs are not measured here.

Hardware details are in BENCHMARK_ENVIRONMENT.md; exact OS/Maven versions and runtime conditions remain incomplete. The recorded runs do not establish a universal ArrayList performance rule.

## Independent repeat: 2026-10-23

Repeated only the 1000000-element case, retaining the original result file. JMH/JDK, three forks, five 2-second measurement iterations and three 2-second warmup iterations match the original configuration; JVM arguments are empty in both result files. Each method has all 15 measurement samples.

| Run | Array (microseconds/traversal) | ArrayList (microseconds/traversal) | List / array |
| --- | ---: | ---: | ---: |
| Original | 444.744 ± 44.333 | 521.742 ± 48.551 | 1.17x |
| Repeat | 382.055 ± 17.131 | 456.582 ± 12.537 | 1.20x |

Errors are the reported 99.9% confidence interval half-widths, converted from nanoseconds. In the repeat, the array interval is [364.924, 399.185] microseconds and the list interval is [444.045, 469.119], with no overlap. ArrayList has higher mean time in both independent runs, providing more support for the direction of the difference at this size. Interval separation is descriptive evidence, not a separate hypothesis test.

Both implementations were faster in the repeat (about 14% lower array time and 12% lower list time), and hardware load/power/thermal conditions were not controlled. Keep the runs separate rather than pooling them as identical conditions. The repeat strengthens the workload-specific conclusion; it does not establish a universal ratio or the underlying JIT/cache mechanism.

The new file is `results/array-vs-arraylist-repeat.json`. Its numerical data is unchanged; the JVM executable path is a placeholder. One independent repeat has now been completed for this case; broader repeatability checks remain open.

Repeat command, with Java on PATH:

```bash
java -jar target/benchmarks.jar ArrayVsArrayListBenchmark -p size=1000000 -prof gc -rf json -rff results/array-vs-arraylist-repeat.json
```
## Interpretation checklist

Compare full-traversal time within each size and include uncertainty. Report measured bytes/op and GC counts, distinguishing setup storage from measured allocations. A small difference is not evidence that one container is universally faster. Explain whether the observed result supports the hypothesis without claiming an unverified JIT or cache mechanism.

## Limitations

This covers sequential indexed reading of prebuilt containers in one thread. It does not compare primitives, insertion/removal, resizing, random lookup, iterator traversal or production tail latency. Both containers remain in benchmark state and share Long objects; their memory access is repeatedly warmed. Profiling adds overhead. No generated assembly or hardware counters are collected.
