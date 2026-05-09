# Strings

A string is *technically* an array of characters, but treating it that way for serious work — substring matching, full-text search, plagiarism detection, DNA alignment — is what separates the people who finish in a coffee break from the people who time out at runtime. The algorithms in this module turn the naive O(nm) string-match into O(n+m), and they're the foundation behind `grep -F`, every search engine, every code editor's symbol jump, and every diff tool.

## Place in the curriculum

- **Prerequisites:** [Linear Structures](/cortex/data-structures-and-algorithms/linear-structures-index) (the array view of strings), [Trees](/cortex/data-structures-and-algorithms/trees-index) (the trie chapter is the bridge into prefix-based string algorithms).
- **Followed by:** [DSA in Real Systems](/cortex/data-structures-and-algorithms/dsa-in-real-systems-index) (where you'll see Aho-Corasick inside `grep -F` and rolling-hash inside `git diff`).

## Chapters

1. [Naive String Matching](/cortex/data-structures-and-algorithms/strings-string-matching-naive) — why O(nm) is the floor for the obvious algorithm.
2. [KMP (Knuth-Morris-Pratt)](/cortex/data-structures-and-algorithms/strings-kmp) — the failure function; O(n+m) substring match.
3. [Z-Algorithm](/cortex/data-structures-and-algorithms/strings-z-algorithm) — KMP's spiritual cousin; same O(n+m), different mental model.
4. [Rabin-Karp and Rolling Hash](/cortex/data-structures-and-algorithms/strings-rabin-karp-and-rolling-hash) — hash the substring; the trick that makes plagiarism detection feasible.
5. [Trie String Applications](/cortex/data-structures-and-algorithms/strings-trie-string-applications) — autocomplete, IP routing, word break.
6. [Suffix Array](/cortex/data-structures-and-algorithms/strings-suffix-array) — the compact alternative to a suffix tree.
7. [Suffix Automaton](/cortex/data-structures-and-algorithms/strings-suffix-automaton) — the minimal DFA that accepts every substring of a fixed string.
8. [Aho-Corasick](/cortex/data-structures-and-algorithms/strings-aho-corasick) — match a *set* of patterns against a stream in linear time. The reason `grep -F` is fast.
