# Java Low-Latency Lab

> 🚧 **Work in Progress**
>
> This project is an ongoing exploration of low-latency Java techniques using the Java Microbenchmark Harness (JMH).
>
> The benchmark suite is being implemented incrementally. Each experiment includes its methodology, benchmark results, observations, and analysis once completed.

## Overview

**Java Low-Latency Lab** is a collection of controlled microbenchmarks exploring performance characteristics relevant to low-latency Java applications.

The project focuses on topics such as:

- primitive vs boxed values,
- memory allocation and object reuse,
- arrays and Java collections,
- memory access patterns,
- heap vs direct memory,
- message decoding,
- queue implementations,
- single-producer/single-consumer communication,
- JVM and garbage-collection behaviour.

The goal is not simply to determine which implementation is "faster".

Instead, each experiment attempts to answer:

> **Why does a particular implementation behave differently, and under which conditions does that difference matter?**

The project uses **JMH (Java Microbenchmark Harness)** to reduce common JVM benchmarking problems such as insufficient warmup, dead-code elimination, constant folding, and incorrect timing methodology.

---

## Project Status

| Experiment                       | Implementation     | Benchmark | Analysis  |
| -------------------------------- | ------------------ | --------- | --------- |
| Primitive `long` vs boxed `Long` | 🟡 Initial version | ⬜ Pending | ⬜ Pending |
| Array vs `ArrayList`             | ⬜ Planned          | ⬜ Pending | ⬜ Pending |
| Array vs `HashMap` lookup        | 🟡 Initial version | ⬜ Pending | ⬜ Pending |
| Allocation vs object reuse       | ⬜ Planned          | ⬜ Pending | ⬜ Pending |
| Heap vs direct `ByteBuffer`      | ⬜ Planned          | ⬜ Pending | ⬜ Pending |
| Text vs binary message decoding  | ⬜ Planned          | ⬜ Pending | ⬜ Pending |
| Queue vs SPSC ring buffer        | ⬜ Planned          | ⬜ Pending | ⬜ Pending |

Legend:

- 🟢 Complete
- 🟡 In progress / initial implementation
- ⬜ Planned or pending

---

## Motivation

Low-latency systems often operate under requirements where small delays can become significant when repeated millions of times.

This is especially relevant to systems such as:

- trading infrastructure,
- market-data processing,
- real-time event processing,
- messaging systems,
- high-throughput backend services.

In these environments, performance can be influenced by details that are less important in typical applications:

- object allocation,
- garbage collection,
- boxing and unboxing,
- memory layout,
- cache locality,
- data structures,
- synchronization,
- message representation.

This repository is intended as a practical laboratory for investigating these effects instead of relying only on assumptions about performance.

---

# Benchmark Methodology

All performance experiments use **JMH**.

Microbenchmarking JVM applications can be misleading because the JVM performs runtime optimizations such as:

- Just-In-Time compilation,
- method inlining,
- constant folding,
- dead-code elimination,
- escape analysis.

For this reason, benchmarks in this repository follow several rules.

### Warmup

Benchmarks include warmup iterations before measurements are collected.

This allows frequently executed code to be optimized by the JVM before measurements begin.

### Forks

Benchmarks are executed in separate JVM forks where appropriate.

This reduces the effect of previous benchmark executions on later measurements.

### Benchmark State

Input data is prepared outside the measured operation whenever possible.

The benchmark should measure the operation under investigation rather than unrelated setup work.

### Equivalent Work

Two implementations being compared should perform equivalent logical work.

A faster benchmark is meaningless if one implementation performs less work than the other.

### Results

Results will be stored together with information about the machine and JVM used to produce them.

Raw benchmark output should be preserved where practical so results can be inspected independently.

---

# Environment

Benchmark results depend heavily on hardware and JVM configuration.

The environment used for final measurements will be recorded here.

```text
CPU:        TBD
RAM:        TBD
OS:         TBD
JDK:        Java 21
JVM:        TBD
JMH:        1.37
Maven:      TBD
```

Additional JVM flags used for individual experiments will be documented alongside their results.

---

# Experiments

## 1. Primitive `long` vs Boxed `Long`

**Status:** 🟡 Initial implementation

### Question

What performance and allocation differences appear when using primitive `long` values compared with boxed `Long` values?

### Why It Matters

Boxed values introduce additional JVM behaviour including:

- boxing,
- unboxing,
- object representation,
- possible allocation,
- cached wrapper objects.

These differences may become relevant in allocation-sensitive hot paths.

### Important Benchmark Consideration

Java caches certain boxed `Long` values.

Using only small values may therefore hide part of the cost that the experiment is attempting to measure.

