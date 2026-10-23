# Java Low-Latency Lab

Seven JMH experiments covering Java data structures, allocation, buffer access, message decoding and single-producer/single-consumer queues.

This lab is for practicing benchmark design and interpreting measurements. Each experiment has a question, correctness tests, recorded measurements and notes on what the measurements leave out.

## Experiments

| Experiment | Implementation | Write-up |
| --- | --- | --- |
| Primitive vs boxed values | Sum identical data in long[] and Long[] | [01](experiments/01-primitive-vs-boxed.md) |
| Array vs ArrayList | Indexed traversal using the same Long objects | [02](experiments/02-array-vs-arraylist.md) |
| Array vs HashMap | Successful lookups with sequential and random keys | [03](experiments/03-array-vs-hashmap.md) |
| Allocation vs reuse | Create or reuse a message, then publish and process it | [04](experiments/04-allocation-vs-reuse.md) |
| Heap vs direct ByteBuffer | Read and write long values at absolute offsets | [05](experiments/05-bytebuffer.md) |
| Text vs binary decoding | Decode the same four fields from text or a 25-byte buffer | [06](experiments/06-message-decoding.md) |
| Queue vs SPSC ring buffer | One producer and one consumer using offer/poll | [07](experiments/07-spsc-ring-buffer.md) |

All seven have initial results. The latest test reports contain 37 passing cases. Independent repeat runs are still needed, especially for the largest array/ArrayList case, where confidence intervals overlap.

## Build

Requirements: JDK 21 and Maven 3. IntelliJ's bundled Maven also works.

```bash
git clone https://github.com/michval1/java-low-latency-lab.git
cd java-low-latency-lab
mvn clean package
```

In IntelliJ, select JDK 21 for the project and Maven runner, then execute `clean package` in the Maven tool window. This runs the tests and creates `target/benchmarks.jar` with its dependencies.

## Run

Run one experiment from the repository root:

```bash
java -jar target/benchmarks.jar PrimitiveVsBoxedBenchmark -prof gc -rf json -rff results/primitive-vs-boxed-arrays.json
```

Replace the class name and output filename for another experiment. Use a new filename to retain an earlier run. To run everything, omit the class-name filter. Individual runs take several minutes.

If Java is not on PATH, PowerShell can invoke the JDK directly:

```powershell
& "<JDK_HOME>\bin\java.exe" -jar ".\target\benchmarks.jar" PrimitiveVsBoxedBenchmark -prof gc -rf json -rff results/primitive-vs-boxed-arrays.json
```

Replace `<JDK_HOME>` with the JDK directory on your machine. The exact commands for each experiment are in its write-up.

## Results

[RESULTS_SUMMARY.md](RESULTS_SUMMARY.md) compares the findings and links to the full methods. JMH JSON files are in `results/`. The original fixed-addition baseline is retained separately from the later array-sum experiment.

A few observations from the recorded runs:

- Boxed array summation became more expensive relative to primitive storage as the dataset grew.
- Reusing a message reduced measured allocation from about 40 B/message to near zero in the chosen single-threaded path.
- Heap buffer reads had lower time than direct reads; write intervals overlapped.
- The fixed binary decoder allocated less than the split/parse text implementation.
- The SPSC ring recorded more successful polls than ArrayBlockingQueue under the same measurement schedule.

These observations apply to the specific workloads. No experiment measures production p99 latency. Queue primary throughput includes failed attempts; successful poll counters are aggregate counts. Lookup time is normalized per lookup, whereas summation and buffer time describe complete traversals.

## Measurement choices

Inputs are deterministic and prepared outside measurement. Experiments use three JVM forks with warmup and measurement iterations. The GC profiler records measured allocation; it does not report retained input memory or the size of native buffers.

The write-ups document the exceptions: per-message allocation is measured in Experiment 04, text-parser temporaries are measured in Experiment 06, and both queue threads run concurrently in Experiment 07. The custom ring supports exactly one producer and one consumer.

Hardware and runtime details are in [BENCHMARK_ENVIRONMENT.md](BENCHMARK_ENVIRONMENT.md). Exact Windows/Maven versions, power settings and background load were not recorded.

Before committing new JSON results:

```powershell
.\scripts\Sanitize-JmhResults.ps1
git diff --check
```

The script replaces the JVM executable path with a placeholder and flags other absolute paths. Inspect additional logs separately.

## Repository layout

```text
src/main/java/sk/michal/latency/   benchmark and implementation classes
src/test/java/sk/michal/latency/   correctness and concurrent-transfer tests
experiments/                     questions, methods, results and limitations
results/                         JMH JSON and the original baseline
scripts/                         result sanitization
data/                           small synthetic market-message fixture
```

The CSV fixture and BenchmarkData class provide small examples; performance experiments generate their own larger inputs. See [DUMMY_DATA.md](DUMMY_DATA.md).

[PROJECT_NOTES.md](PROJECT_NOTES.md) records implementation choices and remaining work. [EXPERIMENT_TEMPLATE.md](EXPERIMENT_TEMPLATE.md) is the starting point for another experiment.

## Next work

- Repeat the largest array/ArrayList case in a separate run and compare uncertainty.
- Record exact OS/Maven versions and machine conditions for future runs.
- Profile selected results to investigate causes rather than attributing them to cache or JIT behavior from timing alone.
- Extend queue validation with longer stress runs before considering production use.
