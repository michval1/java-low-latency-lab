# Experiment 01: primitive versus boxed array storage

## Question

How does summing an identical dataset differ when values are stored in long[] versus Long[]?

## Hypothesis (recorded before performance measurement)

The primitive array is expected to be faster, particularly for large datasets, because it stores values contiguously without references to wrapper objects. The boxed array requires reference loads and unboxing. This is a hypothesis, not a measured conclusion.

Neither measured loop intentionally allocates objects. Wrappers are created in setup, so near-zero measured allocation would not imply that Long[] has the same memory footprint as long[].

## Implementations and unit of work

- Both methods sum all elements into a primitive long accumulator and return the sum.
- One JMH operation means one full-array sum. ns/op is time per complete array, not per element.
- Sizes: 1,024; 65,536; 1,000,000 elements.
- Deterministic pseudorandom values: seed 42, range [1000, 1000000), outside the guaranteed Long cache range.
- Setup constructs both representations before measurement. Both are retained in each benchmark state.
- Data generation, array allocation and initial boxing are excluded from measurement.
- Correctness checks cover matching sums and repeatable setup at every configured size.

## Configuration

AverageTime, nanoseconds, one thread, three JVM forks, three 2-second warmup iterations and five 2-second measurement iterations per fork.

## Run

Build with `mvn clean package` or the IntelliJ Maven tool window. From the repository root, with Java on PATH:

```bash
java -jar target/benchmarks.jar PrimitiveVsBoxedBenchmark -prof gc -rf json -rff results/primitive-vs-boxed-arrays.json
```

For PowerShell with an IDE-managed JDK:

```powershell
& "<JDK_HOME>\bin\java.exe" -jar ".\target\benchmarks.jar" PrimitiveVsBoxedBenchmark -prof gc -rf json -rff results/primitive-vs-boxed-arrays.json
```

Replace the placeholder locally; do not commit your personal installation path. The default run is approximately five minutes plus JVM startup and profiling overhead.

## Environment to record before the run

- CPU:
- RAM:
- OS and version:
- JDK and JVM: OpenJDK 21.0.12.1+1-LTS, OpenJDK 64-Bit Server VM.
- Maven:
- JVM flags: none; GC profiler enabled.

## Results

Measured on 2026-10-01, using the documented configuration and GC profiler. The JSON includes six combinations, each with three forks and five measured iterations per fork. The existing Surefire report records three passing correctness tests (zero failures/errors).

| Elements | primitivePath (ns/op) | boxedPath (ns/op) | Boxed / primitive |
| ---: | ---: | ---: | ---: |
| 1,024 | 185.831 ± 0.190 | 191.388 ± 0.700 | 1.03x |
| 65,536 | 12,333.670 ± 19.199 | 14,627.794 ± 66.463 | 1.19x |
| 1,000,000 | 189,769.004 ± 278.934 | 395,485.636 ± 8,254.730 | 2.08x |

Errors are JMH's 99.9% confidence interval half-widths. Each operation is a full-array sum. Results are stored in `results/primitive-vs-boxed-arrays.json`; only the personal JVM executable path was replaced with `<JDK_HOME>/bin/java.exe`. Numerical data is unchanged. The old baseline file measures fixed-value addition and is not directly comparable.

## Interpretation

The observed difference increases with dataset size. At one million elements, summation took approximately 190 microseconds for primitives and 395 microseconds for boxed storage. This supports the recorded hypothesis for this workload and run.

Reference loading, unboxing and the larger memory footprint of boxed storage are plausible explanations. Cache effects are an interpretation, not a confirmed diagnosis: no hardware-counter or assembly analysis was performed.

Normalized allocation (primitive / boxed, B/op) was approximately 0.000637 / 0.000654, 0.0423 / 0.0501, and 0.651 / 1.347 at the three sizes. All combinations reported zero GC collections during measurement. Allocation rate stayed close to 0.0033 MB/sec in every combination. The small residual allocation is consistent with background measurement activity; its exact source was not identified. Larger B/op in slower benchmarks does not establish an allocation per element. These loops do not explicitly create objects, and wrapper construction happens in setup.

This experiment measures access to prebuilt representations, not the cost of constructing them or their total retained memory. CPU, RAM, exact OS version and Maven version still need to be documented. Additional independent runs can assess repeatability.
## Limitations

Repeated sequential traversal warms caches and does not model random lookup, per-message boxing or end-to-end trading latency. Both representations are retained, and boxed object layout and collector behavior depend on the JVM. GC profiler allocation during measurement does not quantify retained setup memory. Profiling adds overhead; document whether measurements used it. No p99 latency is measured by this AverageTime experiment.