The final benchmark will include inputs outside the commonly cached range and varying input values.

### Metrics

The experiment will investigate:

- average operation time,
- allocation rate,
- garbage-collection behaviour.

### Results

*Not measured yet.*

---

## 2. Array vs `ArrayList`

**Status:** ⬜ Planned

### Question

How does direct array access compare with access through `ArrayList` when processing the same logical dataset?

### Areas of Interest

The experiment will investigate:

- indexing overhead,
- boxing where applicable,
- memory layout,
- iteration performance,
- cache locality.

### Results

*Not measured yet.*

---

## 3. Array vs `HashMap` Lookup

**Status:** 🟡 Initial implementation

### Question

How does direct indexed lookup compare with hash-based lookup?

### Current Implementation

The initial benchmark contains:

- an array,
- a `HashMap`,
- 1,024 values,
- lookup operations for both structures.

### Planned Improvements

The final experiment will introduce:

- multiple dataset sizes,
- pre-generated lookup keys,
- varying access patterns,
- equivalent stored values,
- clearer separation of lookup cost from benchmark setup.

The benchmark must also account for boxing and unboxing performed by the `HashMap` implementation.

### Results

*Not measured yet.*

---

## 4. Allocation vs Object Reuse

**Status:** ⬜ Planned

### Question

What happens when a message-processing hot path creates new objects for every message compared with reusing existing objects?

### Areas of Interest

The experiment will investigate:

- allocation rate,
- throughput,
- average latency,
- garbage-collection activity.

### Scenario

Synthetic market messages will be processed using two approaches:

```text
Message -> new object -> process
```

and:

```text
Message -> reusable object -> process
```

Both implementations must produce equivalent results.

### Results

*Not measured yet.*

---

## 5. Heap vs Direct `ByteBuffer`

**Status:** ⬜ Planned

### Question

How does a heap-backed `ByteBuffer` compare with a direct `ByteBuffer` for controlled read/write workloads?

### Important Benchmark Consideration

Buffer allocation must not accidentally become part of a benchmark intended to measure buffer access.

Buffers will therefore be prepared before the measured operation unless allocation itself is explicitly being tested.

### Areas of Interest

- read performance,
- write performance,
- allocation behaviour,
- JVM/native memory interaction.

### Results

*Not measured yet.*

---

## 6. Text vs Binary Message Decoding

**Status:** ⬜ Planned

### Question

What is the cost of decoding a text-based market message compared with decoding a fixed binary representation?

Example logical message:

```text
orderId = 10001
priceTicks = 185025
quantity = 100
side = BUY
```

Both decoders must produce equivalent logical values.

### Text Representation

Example:

```text
10001,185025,100,BUY
```

### Binary Representation

A compact fixed-layout representation will encode the same fields directly as binary values.

### Areas of Interest

The experiment will investigate:

- parsing overhead,
- allocation,
- throughput,
- latency,
- message size.

### Results

*Not measured yet.*

---

## 7. Queue vs SPSC Ring Buffer

**Status:** ⬜ Planned

### Question

How does a conventional queue compare with a fixed-size Single Producer / Single Consumer ring buffer?

### Scenario

```text
Producer
   |
   v
+--------------------+
| Queue / RingBuffer |
+--------------------+
   |
   v
Consumer
```

The experiment represents a simplified message-passing pipeline.

### Ring Buffer Requirements

Before benchmarking, the implementation must correctly handle:

- producer/consumer ordering,
- wrap-around,
- full buffer state,
- empty buffer state,
- visibility between threads.

Correctness will be tested before performance measurements are collected.

### Areas of Interest

- throughput,
- allocation,
- synchronization overhead,
- predictable memory usage,
- latency.

### Results

*Not measured yet.*

---

# Dummy Market Data

The repository contains a small synthetic market-data dataset:

```text
data/sample-market-messages.csv
```

Example structure:

```text
message_id,order_id,price_ticks,quantity,side
1,10001,185025,100,BUY
...
```

The small dataset exists primarily to make the examples understandable.

Larger deterministic datasets will be generated for benchmarks where required.

Input generation should normally happen outside the measured benchmark method.

---

# Project Structure

```text
java-low-latency-lab/
│
├── README.md
├── pom.xml
├── DUMMY_DATA.md
├── EXPERIMENT_TEMPLATE.md
├── PROJECT_NOTES.md
│
├── data/
│   └── sample-market-messages.csv
│
└── src/
    ├── main/
    │   └── java/
    │       └── sk/
    │           └── michal/
    │               └── latency/
    │                   ├── BenchmarkData.java
    │                   ├── PrimitiveVsBoxedBenchmark.java
    │                   └── LookupBenchmark.java
    │
    └── test/
        └── java/
```

