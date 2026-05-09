---
title: Suffix Automaton
summary: The minimal DFA that accepts every substring of a fixed string. Linear size, linear construction, and once built, every substring query becomes a state-traversal.
prereqs:
  - strings-suffix-array
  - graphs-introduction-to-graphs
---

# 7. Suffix Automaton

## The Hook

A **suffix automaton (SAM)** is the smallest deterministic finite automaton (DFA) that recognises every substring of a fixed string `S`. Once constructed, asking "is `P` a substring of `S`?" reduces to walking the DFA on `P`'s characters — `O(|P|)` per query.

The suffix automaton has remarkable properties:

- **Linear size.** A suffix automaton on `S` of length `n` has at most `2n − 1` states and `3n − 4` transitions.
- **Linear construction.** Blumer et al. (1985) gave an online algorithm constructing SAM character-by-character in `O(n)`.
- **Linear-time queries.** Each substring check is `O(|P|)`. Each state corresponds to an *equivalence class* of substrings ending at the same set of positions.

The SAM is harder to visualise than a trie or a suffix array — it's a graph with structural invariants that take time to internalise. But it's the *fastest* known algorithm for several string problems: counting distinct substrings, finding the longest common substring of multiple strings, finding the lexicographically k-th smallest substring, and more.

This chapter is an introduction. The construction itself is one of the most beautiful algorithms in string processing; we'll sketch it and refer to longer treatments for the full proof.

---

## Table of contents

