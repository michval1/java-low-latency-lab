# Java Low-Latency Lab

## Goal

Create small, reproducible JMH experiments and learn to explain **why** one implementation
behaves differently from another.

Do not optimize based only on intuition. Benchmark first.

## Experiments to implement

1. `long` vs `Long`
2. array vs `ArrayList`
3. array lookup vs `HashMap`
4. allocate-per-message vs reuse
5. heap `ByteBuffer` vs direct `ByteBuffer`
6. `ArrayBlockingQueue` vs custom SPSC ring buffer
7. string parsing vs fixed binary decoding

## Rule

Every experiment should answer:

- What exactly is measured?
- What is excluded from measurement?
- What is the expected hypothesis?
- What did JMH show?
- What could make the benchmark misleading?

## Run

```bash
mvn clean package
mvn exec:java -Dexec.mainClass=org.openjdk.jmh.Main
```

To run one benchmark:

```bash
mvn exec:java -Dexec.mainClass=org.openjdk.jmh.Main -Dexec.args="PrimitiveVsBoxedBenchmark"
```

## Example result format

```text
Benchmark                               Mode  Cnt   Score   Error  Units
PrimitiveVsBoxedBenchmark.primitive      avgt   10   3.210 ± 0.10 ns/op
PrimitiveVsBoxedBenchmark.boxed          avgt   10  12.870 ± 0.40 ns/op
```

Your results will differ. Never put made-up benchmark numbers in GitHub.

## Recommended profiling tools later

- Java Flight Recorder
- JDK Mission Control
- async-profiler
- Linux `perf`

## Included dummy data

This repository includes sample fixtures under `data/` so you can start implementing
without inventing inputs first.

Look for `DUMMY_DATA.md` or `DUMMY_DATA_EXPECTED.md` for the expected behavior.