As the project develops, benchmark documentation and raw results will be added:

```text
experiments/
results/
```

---

# Building the Project

## Requirements

The intended environment is:

- Java 21+
- Maven
- JMH 1.37

Check the installed versions:

```bash
java -version
mvn -version
```

Clone the repository:

```bash
git clone https://github.com/michval1/java-low-latency-lab.git
cd java-low-latency-lab
```

Build:

```bash
mvn clean package
```

> The final benchmark execution workflow is still being verified and will be updated as part of the initial project setup.

---

# Running Benchmarks

The final project will provide a reproducible command for running the complete benchmark suite as well as individual experiments.

For example:

```bash
mvn clean package
```

followed by the configured JMH runner.

Exact commands will be documented once the standalone benchmark execution setup has been verified.

---

# Results

No benchmark numbers are currently presented as final results.

This is intentional.

Performance numbers will only be added after:

1. the benchmark implementation has been reviewed,
2. equivalent work between implementations has been verified,
3. JVM warmup and forks have been configured,
4. the execution environment has been documented,
5. the benchmark has been reproduced across multiple runs.

Raw JMH results will be stored in the repository where practical.

---

# Experiment Documentation

Each completed experiment should document:

### 1. Question

What exactly is being measured?

### 2. Hypothesis

What behaviour is expected before running the benchmark?

### 3. Implementation

How are the compared approaches implemented?

### 4. Benchmark Configuration

Including:

```text
Warmup iterations
Measurement iterations
Fork count
Benchmark mode
Time unit
JVM arguments
```

### 5. Environment

Including:

```text
CPU
RAM
Operating system
JDK
JVM
```

### 6. Results

Raw JMH results and a summarized comparison.

### 7. Interpretation

Why might the observed difference exist?

### 8. Limitations

What does the benchmark **not** prove?

This is important because microbenchmark results should not automatically be generalized to complete production systems.

---

# Planned Repository Layout

As experiments are completed, the repository will evolve toward:

```text
java-low-latency-lab/
│
├── README.md
├── pom.xml
│
├── data/
│
├── experiments/
│   ├── 01-primitive-vs-boxed.md
│   ├── 02-array-vs-arraylist.md
│   ├── 03-array-vs-hashmap.md
│   ├── 04-allocation-vs-reuse.md
│   ├── 05-bytebuffer.md
│   ├── 06-message-decoding.md
│   └── 07-spsc-ring-buffer.md
│
├── results/
│   └── ...
│
└── src/
    ├── main/
    │   └── java/
    └── test/
        └── java/
```

---

# Goals

By the end of the project, the repository should demonstrate practical understanding of:

- Java performance measurement,
- JMH,
- JVM warmup and JIT compilation,
- primitive vs boxed data,
- object allocation,
- garbage-collection pressure,
- Java collections,
- memory access patterns,
- `ByteBuffer`,
- binary data representation,
- concurrency,
- SPSC communication,
- benchmark design,
- performance-result interpretation.

The broader goal is to understand the trade-offs involved in designing **predictable, allocation-conscious, low-latency Java systems**.

---

# Key Findings

This section will be updated as experiments are completed.

No conclusions are currently presented because the benchmark suite has not yet been fully executed and validated.

---

# Limitations

Microbenchmarks measure isolated operations under controlled conditions.

Results from this project should therefore not be interpreted as universal rules such as:

> "Arrays are always faster than collections."

or:

> "Object allocation should always be avoided."

Real-world performance depends on:

- workload,
- JVM implementation,
- hardware,
- dataset size,
- access patterns,
- concurrency,
- garbage collector,
- application architecture.

The purpose of these experiments is to understand specific trade-offs under documented conditions.

---

# Roadmap

- [ ] Verify Java 21 / Maven environment
- [ ] Finalize reproducible JMH execution
- [ ] Improve primitive vs boxed benchmark
- [ ] Measure primitive vs boxed allocation
- [ ] Implement array vs `ArrayList`
- [ ] Improve array vs `HashMap`
- [ ] Implement allocation vs reuse
- [ ] Implement heap vs direct `ByteBuffer`
- [ ] Implement text vs binary decoding
- [ ] Implement and test SPSC ring buffer
- [ ] Record benchmark environment
- [ ] Store raw JMH results
- [ ] Document findings for each experiment
- [ ] Add correctness tests
- [ ] Complete final benchmark comparison

---

## Current State

The repository currently provides the initial JMH benchmark structure, synthetic market data, experiment documentation templates, and the first benchmark implementations.

The project is under active development. Benchmark results and conclusions will be added only after each experiment has been implemented and validated.
