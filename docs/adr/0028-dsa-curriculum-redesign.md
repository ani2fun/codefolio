# DSA book — ground-up curriculum redesign (zero → senior)

The DSA book (`content/cortex/data-structures-and-algorithms/`, ~600 files) was
built **completeness-first**: a maximalist per-file template applied topic-by-
topic, depth-first. It produced genuinely good material — 74 named patterns, 60+
topics, reaching into flows, SCC, 2-SAT, suffix automaton, treaps, concurrency,
real-systems — but it reads as overwhelming and directionless. Arrays alone is 56
files flattened into one undifferentiated index list; coverage is wildly uneven
(a few deep "mansions", 14 intro+memorize "stubs"); and there is **no designed
progression** through any of it.

The root cause: first-principles teaching lives at two levels, and the project
nailed one and left the other empty.

- **File level — covered.** `DSA-WRITING-STYLE-GUIDE.md` enforces why-before-how,
  define-before-use, the 4-part paragraph. Individual lessons are precise.
- **Curriculum level — absent.** Nothing defined the *order concepts unlock*, the
  *prerequisite DAG* (nothing used before it is taught), or a designated *spine*
  (the minimal must-read path). Numeric prefixes gave ordering, not designed
  progression. `prereqs:` existed only as a decorative bottom-of-lesson section.

This ADR records the decision to **redesign the book around a curriculum spine**
that takes a complete beginner to FAANG/Anthropic-senior level, reusing the
existing rendering infrastructure (ADR-0023 Visualise pipeline, ADR-0024/0027
bespoke renderers, runnable code, mermaid/D2). Execution logistics — session
slices, model/thinking assignments, time estimate, handoffs — live in the
session plan, not here; this ADR is the durable *what and why*.

## Decision

1. **Organise around a spine, not per-file completeness.** A single linear walk
   through a prerequisite DAG. Every concept has explicit prerequisites; depth
   (extra problems, advanced variants) hangs off the spine *without blocking*
   forward progress.
2. **Fresh spine + complete the stubs + promote + link.** Author a new lean spine;
   complete all 14 stubs to full depth; promote existing strong lessons onto the
   spine; link the deep problems/code/widgets as a frozen `practice`/`reference`
   archive. Not a blank-slate rewrite; not an in-place re-fit of 600 files (that
   was the migration that produced the pain).
3. **Understanding first, then fluency.** Build mental models + tradeoff judgment
   first; layer interview/competitive pattern-fluency and speed on top.
4. **Scope = senior + selected advanced.** Complete everything that exists + the
   advanced topics a senior genuinely meets (advanced trees/DS, flows/SCC/bridges/
   2-SAT, randomized, NP/approximation awareness, systems). Pure-competitive math,
   geometry, FFT/NTT, and CP-only optimizations are **light/awareness or
   deferred** — listed below for auditability, not authored.

**Nothing is optional:** every existing topic is on the spine; the 14 stubs get
completed. The `defaultStatus: optional` flags on 17 sections were cosmetic and
are not a priority signal.

## What makes someone expert (the design thesis)

Not memorizing more structures — four capabilities, built *in order*: (1)
**mental models** — *see* memory, pointers, the call stack, a tree as physical
things; (2) **cost intuition** — *feel* an operation's cost; (3) **pattern
transfer** — map an unseen problem onto a known technique; (4) **tradeoff
judgment** — choose and defend.

## The per-concept learning loop

Every spine concept is taught in six steps. The infrastructure already supports
all six:

| Step | What | Infra |
|---|---|---|
| 1. **Why It Exists** | a concrete situation that creates the need | prose |
| 2. **See it before you name it** | watch it move | auto-trace `python/java run viz=… viz-root=…`; or `d3 widget=…` |
| 3. **Name & formalize** | precise definition, invariant, cost (time+space) | prose + `mermaid` / `d2` |
| 4. **Trace one concretely** | step through with locals + call-stack | `java trace` / Visualise modal |
| 5. **Your turn** | guided → independent, runnable in-browser | `python run` / `java run` |
| 6. **Reflect & connect** | when to reach for it, what's next, recall card | prose + spaced-repetition card |

## The spine (dependency-ordered; senior + selected-advanced scope)

Status tags: **[DEEP]** exists, promote · **[STUB→DO]** exists as intro+memorize,
must complete · **[+PATTERNS]** lessons exist, add pattern/problem layer ·
**[NEW]** author from scratch · **[AWARE]** light/selected/awareness depth.

