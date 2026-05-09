# Probabilistic and Advanced

The structures here trade certainty for **scale**. A regular hash-set tells you whether `x` is in the set; a Bloom filter tells you "definitely not, or probably yes" using 1/8th the memory. A regular count tells you the exact number of unique IPs that hit a server today; HyperLogLog tells you within 1% using 12 KB. When you can't afford the exact answer — because the data doesn't fit in memory, or it streams past too fast to store — these structures are the right tool.

## Place in the curriculum

- **Prerequisites:** [Linear Structures](/cortex/data-structures-and-algorithms/linear-structures-index) (hash tables), [Trees](/cortex/data-structures-and-algorithms/trees-index) (BSTs and treaps), [Algorithms by Strategy](/cortex/data-structures-and-algorithms/algorithms-by-strategy-index) (randomized algorithms — to know what "expected O(log n)" means).
- **Followed by:** [DSA in Real Systems](/cortex/data-structures-and-algorithms/dsa-in-real-systems-index) (where every one of these structures shows up in production: Bloom filters in Cassandra, HLL in Redis, skip lists in LevelDB).

## Chapters

1. [Skip List](/cortex/data-structures-and-algorithms/probabilistic-and-advanced-skip-list) — probabilistic O(log n) ordered structure; the simpler alternative to RB-trees that LevelDB and Redis use.
2. [Bloom Filter](/cortex/data-structures-and-algorithms/probabilistic-and-advanced-bloom-filter) — false-positive-only set membership; a database's first line of defence against expensive disk lookups.
3. [Count-Min Sketch](/cortex/data-structures-and-algorithms/probabilistic-and-advanced-count-min-sketch) — streaming frequency estimation in sublinear memory.
4. [HyperLogLog](/cortex/data-structures-and-algorithms/probabilistic-and-advanced-hyperloglog) — cardinality estimation; "how many unique users?" in 12 KB.
5. [Treap](/cortex/data-structures-and-algorithms/probabilistic-and-advanced-treap) — the "Tree + Heap" hybrid that gets balance for free from random priorities.
6. [Persistent Data Structures](/cortex/data-structures-and-algorithms/probabilistic-and-advanced-persistent-data-structures) — path copying and fat nodes; the tree behind every undo system and every functional language's immutable map.
