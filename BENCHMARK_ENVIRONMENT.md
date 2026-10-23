# Benchmark environment

The experiments were run on the environment listed below. Exact OS build, firmware, power settings, background load and Maven version were not recorded during the runs.

| Item | Recorded value |
| --- | --- |
| CPU | AMD Ryzen 7 7700 |
| RAM | 32 GB, reported memory speed 6000 MT/s |
| OS | Windows 11; exact version/build not recorded |
| JDK | OpenJDK 21.0.12.1+1-LTS |
| JVM | OpenJDK 64-Bit Server VM |
| JMH | 1.37 |
| Maven | Bundled with IntelliJ IDEA; exact version not recorded |
| JVM flags | Inspect jvmArgs in each JSON result |
| Profiling | JMH GC profiler |

CPU affinity, power plan, memory timings, thermal state and background processes were not controlled/documented. These remain limitations for precise cross-machine comparison. Conditions may differ between runs.

No username, device name, serial number or personal installation path is required in this record. The jvm executable field in committed JSON is a placeholder; preserve actual JDK version and flags for reproducibility.