- **Part 0 — How to think.** Machine model: bits/bytes/addresses/words/cache **[DEEP, reframe]** · cost: asymptotics, recurrences+Master, amortized **[DEEP]** · proof technique: induction, loop invariants, exchange argument **[DEEP]** · light math primer (logs, summations, modular-arithmetic basics, basic counting) **[NEW light]** — load-bearing for hashing (Part 1) & string hashing (Part 9) · problem-solving methodology (examples-first, find the invariant, constraints→target-complexity→technique, stress-test against brute force) **[NEW]**.
- **Part 1 — Linear structures.** Arrays (static/dynamic/multidim, amortized doubling) **[DEEP]** · strings (beginner: char arrays, ops) **[NEW]** · linked lists (singly/doubly/circular, sentinels) **[DEEP]** · stacks & queues/deques/circular buffers **[DEEP]** · hash tables (hashing, chaining vs open-addressing, load factor, rehash) **[DEEP]** · heaps/priority queues (binary, d-ary, heapify) **[DEEP]** · bit manipulation (binary representation, set/clear/toggle, `x&-x`, popcount, masks, XOR) **[DEEP — `08-bit-tricks`, 6 patterns]**.
- **Part 2 — Sorting & searching.** Elementary/merge/quick/heap, linear-time (counting/radix/bucket), lower bound, stability/in-place/external **[DEEP]** · selection: quickselect **[DEEP]** + median-of-medians **[NEW]** · binary search: classic/rotated, lower/upper bound, on the answer, ternary **[DEEP]**.
- **Part 3 — Recursion & paradigms I.** Recursion + call stack **[DEEP]** · divide & conquer (closest pair, Karatsuba) **[STUB→DO]** · backtracking (subsets/perms/combos, N-Queens, Sudoku, pruning, branch&bound, meet-in-the-middle) **[DEEP, +meet-in-middle]**.
- **Part 4 — Trees.** Binary trees + traversals (recursive/iterative/Morris, level-order) **[DEEP]** · BST (insert/delete, succ/pred, validation, iterators) **[DEEP]** · balanced BSTs: AVL, red-black, B/B+ **[STUB→DO, rotations via static D2]** · tries (+radix) **[STUB→DO]** · bridge structures: segment tree, Fenwick/BIT, DSU **[STUB→DO]** · tree pattern layer: traversal patterns, LCA/binary lifting, Euler-tour-to-range **[+PATTERNS]** (tree-DP deferred to Parts 6/10).
- **Part 5 — Graphs (core).** Representations, BFS/DFS, components, flood fill, multi-source BFS **[DEEP, +multi-source]** · topo sort, cycle detection, bipartite/2-color **[DEEP]** · shortest paths: BFS, 0-1 BFS, Dijkstra, Bellman-Ford, Floyd-Warshall **[DEEP, +0-1 BFS]** · MST: Prim, Kruskal+DSU **[DEEP]** · graph pattern/problem layer **[+PATTERNS — pattern-thin today]**.
- **Part 6 — Greedy & DP.** Greedy (exchange argument, interval scheduling, Huffman, fractional knapsack) **[STUB→DO]** · DP core (memo/tab, 1D/2D, 0/1 & unbounded knapsack, coin change, LIS, LCS/edit, matrix chain, subset sum, grid/interval/palindrome) **[DEEP]** · amortized analysis cross-ref **[DEEP]**.
- **Part 7 — Advanced data structures (senior-met).** Segment tree w/ lazy propagation; iterative segtree **[NEW]** · Fenwick 2D/range-update **[+]** · sparse table/RMQ; sqrt decomposition + Mo's **[NEW, selected]** · monotonic stack/queue, min-stack/min-queue **[+PATTERNS]** · LCA/binary lifting, Euler tour (x-ref Part 4) · treap, persistent structures **[DEEP, promote from probabilistic]** · bitsets **[DEEP]**; XOR/linear basis **[AWARE]** · HLD, centroid, link-cut **[AWARE — deferred]**.
- **Part 8 — Advanced graphs (lessons exist → add pattern/problem layer).** SCC (Tarjan/Kosaraju) **[+PATTERNS]** · bridges/articulation, biconnected, block-cut **[+PATTERNS]** · network flow (Ford-Fulkerson, Edmonds-Karp, Dinic), min-cut, min-cost-max-flow **[+PATTERNS]** · bipartite matching (Kuhn/Hopcroft-Karp), König, Hungarian **[+PATTERNS]** · 2-SAT **[+PATTERNS]** · Eulerian path/circuit **[NEW]** · (directed MST, Boruvka, Kirchhoff **[AWARE — deferred]**).
- **Part 9 — Strings (advanced; lessons exist → add pattern/problem layer).** KMP, Z, Rabin-Karp/polynomial hashing **[+PATTERNS]** · Aho-Corasick, suffix array(+LCP), suffix automaton **[+PATTERNS]** · Manacher **[NEW]** · (eertree, Lyndon **[AWARE]**) · string pattern layer **[+PATTERNS]**.
- **Part 10 — Advanced DP (selected, senior-met).** Bitmask DP (TSP), digit DP, DP-on-trees/rerooting **[NEW]** · monotonic-queue optimization **[NEW]** · probability/expectation DP, game DP **[AWARE]** · (CHT / D&C-opt / Knuth / slope-trick / Li-Chao **[AWARE — deferred]**).
- **Part 11 — Selected mathematics (light per scope).** Builds on the Part 0 primer. Modular inverse + applications **[NEW]** · sieve + factorization **[NEW light]** · combinatorics (nCr mod p, Pascal, inclusion-exclusion) **[NEW light]** · matrix exponentiation as a DP accelerator **[NEW light]** · game theory: Nim/Sprague-Grundy **[AWARE]** · (FFT/NTT, CRT, Pollard, Burnside, discrete log **[deferred — competitive-only]**).
- **Part 12 — Randomization, theory & systems (senior breadth).** Randomized algorithms (Las Vegas/Monte Carlo, randomized quicksort, reservoir sampling) **[STUB→DO]** · intractability awareness: P/NP/NP-complete/NP-hard, reductions, classic NPC **[NEW awareness]** · approximation (ratio bounds); LP/duality **[AWARE]** · streaming/sketches: Bloom, Count-Min, HyperLogLog **[DEEP]** · skip lists, persistent/functional, external-memory/cache-oblivious **[DEEP/partial]** · concurrency: CAS/atomics, lock-free queue, concurrent hashmap, RCU **[DEEP]** · DSA in real systems: Postgres B-tree, Linux RB/CFS, Redis, Git Merkle DAG, LSM, network plane **[DEEP]** · (computational geometry **[deferred — competitive-only]**).
- **Part 13 — Synthesis & mastery.** Meta-framework capstone (constraints→complexity→technique; pattern selection; "I've never seen this" attack plan) **[NEW]** · decision guides (which structure? which pattern?) **[NEW]** · mixed problem sets / mock interviews + communicating a solution (clarify → approach → complexity → edge cases) **[NEW]** · spaced-repetition recall decks **[DEEP]**.

