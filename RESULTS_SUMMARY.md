# Measured experiment summary

Seven experiments have implementations, correctness tests and recorded JMH results. The latest available Surefire reports record 37 passing test cases. These are initial measured findings; hardware details are in [BENCHMARK_ENVIRONMENT.md](BENCHMARK_ENVIRONMENT.md); exact OS/Maven metadata and independent repeatability checks remain incomplete.

## Findings and scope

| Experiment | Observation in recorded runs | Important boundary |
| --- | --- | --- |
| long[] / Long[] summation | Boxed/primitive time ratios: 1.03x, 1.19x, 2.08x across three sizes. | Whole-array sums, prebuilt objects; not allocation per element. |
| Long[] / ArrayList<Long> | List/array time ratios: 1.60x, 1.45x, 1.17x. | Original large-size intervals overlap; an independent repeat gives a 1.20x ratio with separated intervals. Shared objects and indexed traversal. |
| Array / HashMap lookup | Map mean lookup time was higher in all twelve combinations. | Dense integer keys, repeated 1024-query batches, successful queries only. |
| Allocation / reuse | Fresh-object path: approximately 40 B/message and 2.1x higher time. | Single-thread ownership and equal volatile publication overhead. |
| Heap / direct ByteBuffer | Heap read times approximately 3.6x lower; write intervals overlap. | Java memory access, not I/O, allocation or native-memory footprint. |
| Text / binary decoding | Binary time approximately 21.6–21.7x lower; 40 versus 320 B/message. | Prepared fixed binary inputs versus chosen split/parse text implementation. |
| ArrayBlockingQueue / SPSC | Ring successful poll counts approximately 1.90x and 1.65x higher under the same configured schedule. | EVENTS are aggregate counts, not ops/s; primary throughput includes failed attempts. |

## Read the full methodology

1. [Primitive versus boxed](experiments/01-primitive-vs-boxed.md)
2. [Array versus ArrayList](experiments/02-array-vs-arraylist.md)
3. [Array versus HashMap](experiments/03-array-vs-hashmap.md)
4. [Allocation versus reuse](experiments/04-allocation-vs-reuse.md)
5. [Heap versus direct buffer](experiments/05-bytebuffer.md)
6. [Text versus binary decoding](experiments/06-message-decoding.md)
7. [Queue versus SPSC](experiments/07-spsc-ring-buffer.md)

Do not rank experiments against each other: their units and workloads differ. Summation/buffer results describe complete traversals; lookup results are normalized per lookup; allocation and decoding operate per message; queue results use throughput and event counts. None establishes production p99 latency.

## Reproducibility status

- JSON files preserve JMH configuration, measurements, confidence intervals and GC-profiler output; the personal JVM executable path is replaced with a placeholder.
- The older fixed-addition baseline is a separate workload and cannot be directly compared with current array summation.
- The runs used OpenJDK 21.0.12.1+1-LTS and JMH 1.37. The benchmark machine uses Ryzen 7 7700, 32 GB RAM at 6000 MT/s and Windows 11. Exact build/runtime conditions remain incomplete; see BENCHMARK_ENVIRONMENT.md.
- Correctness tests are independent of performance measurements; the concurrent ring test is evidence, not a formal memory-model proof.
- The largest array/list case was repeated independently on 2026-10-23: array 382.055 ± 17.131 microseconds/traversal, list 456.582 ± 12.537, ratio 1.20x. The repeat has separated intervals and the same ordering as the original, but both absolute times changed. Other findings do not yet have independent repeat runs.

## Before committing a new run

Run from the repository root in PowerShell:

```powershell
.\scripts\Sanitize-JmhResults.ps1
git diff --check
git status
```

The script replaces only the JMH `jvm` field. It validates JSON and reports remaining absolute paths; it does not make arbitrary logs or other files anonymous. Inspect any newly added console logs before committing.
