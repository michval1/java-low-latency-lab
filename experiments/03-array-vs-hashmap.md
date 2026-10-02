# Experiment 03: indexed array versus HashMap lookup

## Question and hypothesis

For dense integer keys in [0, size), how do repeated successful lookups compare in long[] and HashMap<Integer, Long>?

Before measurement: direct array indexing is expected to have lower average time than map lookup. Random keys may increase access costs relative to sequential keys. The size of either effect is unknown until measured.

## Implementations

- Dataset sizes: 1024, 65536, 1000000. Both representations associate key i with value i * 10L.
- Setup creates the array, pre-sizes/populates the map, and generates 1024 query keys.
- Sequential queries traverse a contiguous block near the middle (wrapping for small test datasets). Random queries sample the whole key range with deterministic seed 42.
- Each method performs the same 1024 lookups and returns the sum, allowing correctness checks and result consumption.
- Map query Integer objects are preboxed in setup to exclude per-query key boxing from measurement. Values are unboxed by mapLookup.
- All queries are hits. Input generation, population and wrapper construction are outside measurement.
- JMH OperationsPerInvocation(1024) normalizes time and allocation to one lookup. Unlike Experiments 01 and 02, ns/op here is per lookup, including its share of the loop and accumulation overhead.

## Configuration and run

AverageTime, nanoseconds, one thread, three forks, three 2-second warmup iterations and five 2-second measurement iterations.

Build with `clean package` in IntelliJ's Maven tool window. From the project root:

```powershell
& "<JDK_HOME>\bin\java.exe" -jar ".\target\benchmarks.jar" LookupBenchmark -prof gc -rf json -rff results/array-vs-hashmap.json
```

Replace the JDK placeholder locally. The full run has 12 combinations and takes approximately ten minutes plus startup/profiling overhead. To measure only random access, append `-p accessPattern=random`; this halves the combinations and must be documented with the results.

Sanitize the JSON jvm path before committing, preserving all numerical measurements.

## Environment and results

The successful run contains all 12 combinations, each with three forks and five measurement iterations per fork. JDK: OpenJDK 21.0.12.1+1-LTS. Existing Surefire reports record 16 passing tests: 8 lookup cases, 5 array/list cases and 3 primitive/boxed cases; zero failures or errors.
| Access | Entries | Array (ns/lookup) | Map (ns/lookup) | Map / array |
| --- | ---: | ---: | ---: | ---: |
| sequential | 1024 | 0.197 ± 0.007 | 1.779 ± 0.055 | 9.05x |
| sequential | 65536 | 0.192 ± 0.001 | 1.867 ± 0.070 | 9.73x |
| sequential | 1000000 | 0.193 ± 0.001 | 2.131 ± 0.090 | 11.07x |
| random | 1024 | 0.194 ± 0.001 | 2.026 ± 0.085 | 10.47x |
| random | 65536 | 0.397 ± 0.008 | 2.994 ± 0.215 | 7.54x |
| random | 1000000 | 0.423 ± 0.006 | 3.225 ± 0.060 | 7.63x |

Errors are JMH's 99.9% confidence interval half-widths. These are per-lookup averages normalized from a batch of 1024 lookups, including loop and summation overhead. They are not standalone request latency or p99 latency. Sub-nanosecond normalized times do not imply that an isolated request completes in that time.

The map had higher mean time in every combination in this run. Random accesses increased mean time for both representations at the two larger sizes. At 1024 entries, array random and sequential intervals overlap; no clear ordering is established for those two cases. The sequential array time stays similar across dataset sizes, consistent with the repeated fixed-size local query block; this does not show the cost of traversing a whole large array.

This supports the direct-indexing hypothesis for the documented workload. Map indirection, boxed values and key handling are possible contributors, but no hardware-counter or assembly inspection confirms their individual effects. The fixed query stream repeatedly warms its limited working set, so no broad random-stream or cold-cache conclusion is warranted.

All combinations reported zero GC collections. Measured normalized allocation was tiny (approximately 0.00000066 to 0.00001087 B/lookup); construction and key boxing occurred in setup. This does not establish equal retained memory usage for arrays and maps.

Numerical data is stored in `results/array-vs-hashmap.json`. Only the personal JVM executable path was replaced with a placeholder. Hardware metadata, exact OS and Maven versions remain to be documented, and independent runs can assess repeatability.

## Limitations

This comparison fits dense bounded integer keys; sparse IDs, strings and general key spaces may not permit direct indexing without additional translation or excessive memory. It compares full representations, not isolated hashing overhead: map nodes, references and boxed values differ from primitive array storage. Both structures remain in state.

The fixed 1024-query stream repeats, so only a limited subset is accessed in large datasets and caches may warm substantially. This is not a cold-cache or whole-dataset random-stream experiment. Sequential queries cover a local block, not the entire container. Existing preboxed key objects can benefit from Integer caching for small keys. No missing-key behavior, insertions, concurrency or tail latency is measured. GC profiling does not measure retained setup memory.