**Deferred (competitive-only, not authored — listed so completeness is
auditable):** full math (FFT/NTT, CRT, Pollard's rho, Burnside, discrete log),
computational geometry, link-cut/HLD/centroid, the CP DP-optimization toolbox.

## The pattern taxonomy (patterns are a cross-cutting layer, not a stage)

Patterns recur at every structural level — ~14 groups, each attached to its Part.
Master set (the existing 74 ∪ the interview/CP canon), with recognition signals:

1. **Array/string scan** — prefix sums · difference arrays · two pointers (opposite / same-dir) · sliding window (fixed / variable) · Kadane · cyclic sort · interval merging · maximum overlap · sweep-line · coordinate compression
2. **List & cycles** — fast & slow (Floyd) · in-place reversal (k-group) · merge · split · reorder
3. **Stack / monotonic** — parsing/validation · monotonic stack · monotonic deque · min-stack/min-queue
4. **Heap / selection** — top-K · two heaps (median) · K-way merge · quickselect
5. **Binary search** — classic/rotated · lower/upper bound · binary search on the answer · ternary
6. **Trees** — BFS (level-order) · DFS (pre/in/post) · tree DP · rerooting · LCA/binary lifting · Euler-tour-to-range · trie traversal
7. **Graphs** — BFS/DFS/components · multi-source BFS · topological sort · union-find · Dijkstra/0-1 BFS/Bellman-Ford · bipartite/2-color · SCC/2-SAT · bridges/articulation · flow/matching · Eulerian
8. **Backtracking** — subsets · permutations · combinations · constraint search + pruning · meet-in-the-middle
9. **DP families** — 0/1 & unbounded knapsack · linear/Fibonacci · LCS/edit · LIS · palindrome · grid · interval · bitmask · digit · tree/rerooting
10. **Greedy** — sort-then-greedy · interval scheduling · Huffman · exchange-argument design
11. **Bit** — XOR trick · submask enumeration · bit tricks (`x&-x`, popcount) · XOR/linear basis
12. **Math (light)** — modular + fast-exp · sieve/factor · combinatorics (nCr, IE) · matrix exponentiation · Nim/Grundy
13. **Strings** — KMP/Z · polynomial hashing · Aho-Corasick · suffix array/automaton · Manacher
14. **Methodology** — constraint→complexity mapping · invariant-finding · brute-force-then-optimize

## Tier & content model

- **Spine lesson** (`tier: spine`): one concept, the six-step loop, density-
  budgeted, ≥1 visual, explicit `prereqs:`. The new primary file type — replaces
  the old 16-section LESSON template *for spine nodes* (the old templates stay
  valid for archive/depth).
- **Practice** (`tier: practice`): problems (existing ones linked), runnable.
- **Reference** (`tier: reference`): recall cards, decision/synthesis guides, the
  deep-dive archive.
- **Guided index:** a section's `index.md` renders the spine as "Start Here", with
  practice + reference demoted. Tier is **inferred from path** (optional `tier:`
  frontmatter override), proven non-breaking: `Frontmatter.parse` lifts only
  `title`/`summary`/`essential` and drops every other key, so `tier:`/`prereqs:`
  cannot break parsing or rendering. This extends the existing `essential` /
  `_section.json#defaultStatus` 2-tier primitive to a finer per-file 3-way split;
  the two stay orthogonal (`essential` drives sidebar emphasis, `tier` drives
  index grouping).
- **Archive is frozen.** The existing ~600 files become a read-only archive; the
  spine links *into* it; the archive is not required to link back — this prevents
  two-corpus drift. Promoting/moving a lesson changes its path→slug→inbound links
  and `prereqs:`; inbound links are rewritten in lockstep, guarded by a new
  `tools/dsa_prereq_lint.py`.
- **Derive-don't-manifest:** spine membership = tier-derived; spine order =
  numeric prefixes; the prerequisite chain = per-file `prereqs:` frontmatter. No
  new manifest files (honours the existing no-`meta.json` rule).

## Writing style — backbone + beginner-voice layer

Keep `DSA-WRITING-STYLE-GUIDE.md` as the backbone (4-part paragraph, ≤25-word
sentences, no hedging/filler, active voice, define-before-use, always state time
**and** space, the CLARIFY/SIMPLIFY/DEEPEN/CONTRAST modes). Layer on a
beginner-voice extension because the backbone targets a "technically capable
smart peer", and the audience here is a complete beginner: recalibrate the
assumed knowledge to the spine; second-person conversational voice; name the
misconception before it forms; show-before-name for brand-new concepts; analogy
discipline (one, fully mapped, then dropped, state where it breaks); acknowledge
difficulty + mark the payoff; ban more curse-of-knowledge words (*obviously,
clearly, of course, trivially, it's easy to see*); vary rhythm; predict-before-
reveal. The extension lives in the style guide; this ADR records that it exists.

## Consequences

- A new curriculum-level layer sits above the existing per-file templates. The
  spine + this taxonomy are the version-controlled source of truth (this ADR);
  the authoring template + style-guide extension are gitignored authoring scratch
  under `docs/_prompts/`; the old sweep-based authoring prompts are removed.
- The reader's entry point changes from a flat list to a guided "Start Here" path
  — a `tools/gen_cortex_index.py` change (tier inference + grouped output), pure
  markdown, no Scala/renderer change. The change is global: it reformats every
  section index on the next hook run.
- Coverage becomes even (breadth-first spine first, then deepen) rather than a few
  deep mansions beside 14 stubs. A beginner can read end-to-end at every stage.
- The 14 stubs (trie, AVL, red-black, B-tree, segment, Fenwick, DSU, divide-and-
  conquer, greedy, randomized + the pattern-thin graphs/strings layers) are
  scheduled for completion, not deferred.

## Honest caveats

- The **Visualise pipeline is in-flight** (`jvm-tracer-visualise-feature`, ADR-0023/
  0027). Step-2/step-4 visuals assume it traces a structure cleanly; where it does
  not (AVL/red-black rotations, B-trees, open-addressing probes, strings, math),
  the lesson falls back to hand-authored `d3 widget=` static frames or static D2/
  mermaid. The 16 bespoke renderers cover the common structures.
- **No new renderers are commissioned by this redesign** — visual gaps route to
  static diagrams. A future ADR may add renderers for rotations/B-trees if needed.
- This is a large multi-session program. The spine is a proposal open to
  re-ordering as authoring surfaces dependency problems; the prereq lint exists to
  catch forward references early.
