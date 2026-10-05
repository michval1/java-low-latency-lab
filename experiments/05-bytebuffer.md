# Experiment 05: heap versus direct ByteBuffer

## Question and hypothesis (before measurement)

How does sequential absolute access differ for heap-backed and direct buffers when reading or writing identical long values?

Neither buffer is assumed universally faster. Direct memory can benefit some native I/O workflows, but this experiment contains no I/O. For pure Java access the difference may be small or depend on JIT optimization and dataset size. Both measured paths are expected to have very low allocation because buffer construction is outside measurement.

## Method

- Sizes: 1024 and 65536 long values (8192 and 524288 bytes).
- Both buffers use explicit BIG_ENDIAN byte order, identical capacity and deterministic initial values (seed 42).
- Setup allocates, populates and touches both buffers before measurement. Direct allocation/deallocation and initial page touching are excluded.
- Read methods sum all values with absolute getLong(byteOffset).
- Write methods fill all values with an incrementing base plus the element index, then read back the last long. Sequence advancement and that readback are part of both write timings.
- Absolute access does not change buffer position, so no flip/rewind is required.
- Each JMH operation is one full-buffer traversal; ns/op is NOT per long. Read and write are separate benchmark cases with separate trial state.
- Correctness tests check initial sums, matching writes, changing write sequences and repeatable setup.

## Configuration and run

AverageTime in ns/op, one thread, three forks, three 2-second warmup iterations and five 2-second measurement iterations.

Build via `clean package` in IntelliJ Maven, then run from the project root:

```powershell
& "<JDK_HOME>\bin\java.exe" -jar ".\target\benchmarks.jar" ByteBufferBenchmark -prof gc -rf json -rff results/bytebuffer.json
```

Replace the JDK placeholder locally. Eight combinations take approximately six to seven minutes plus overhead. Sanitize the JSON JVM path before committing.

## Environment and results

All eight combinations completed with three forks and five measurement iterations per fork. Existing Surefire reports show 22 passing tests, including three buffer cases, with zero failures/errors.

| Operation | Values | Heap (ns/traversal) | Direct (ns/traversal) |
| --- | ---: | ---: | ---: |
| Read | 1024 | 54.116 ± 0.473 | 194.850 ± 1.040 |
| Read | 65536 | 3409.798 ± 163.232 | 12332.266 ± 14.703 |
| Write | 1024 | 169.411 ± 5.418 | 164.340 ± 2.489 |
| Write | 65536 | 10874.400 ± 705.662 | 10133.643 ± 53.788 |

Errors are JMH 99.9% confidence interval half-widths. One operation traverses the entire buffer. Direct/heap read-time ratios were approximately 3.60x and 3.62x. For writes, the reported intervals overlap at both sizes; point estimates do not establish a repeatable winner. No assembly or hardware-counter evidence establishes the mechanism behind the read difference.

All cases reported zero GC collections and tiny residual allocation (less than 0.043 B/traversal). Allocation and initial population were in setup; native/retained memory and buffer construction costs are not measured. JSON numerical values are preserved; only the personal JVM executable path was sanitized. Hardware metadata and independent repeatability remain outstanding.

## Interpretation and limitations

Compare heap versus direct within the same size and operation; inspect uncertainty, allocations and GC events. Retained direct/native memory is not measured by heap bytes/op. Low measured allocation does not mean direct buffers require no resources.

Repeated sequential access warms caches and is not cold-memory behavior. Both buffers are retained in state. No file/socket I/O, concurrency, buffer allocation benchmark or tail latency is modeled. Do not infer native I/O performance from these results or assume direct allocation is cheap because accesses are fast. No assembly or hardware-counter evidence is collected.
