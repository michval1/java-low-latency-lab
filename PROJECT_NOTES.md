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

1. Save the baseline result and commit the packaging change.
2. Update README to use the working JAR invocation instead of `exec:java`.
3. Record CPU, RAM, exact OS version, and Maven version for subsequent experiments.
4. Expand PrimitiveVsBoxedBenchmark to compare equivalent work over long[] and Long[] with identical values, including values outside the Long cache range and several input sizes.
5. Record a hypothesis before the next run, define what one benchmark operation means, and collect results with multiple forks.
6. Inspect allocations with the JMH GC profiler; distinguish storage costs from allocations during the measured operation.

