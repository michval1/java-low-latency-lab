# Dummy Benchmark Data

`BenchmarkData.java` contains a tiny in-memory trading-like fixture.

Example values:

```text
order 10001 -> price 10000, qty 10
order 10002 -> price 10001, qty 25
...
```

`data/sample-market-messages.csv` contains the same kind of values in a human-readable format.

## How to use it

For a benchmark such as array vs map:

1. In `@Setup`, create a large array/map.
2. Repeat or generate values based on this fixture.
3. Prepare random/precomputed lookup keys.
4. In the `@Benchmark` method, perform only the operation you want to measure.

Do **not** benchmark reading the CSV file unless file parsing itself is the topic.

## Suggested generated sizes

Try:

```text
1,024 records
65,536 records
1,000,000 records
```

This gives you a chance to observe cache/memory effects instead of testing only eight values.
