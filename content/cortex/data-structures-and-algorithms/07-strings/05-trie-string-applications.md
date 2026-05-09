---
title: Trie String Applications
summary: A short tour of where tries solve string problems that hash tables and trees can't. Autocomplete, longest-common-prefix queries, dictionary-based word break, and IP routing.
prereqs:
  - trees-trie-introduction-to-tries
---

# 5. Trie String Applications

## The Hook

The [Trie chapter](/cortex/data-structures-and-algorithms/trees-trie-introduction-to-tries) introduced the structure: a tree where each path from root to a node spells out a string. This chapter is shorter; it's a tour of *where* tries beat hash tables and balanced BSTs in real string problems. The pattern is consistent: tries win when you need **prefix-shared computation**, when prefix queries are common, or when the alphabet is small and you're doing many lookups.

Five canonical applications: autocomplete, longest common prefix, dictionary-based word break, IP routing tables, and Aho-Corasick (the next chapter, which is a trie with failure links).

---

## Autocomplete

The hello-world of tries. Insert every word in your dictionary. To autocomplete a query, walk down the trie following the query's characters; collect every word in the subtree below.

`O(L)` to descend to the prefix node + `O(K · L_avg)` to enumerate the K matching words. A hash-set-based dictionary cannot do better than `O(N)` per autocomplete (scan all words).

For ranked autocomplete (Google-style), augment each trie node with a *priority queue of top-K completions* in its subtree. After inserts/updates, propagate priority changes up the spine — `O(L log K)` per update.

```python
# Pseudocode
def autocomplete(trie_root, prefix):
    node = walk(trie_root, prefix)
    if node is None: return []
    # DFS the subtree, collecting all words
    return [prefix + suffix for suffix in walk_subtree(node)]
```

***

## Longest Common Prefix

Given a list of strings, find the longest prefix common to all of them. Build a trie. Walk down from the root as long as each node has exactly one child *and* is not the end of any word. The longest path satisfying both is the LCP.

`O(total_chars)` to build, `O(LCP_length)` to extract.

For `n` strings of total length `T`, this beats the obvious `O(T)` character-by-character compare for *streaming* applications where strings arrive over time (you can update the trie incrementally) but not necessarily for one-shot calls.

***

## Word Break

Given a string `s` and a dictionary of valid words, can `s` be segmented into a sequence of words from the dictionary?

Build a trie of the dictionary. Use DP: `dp[i] = true` if `s[0..i]` can be segmented. For each `i`, walk the trie from `s[i]` onward, marking `dp[i + word_len] = true` whenever a complete word in the trie is found. `O(|s| × max_word_len)` worst case — much better than the `O(|s|² × |dict|)` you'd get from naive substring lookup.

```python
def word_break(s, dictionary):
    root = build_trie(dictionary)
    n = len(s)
    dp = [False] * (n + 1)
    dp[0] = True
    for i in range(n):
        if not dp[i]: continue
        node = root
        for j in range(i, n):
            if s[j] not in node.children: break
            node = node.children[s[j]]
            if node.is_end:
                dp[j + 1] = True
    return dp[n]
```

***

## IP Routing Tables

The Linux kernel's `fib_trie.c` is a Patricia trie (a compressed binary trie) over IP addresses. Every packet hits this trie for **longest prefix match**: which routing entry has the longest prefix that matches this destination IP?

A binary trie of 32-bit IP addresses has up to 32 levels — but Patricia compression collapses single-child chains, reducing height dramatically in practice. Linux's actual structure is a level-compressed (LC) trie that further packs the structure for cache friendliness.

The result: a single `ip route` lookup happens for every packet at line rate (millions of packets per second). The trie is read-mostly with infrequent updates; lock-free reads via RCU make this safe in the kernel.

***

## Sets of Strings with Prefix Operations

Hash sets give you `add`, `remove`, `contains` in `O(L)` (where `L` is string length). Tries give you the same, *plus*:

- `count_with_prefix(prefix)` — number of stored strings starting with `prefix`. With a per-node count, `O(L)`.
- `delete_all_with_prefix(prefix)` — delete an entire branch. `O(L)`.
- `iterate_in_sorted_order()` — DFS the trie. `O(N)` for `N` total characters.