1. [What the SAM accepts](#what-the-sam-accepts)
2. [States and equivalence classes](#states-and-equivalence-classes)
3. [Construction sketch](#construction-sketch)
4. [Implementation](#implementation)
5. [Applications](#applications)
6. [Production reality](#production-reality)
7. [Cross-links](#cross-links)
8. [Final takeaway](#final-takeaway)

***

# What the SAM accepts

For a string `S` of length `n`, the suffix automaton on `S` is a DFA where:

- The initial state represents the empty string.
- Each state represents one or more substrings of `S` that share an *equivalence class* (defined below).
- An "accept" state corresponds to a *suffix* of `S`. (Sometimes every state is treated as accepting, since every state corresponds to at least one substring.)
- For a query `P`, walking from the start state along edges labelled `P[0], P[1], …, P[|P|-1]` either succeeds (P is a substring of S) or fails (P is not).

***

# States and equivalence classes

Two substrings of `S` are in the **same equivalence class** iff they end at the *same set of positions* in `S`. Formally: `endpos(u) = {i : S[i - |u| + 1 .. i] = u}`.

For example, if `S = "abcbc"`:

- `"bc"` ends at positions 2 and 4. `endpos("bc") = {2, 4}`.
- `"c"` also ends at positions 2 and 4. `endpos("c") = {2, 4}`.
- So `"bc"` and `"c"` are in the same equivalence class — *one state* in the SAM represents both.

Each state in the SAM corresponds to one equivalence class, and stores: the *longest substring* in that class, the *parent* state via the **suffix link** (which, follows the relation between equivalence classes), and outgoing transitions for each character.

The suffix-link tree is a separate structure with deep applications — many SAM-based algorithms walk it.

***

# Construction sketch

The online construction adds characters one at a time. After processing `S[0..i-1]`, the SAM accepts every substring of `S[0..i-1]`. Adding character `S[i]` creates at most two new states (typically one "main" state plus possibly one "clone" state to maintain the equivalence-class invariants).

The full construction is ~50 lines of code with subtle invariants. The key data structure: each state `s` stores `len[s]` (length of the longest string in the class), `link[s]` (the suffix link to the parent class), and a transition map.

Time: `O(n · |Σ|)` if transitions are stored in arrays, or `O(n log |Σ|)` with hash maps. Effectively `O(n)` for fixed alphabets.

***

# Implementation

A working suffix automaton in Python:

```python run
class SAMState:
    __slots__ = ("trans", "link", "length")
    def __init__(self):
        self.trans = {}
        self.link = -1
        self.length = 0

class SuffixAutomaton:
    def __init__(self):
        self.states = [SAMState()]                                              # initial state at index 0
        self.last = 0

    def extend(self, c):
        cur = len(self.states)
        self.states.append(SAMState())
        self.states[cur].length = self.states[self.last].length + 1

        p = self.last
        while p != -1 and c not in self.states[p].trans:
            self.states[p].trans[c] = cur
            p = self.states[p].link

        if p == -1:
            self.states[cur].link = 0
        else:
            q = self.states[p].trans[c]
            if self.states[p].length + 1 == self.states[q].length:
                self.states[cur].link = q
            else:
                clone = len(self.states)
                self.states.append(SAMState())
                self.states[clone].length = self.states[p].length + 1
                self.states[clone].link = self.states[q].link
                self.states[clone].trans = dict(self.states[q].trans)

                while p != -1 and self.states[p].trans.get(c) == q:
                    self.states[p].trans[c] = clone
                    p = self.states[p].link

                self.states[q].link = clone
                self.states[cur].link = clone

        self.last = cur

    def contains(self, pattern):
        s = 0
        for c in pattern:
            if c not in self.states[s].trans:
                return False
            s = self.states[s].trans[c]
        return True


if __name__ == "__main__":
    sam = SuffixAutomaton()
    s = "abcbc"
    for c in s:
        sam.extend(c)

    for query in ["abc", "bcb", "bcc", "abcbc", "abcbcb"]:
        print(f"is '{query}' a substring of '{s}'? {sam.contains(query)}")

    # Count distinct substrings
    # For each state s != 0, the number of substrings it represents is len[s] - len[link[s]]
    distinct = sum(sam.states[i].length - sam.states[sam.states[i].link].length for i in range(1, len(sam.states)))
    print(f"\nDistinct non-empty substrings of '{s}': {distinct}")
```

The Java/C/Scala variants follow the same algorithm; they're 60-80 lines each. Suffix automata are usually implemented in libraries rather than from scratch in production code.

***

# Applications

- **Substring queries.** `O(|P|)` per query after `O(n)` construction.
- **Counting distinct substrings.** `Σ (len[s] − len[link[s]])` over all non-initial states. `O(n)` after construction.
- **Longest common substring of two strings `S` and `T`.** Build SAM on `S`. Walk through `T` character by character; track the longest match.
- **Longest common substring of multiple strings.** Generalised suffix automaton, where each state remembers which input string contributed to it.
- **k-th lexicographically smallest substring.** Walk the SAM in lexicographic order, counting substrings as you go.

Each of these is `O(n)` or `O(n log n)`, hard to beat with any other structure.

***

# Production reality

- **Bioinformatics.** SAM (the data structure, not the bioinformatics file format) is used in some advanced sequence-matching tools, though FM-index and BWT-based methods dominate.
- **Plagiarism detection.** Generalised SAM finds the longest common substring of two documents in linear time, which is a stronger primitive than rolling-hash.
- **Competitive programming.** SAM is the "advanced string structure" — taught in IOI training, used in Codeforces problems where suffix arrays don't quite suffice.
- **No standard library ships a suffix automaton.** It's a niche enough structure that specialised libraries handle it.

***

# Cross-links

- **Prerequisites:** [Suffix Array](/cortex/data-structures-and-algorithms/strings-suffix-array) (similar information, different representation), [Graphs](/cortex/data-structures-and-algorithms/graphs-introduction-to-graphs) (the SAM is a DAG).
- **Sibling structure:** [Aho-Corasick](/cortex/data-structures-and-algorithms/strings-aho-corasick) — solves a different problem (multi-pattern matching), but uses similar failure-link ideas.

***

# Final takeaway

The suffix automaton is the minimal DFA recognising all substrings. Three patterns to internalise:

1. **Linear size, linear construction, linear queries.** No other suffix-based structure achieves all three.
2. **Equivalence classes are the abstraction.** A state = a set of substrings ending at the same positions. Many SAM algorithms walk the suffix-link tree exploiting this.
3. **Niche but unbeatable for specific problems.** Distinct-substring counting, multi-string LCS, k-th smallest substring — all `O(n)` via SAM, harder via other structures.