Hash sets can't do any of those efficiently. When prefix-related queries are common in your application, the trie is the right data structure even if pure existence checks are slightly faster on a hash set.

***

# Implementation: Word Break

```python run
class TrieNode:
    __slots__ = ("children", "is_end")
    def __init__(self):
        self.children = {}
        self.is_end = False

def insert(root, word):
    node = root
    for ch in word:
        node = node.children.setdefault(ch, TrieNode())
    node.is_end = True

def word_break(s, dictionary):
    root = TrieNode()
    for w in dictionary:
        insert(root, w)
    n = len(s)
    dp = [False] * (n + 1)
    dp[0] = True
    for i in range(n):
        if not dp[i]:
            continue
        node = root
        for j in range(i, n):
            if s[j] not in node.children:
                break
            node = node.children[s[j]]
            if node.is_end:
                dp[j + 1] = True
    return dp[n]


if __name__ == "__main__":
    dictionary = ["leet", "code", "leetcode"]
    print(f"'leetcode' breakable: {word_break('leetcode', dictionary)}")    # True
    print(f"'leetcoder' breakable: {word_break('leetcoder', dictionary)}")  # False

    dictionary2 = ["apple", "pen", "applepen", "pine", "pineapple"]
    print(f"'pineapplepenapple' breakable: {word_break('pineapplepenapple', dictionary2)}")  # True
```

```java run
import java.util.*;

class Solution {
    static class TrieNode {
        Map<Character, TrieNode> children = new HashMap<>();
        boolean isEnd = false;
    }

    static void insert(TrieNode root, String w) {
        TrieNode node = root;
        for (char ch : w.toCharArray()) {
            node = node.children.computeIfAbsent(ch, k -> new TrieNode());
        }
        node.isEnd = true;
    }

    static boolean wordBreak(String s, List<String> dict) {
        TrieNode root = new TrieNode();
        for (String w : dict) insert(root, w);
        int n = s.length();
        boolean[] dp = new boolean[n + 1];
        dp[0] = true;
        for (int i = 0; i < n; i++) {
            if (!dp[i]) continue;
            TrieNode node = root;
            for (int j = i; j < n; j++) {
                node = node.children.get(s.charAt(j));
                if (node == null) break;
                if (node.isEnd) dp[j + 1] = true;
            }
        }
        return dp[n];
    }

    public static void main(String[] args) {
        System.out.println(wordBreak("leetcode", Arrays.asList("leet", "code", "leetcode")));
    }
}
```

***

# Edge cases and pitfalls

- **Adversarial-prefix dictionaries** can make tries memory-bloated. If every word starts with a unique character, the trie degenerates to a forest of independent paths — same as just storing the words.
- **Unicode tries get expensive.** A trie keyed by Unicode codepoints (~1.1M values) wastes most of its node space. Use a hash map of children instead of an array.
- **Concurrent updates.** Tries are not naturally thread-safe. For read-heavy/write-rare cases (IP routing), use RCU or copy-on-write techniques.

***

# Cross-links

- **Prerequisite:** [Trie](/cortex/data-structures-and-algorithms/trees-trie-introduction-to-tries) — the structural introduction.
- **Closely related:** [Aho-Corasick](/cortex/data-structures-and-algorithms/strings-aho-corasick) — a trie with failure links, for multi-pattern matching.
- **Production deep-dive:** [Network Data Plane](/cortex/data-structures-and-algorithms/dsa-in-real-systems-network-data-plane) — *stub* — IP routing in detail.

***

# Final takeaway

Tries shine when prefix structure matters. Three patterns to internalise:

1. **Tries beat hash sets when prefix queries are common.** Autocomplete, IP routing, prefix-counting — none of these are efficient on a hash set.
2. **Tries decompose problems into "walk down the trie".** Once you have the structure, many string problems collapse to a DFS or DP over the trie.
3. **Compression matters at scale.** Plain tries waste space on single-child chains. Production code (Linux LC-trie, DuckDB ART) uses compression for memory and cache reasons.
