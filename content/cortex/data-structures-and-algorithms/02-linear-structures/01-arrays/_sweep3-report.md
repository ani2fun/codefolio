# Sweep 3 — chapter coverage report
Chapter: `02-linear-structures/01-arrays`

This file is overwritten per session. One line per `inform`-mode section finished
during the rewrite — `author`-mode sections do not produce coverage rows.

## Pilot — `02-linear-structures/01-arrays/pattern-04-pattern-two-pointers`

04-pattern-two-pointers/01-pattern.md :: Recognition Checklist :: covered=[two-position-test, end-placement-test, inward-motion-test, constant-work-test]  skipped=[]  legacy=[N/A — author mode]
04-pattern-two-pointers/02-problems/01-flip-characters.md :: Intuition :: covered=[mirror-pair-structure, pointer-placement, single-pointer-fails-need-temp]  skipped=[]  legacy=[01-data-structure/01-arrays/03-pattern-two-pointers/03-flip-characters.md]
04-pattern-two-pointers/02-problems/02-palindrome-checker.md :: Intuition :: covered=[symmetry-property, branch-cascade, naive-reverse-copy-O(n)-space]  skipped=[]  legacy=[01-data-structure/01-arrays/03-pattern-two-pointers/04-palindrome-checker.md]
04-pattern-two-pointers/02-problems/02-palindrome-checker.md :: Approach :: covered=[empty-guard, init, branch-cascade, exit-return]  skipped=[]  legacy=[N/A — derived from frozen solution code]
04-pattern-two-pointers/02-problems/03-vowel-exchange.md :: Intuition :: covered=[mirror-pairing-of-vowels, asymmetric-pointer-motion, single-pointer-O(v)-list-cost]  skipped=[]  legacy=[01-data-structure/01-arrays/03-pattern-two-pointers/05-vowel-exchange.md]
04-pattern-two-pointers/02-problems/04-reverse-words.md :: Intuition :: covered=[word-as-sub-range, outer-scan-plus-inner-reverse, global-two-pointer-fails]  skipped=[]  legacy=[01-data-structure/01-arrays/03-pattern-two-pointers/06-reverse-words.md]
04-pattern-two-pointers/02-problems/05-reverse-segments.md :: Intuition :: covered=[2k-block-shape, stride-skip-untouched, min-clamp-folds-tails, branchless-vs-3-case]  skipped=[]  legacy=[N/A — no legacy match found for this problem]
04-pattern-two-pointers/02-problems/06-reverse-word-order.md :: Intuition :: covered=[two-reversals-cancel-inside-compound-outside, brute-force-split-join-cost]  skipped=[]  legacy=[01-data-structure/01-arrays/03-pattern-two-pointers/08-reverse-word-order.md]
04-pattern-two-pointers/03-memorize.md :: all sections :: covered=[in-hurry, mnemonic, analogy, visual, triggers, contrast-w-sliding-window, template, mistakes, MVE, quick-recall]  skipped=[]  legacy=[N/A — full PATTERN_MEMORIZE template authored fresh]

## Pilot — `02-linear-structures/01-arrays/lesson-01-introduction`

(LESSON_UNIT: all filled sections — Quiz, Further Reading — were `author` mode
per the spec table. CHAPTER_MEMORIZE was also entirely `author` mode. No
coverage rows produced. Migration of the embedded `## Memorize` into
`03-memorize.md` is recorded in the manifest as `memorize_migrated: true`.)

## Open questions surfaced by the pilot

- `01-pattern.md` keeps both the 4-row Fitting-the-Template table AND a verbose
  set of `### Checkpoint N` sub-explanations under Canonical Example. Pilot
  reviewer to decide whether the Checkpoint detail stays or compresses.
- `02-palindrome-checker.md` has an extra `What Failure Looks Like` `<details>`
  block sitting between Diagnostic Questions and the new Approach section.
  Diagram-driven; may want folding into Intuition.
- `06-reverse-word-order.md` carries an additional `What This Works — The
  Intuition` `<details>` plus a final `The Full Picture` overview — both
  valuable, both outside the canonical PROBLEM template. Left in place; Sweep
  4/5 can re-classify if a stricter template is preferred.

## `02-linear-structures/01-arrays/lesson-02-multidimensional`

(LESSON_UNIT: all filled sections — frontmatter summary, The Hook, Working
Example, Production Reality, Quiz, Practice Ladder, Further Reading,
Cross-Links, Final Takeaway — were `author` mode per the spec table. No
coverage rows produced. Existing body content was not rewritten.)

## `02-linear-structures/01-arrays/chapter-memorize-03-memorize`

03-memorize.md :: In a Hurry? :: covered=[lesson-01-introduction, lesson-02-multidimensional]  legacy=[N/A — author mode]
03-memorize.md :: One-Line Mnemonic :: covered=[lesson-01-introduction, lesson-02-multidimensional]  legacy=[N/A — author mode]
03-memorize.md :: Real-World Analogy :: covered=[lesson-01-introduction, lesson-02-multidimensional]  legacy=[N/A — author mode]
03-memorize.md :: Visual Summary :: covered=[lesson-01-introduction, lesson-02-multidimensional]  legacy=[N/A — author mode]
03-memorize.md :: Key Operations :: covered=[lesson-01-introduction, lesson-02-multidimensional]  legacy=[N/A — author mode]
03-memorize.md :: Common Mistakes :: covered=[lesson-01-introduction, lesson-02-multidimensional]  legacy=[N/A — author mode]
03-memorize.md :: Quick Recall :: covered=[lesson-01-introduction, lesson-02-multidimensional]  legacy=[N/A — author mode]

## `02-linear-structures/01-arrays/pattern-05-pattern-two-pointers-reduction`

01-pattern.md :: Understanding the Pattern :: covered=[Why Naive Isn't Enough, The Core Idea, How the Pointers Move]  legacy=[01-identifying-two-pointer-reduction.md]
01-pattern.md :: The Generic Algorithm :: covered=[four-step prose]  legacy=[01-identifying-two-pointer-reduction.md]
01-pattern.md :: Complexity Analysis :: covered=[time sort/greedy, space]  legacy=[02-code-solution.md]
01-pattern.md :: Variants / Taxonomy :: covered=[equality, inequality, dedup, greedy]  legacy=[01-identifying-two-pointer-reduction.md]
01-pattern.md :: Recognition Checklist :: covered=[Q1-Q4]  legacy=[01-identifying-two-pointer-reduction.md]
01-pattern.md :: Canonical Example :: covered=[problem, brute force, key insight, optimised, trace, fitting the template]  legacy=[01-identifying-two-pointer-reduction.md, 02-code-solution.md]
02-problems/01-two-sum.md :: Intuition :: covered=[structure, placement, what breaks]  legacy=[N/A — fresh problem before reduction]
02-problems/01-two-sum.md :: Approach :: covered=[7 numbered steps]  legacy=[N/A — fresh problem before reduction]
02-problems/01-two-sum.md :: Key Takeaway :: covered=[base case, complexities]  legacy=[N/A]
02-problems/02-target-limited-two-sum.md :: Intuition :: covered=[structure, placement, what breaks]  legacy=[03-target-limited-two-sum.md]
02-problems/02-target-limited-two-sum.md :: Approach :: covered=[6 numbered steps]  legacy=[03-target-limited-two-sum.md]
02-problems/02-target-limited-two-sum.md :: Edge Cases :: covered=[7 rows incl. sum-equals-target, single element, duplicates]  legacy=[03-target-limited-two-sum.md]
02-problems/02-target-limited-two-sum.md :: Key Takeaway :: covered=[diff vs Two Sum]  legacy=[N/A]
02-problems/03-duplicate-aware-two-sum.md :: Intuition :: covered=[structure, placement+skip, what breaks]  legacy=[04-duplicate-aware-two-sum.md]
02-problems/03-duplicate-aware-two-sum.md :: Approach :: covered=[7 numbered steps incl. skip-both-sides]  legacy=[04-duplicate-aware-two-sum.md]
02-problems/03-duplicate-aware-two-sum.md :: Edge Cases :: covered=[6 rows incl. empty, multi-pair]  legacy=[04-duplicate-aware-two-sum.md]
02-problems/03-duplicate-aware-two-sum.md :: Key Takeaway :: covered=[diff vs Two Sum]  legacy=[N/A]
02-problems/04-largest-container.md :: Intuition :: covered=[area-formula monotonicity, greedy rule, what breaks]  legacy=[05-largest-container.md]
02-problems/04-largest-container.md :: Approach :: covered=[5 numbered steps]  legacy=[05-largest-container.md]
02-problems/04-largest-container.md :: Edge Cases :: covered=[6 rows incl. equal endpoints, two-element min]  legacy=[05-largest-container.md]
02-problems/04-largest-container.md :: Key Takeaway :: covered=[diff vs sort-based reductions]  legacy=[N/A]
03-memorize.md :: In a Hurry? :: covered=[two-pointers-reduction-pattern]  legacy=[N/A — author mode]
03-memorize.md :: One-Line Mnemonic :: covered=[two-pointers-reduction-pattern]  legacy=[N/A — author mode]
03-memorize.md :: Real-World Analogy :: covered=[two-pointers-reduction-pattern]  legacy=[N/A — author mode]
03-memorize.md :: Visual Summary :: covered=[two-pointers-reduction-pattern]  legacy=[N/A — author mode]
03-memorize.md :: Pattern Recognition Triggers :: covered=[two-pointers-reduction-pattern]  legacy=[N/A — author mode]
03-memorize.md :: Don't Confuse With :: covered=[Direct Two Pointers, Sliding Window]  legacy=[N/A — author mode]
03-memorize.md :: Template Code :: covered=[generic skeleton]  legacy=[N/A — author mode]
03-memorize.md :: Common Mistakes :: covered=[5 nested entries]  legacy=[N/A — author mode]
03-memorize.md :: Minimum Viable Example :: covered=[4-element sorted demo]  legacy=[N/A — author mode]
03-memorize.md :: Quick Recall :: covered=[7 Q&A]  legacy=[N/A — author mode]

## `02-linear-structures/01-arrays/pattern-06-pattern-two-pointers-subproblem`

06-pattern-two-pointers-subproblem/01-pattern.md :: Understanding the Pattern :: covered=[multi-dim-search, segment-sequence, fix-and-reduce-shape]  skipped=[]  legacy=[01-data-structure/01-arrays/05-pattern-two-pointers-subproblem/01-identifying-two-pointer-subproblem.md]
06-pattern-two-pointers-subproblem/01-pattern.md :: Generic Algorithm :: covered=[identify-subproblem, set-invariant, drive-outer, run-inner]  skipped=[]  legacy=[N/A — derived from frozen K Rotations / Three Sum / Four Sum solutions]
06-pattern-two-pointers-subproblem/01-pattern.md :: Complexity Analysis :: covered=[fix-and-reduce-O(n^d), sequence-O(n)]  skipped=[]  legacy=[N/A — derived from current-book Two-Sum / Three-Sum / Four-Sum complexity numbers]
06-pattern-two-pointers-subproblem/01-pattern.md :: Variants / Taxonomy :: covered=[sequence-of-transformations, fix-one-and-reduce, fix-two-and-reduce, k-Sum-generalisation]  skipped=[]  legacy=[N/A — taxonomy derived from in-chapter problem list]
06-pattern-two-pointers-subproblem/01-pattern.md :: Recognition Checklist :: covered=[decomposable, subproblem-fits-two-pointer, decisive-direction, O(1)-per-step]  skipped=[]  legacy=[01-data-structure/01-arrays/05-pattern-two-pointers-subproblem/01-identifying-two-pointer-subproblem.md]
06-pattern-two-pointers-subproblem/01-pattern.md :: Canonical Example :: covered=[problem-statement, brute-force, key-insight-three-reversals, optimized-solution, trace, fitting-template]  skipped=[]  legacy=[01-data-structure/01-arrays/05-pattern-two-pointers-subproblem/01-identifying-two-pointer-subproblem.md]
06-pattern-two-pointers-subproblem/02-problems/01-k-rotations.md :: Intuition :: covered=[head-tail-split-structure, reverse-pointer-placement, naive-shift-O(nk)-temp-O(n)]  skipped=[]  legacy=[01-data-structure/01-arrays/05-pattern-two-pointers-subproblem/02-interview-recommended-k-rotations.md]
06-pattern-two-pointers-subproblem/02-problems/01-k-rotations.md :: Approach :: covered=[normalize-k, reverse-all, reverse-first-k, reverse-last-n-k]  skipped=[]  legacy=[N/A — derived from frozen solution code]
06-pattern-two-pointers-subproblem/02-problems/01-k-rotations.md :: Edge Cases :: covered=[k=0, k=n, k>n, n=1, k=1, n=2/k=1]  skipped=[]  legacy=[01-data-structure/01-arrays/05-pattern-two-pointers-subproblem/02-interview-recommended-k-rotations.md]
06-pattern-two-pointers-subproblem/02-problems/02-three-sum.md :: Intuition :: covered=[3-dim-constraint-fix-one, layered-pointer-placement, naive-O(n^3)-vs-sorted-O(n^2)]  skipped=[]  legacy=[01-data-structure/01-arrays/05-pattern-two-pointers-subproblem/03-code-solution.md]
06-pattern-two-pointers-subproblem/02-problems/02-three-sum.md :: Approach :: covered=[sort, outer-i-skip-dup, inner-two-pointer-skip-dup, return-result]  skipped=[]  legacy=[N/A — derived from frozen solution code]
06-pattern-two-pointers-subproblem/02-problems/02-three-sum.md :: Edge Cases :: covered=[all-zeros, all-positive-early-exit, single-triplet, length<3, empty, multi-dup]  skipped=[]  legacy=[N/A — six rows derived from frozen Python test cases]
06-pattern-two-pointers-subproblem/02-problems/03-approximate-three-sum.md :: Intuition :: covered=[3-dim-closest-fix-one, decisive-direction-distance, naive-O(n^3)-vs-sorted-O(n^2)]  skipped=[]  legacy=[01-data-structure/01-arrays/05-pattern-two-pointers-subproblem/04-approximate-three-sum.md]
06-pattern-two-pointers-subproblem/02-problems/03-approximate-three-sum.md :: Approach :: covered=[sort, init-tracker, outer-i-no-skip, inner-distance-tracker, return-closest]  skipped=[]  legacy=[N/A — derived from frozen solution code]
06-pattern-two-pointers-subproblem/02-problems/03-approximate-three-sum.md :: Edge Cases :: covered=[exact-match, all-same, all-positive-small-target, n=3-minimum, negative-target, closest-below-target]  skipped=[]  legacy=[N/A — six rows derived from frozen Python test cases]
06-pattern-two-pointers-subproblem/02-problems/04-four-sum.md :: Intuition :: covered=[k-Sum-recursive-shape, three-layer-pointer-placement, naive-O(n^4)-vs-O(n^3)]  skipped=[]  legacy=[01-data-structure/01-arrays/05-pattern-two-pointers-subproblem/05-interview-recommended-four-sum.md]
06-pattern-two-pointers-subproblem/02-problems/04-four-sum.md :: Approach :: covered=[sort, outer-i-skip-dup, outer-j-skip-dup-with-j>i+1, need-formula, inner-two-pointer-skip-dup, return-result]  skipped=[]  legacy=[N/A — derived from frozen solution code]
06-pattern-two-pointers-subproblem/02-problems/04-four-sum.md :: Edge Cases :: covered=[all-same, no-quadruplet, minimum-length, large-dup-set, negative-target, length<4]  skipped=[]  legacy=[N/A — six rows derived from frozen Python test cases]
06-pattern-two-pointers-subproblem/03-memorize.md :: all sections :: covered=[in-hurry, mnemonic, analogy, visual, triggers, contrast-with-reduction, template-code, mistakes, MVE-three-sum, quick-recall]  skipped=[]  legacy=[N/A — full PATTERN_MEMORIZE template authored fresh]

## `02-linear-structures/01-arrays/pattern-07-pattern-simultaneous-traversal`

07-pattern-simultaneous-traversal/01-pattern.md :: Why Naive Isn't Enough :: covered=[nested-scan-O(NxM)-cost, restart-is-the-waste, merge-example]  skipped=[]  legacy=[01-data-structure/01-arrays/06-pattern-simultaneous-traversal/01-understanding-the-simultaneous-traversal-pattern.md]
07-pattern-simultaneous-traversal/01-pattern.md :: The Core Idea :: covered=[one-index-per-array, lock-step-advance, conveyor-belt-analogy, never-rewind]  skipped=[]  legacy=[01-data-structure/01-arrays/06-pattern-simultaneous-traversal/01-understanding-the-simultaneous-traversal-pattern.md]
07-pattern-simultaneous-traversal/01-pattern.md :: How the Pointers Move :: covered=[main-loop-both-arrays, two-cleanup-loops, distinction-from-converging-two-pointers]  skipped=[]  legacy=[01-data-structure/01-arrays/06-pattern-simultaneous-traversal/01-understanding-the-simultaneous-traversal-pattern.md]
07-pattern-simultaneous-traversal/01-pattern.md :: The Generic Algorithm :: covered=[6-step-numbered-flow, advance-condition-table]  skipped=[]  legacy=[01-data-structure/01-arrays/06-pattern-simultaneous-traversal/01-understanding-the-simultaneous-traversal-pattern.md (Algorithm)]
07-pattern-simultaneous-traversal/01-pattern.md :: Complexity Analysis :: covered=[O(N+M)-time-derivation, O(1)-space, brute-force-O(N*M)-contrast]  skipped=[]  legacy=[01-data-structure/01-arrays/06-pattern-simultaneous-traversal/01-understanding-the-simultaneous-traversal-pattern.md (Complexity analysis)]
07-pattern-simultaneous-traversal/01-pattern.md :: Canonical Example :: covered=[subsequence-problem-statement, brute-force-nested, key-insight-collapse, optimised-solution, trace, fitting-the-template-4-row]  skipped=[]  legacy=[01-data-structure/01-arrays/06-pattern-simultaneous-traversal/02-identifying-the-simultaneous-traversal-pattern.md]
07-pattern-simultaneous-traversal/02-problems/01-subsequence-checker.md :: Examples :: covered=[abc-ahbgdc-true, axc-ahbgdc-false, empty-s-true]  skipped=[]  legacy=[01-data-structure/01-arrays/06-pattern-simultaneous-traversal/03-subsequence-checker.md]
07-pattern-simultaneous-traversal/02-problems/01-subsequence-checker.md :: Intuition :: covered=[order-preserving-inclusion, two-cursor-asymmetry, brute-force-O(NM)-fragility]  skipped=[]  legacy=[01-data-structure/01-arrays/06-pattern-simultaneous-traversal/03-subsequence-checker.md]
07-pattern-simultaneous-traversal/02-problems/01-subsequence-checker.md :: Approach :: covered=[init, main-loop-comparison, conditional-index1-advance, unconditional-index2-advance, final-check]  skipped=[]  legacy=[N/A — derived from frozen solution code]
07-pattern-simultaneous-traversal/02-problems/01-subsequence-checker.md :: Edge Cases :: covered=[empty-s, empty-t, both-empty, s-longer-than-t, equal, all-same, missing-char, match-at-end]  skipped=[]  legacy=[N/A — author mode]
07-pattern-simultaneous-traversal/02-problems/02-merge-sorted-arrays.md :: Examples :: covered=[append-to-tail, interleave, empty-arr2]  skipped=[]  legacy=[01-data-structure/01-arrays/06-pattern-simultaneous-traversal/04-merge-sorted-arrays.md]
07-pattern-simultaneous-traversal/02-problems/02-merge-sorted-arrays.md :: Intuition :: covered=[in-place-asymmetry, three-pointer-backward-fill, write-pointer-never-catches-read]  skipped=[]  legacy=[01-data-structure/01-arrays/06-pattern-simultaneous-traversal/04-merge-sorted-arrays.md]
07-pattern-simultaneous-traversal/02-problems/02-merge-sorted-arrays.md :: Approach :: covered=[3-index-init, comparison-driven-write, decrement-winner, leftover-arr2-copy]  skipped=[]  legacy=[N/A — derived from frozen solution code]
07-pattern-simultaneous-traversal/02-problems/02-merge-sorted-arrays.md :: Edge Cases :: covered=[arr2-empty, arr1-no-valid, all-arr2-larger, all-arr2-smaller, perfect-interleave, tie]  skipped=[]  legacy=[N/A — author mode]
07-pattern-simultaneous-traversal/02-problems/03-unique-intersections.md :: Examples :: covered=[basic-with-duplicates, no-intersection, all-duplicates-one-record]  skipped=[]  legacy=[01-data-structure/01-arrays/06-pattern-simultaneous-traversal/05-unique-intersections.md]
07-pattern-simultaneous-traversal/02-problems/03-unique-intersections.md :: Intuition :: covered=[set-intersection-semantics, sort-gives-decisive-direction, duplicate-skip-guard]  skipped=[]  legacy=[01-data-structure/01-arrays/06-pattern-simultaneous-traversal/05-unique-intersections.md]
07-pattern-simultaneous-traversal/02-problems/03-unique-intersections.md :: Approach :: covered=[sort-step, init, three-way-comparison, conditional-record, no-cleanup]  skipped=[]  legacy=[N/A — derived from frozen solution code]
07-pattern-simultaneous-traversal/02-problems/03-unique-intersections.md :: Edge Cases :: covered=[no-common, all-match, one-empty, all-duplicates, single-common, identical-singletons]  skipped=[]  legacy=[N/A — author mode]
07-pattern-simultaneous-traversal/02-problems/04-repeated-intersections.md :: Examples :: covered=[mixed-multiplicities, min-pq-rule, no-overlap]  skipped=[]  legacy=[01-data-structure/01-arrays/06-pattern-simultaneous-traversal/06-repeated-intersections.md]
07-pattern-simultaneous-traversal/02-problems/04-repeated-intersections.md :: Intuition :: covered=[multiset-intersection-semantics, both-advance-on-match-gives-min-pq, frequency-map-brute-force-O(N+M)-space]  skipped=[]  legacy=[01-data-structure/01-arrays/06-pattern-simultaneous-traversal/06-repeated-intersections.md]
07-pattern-simultaneous-traversal/02-problems/04-repeated-intersections.md :: Approach :: covered=[sort-step, init, three-way-comparison, unconditional-record, no-cleanup]  skipped=[]  legacy=[N/A — derived from frozen solution code]
07-pattern-simultaneous-traversal/02-problems/04-repeated-intersections.md :: Edge Cases :: covered=[no-overlap, all-match, one-empty, subset, all-duplicates, single-match]  skipped=[]  legacy=[N/A — author mode]
07-pattern-simultaneous-traversal/03-memorize.md :: all sections :: covered=[in-hurry, mnemonic, analogy, visual, triggers, contrast-w-converging-two-pointers, template, mistakes, MVE, quick-recall]  skipped=[]  legacy=[N/A — full PATTERN_MEMORIZE template authored fresh]
## `02-linear-structures/01-arrays/pattern-08-pattern-fixed-sliding-window`

08-pattern-fixed-sliding-window/01-pattern.md :: Recognition Checklist :: covered=[fixed-size, O(1)-add-remove, output-shape, edge-cases]  skipped=[]  legacy=[01-data-structure/01-arrays/07-pattern-fixed-sized-sliding-window/02-identifying-the-fixed-sized-sliding-window-pattern.md]
08-pattern-fixed-sliding-window/01-pattern.md :: Canonical Example (Fitting the Template) :: covered=[aggregate=sum, f_add=+=, f_remove=-=, process=max-avg]  skipped=[]  legacy=[01-data-structure/01-arrays/07-pattern-fixed-sized-sliding-window/02-identifying-the-fixed-sized-sliding-window-pattern.md]
08-pattern-fixed-sliding-window/01-pattern.md :: Canonical Example (Key Insight) :: covered=[adjacent-window-overlap-k-1, O(k)-to-O(1)-per-step, concrete-trace]  skipped=[]  legacy=[01-data-structure/01-arrays/07-pattern-fixed-sized-sliding-window/01-understanding-the-fixed-sized-sliding-window-pattern.md]
08-pattern-fixed-sliding-window/01-pattern.md :: Variants / Taxonomy :: covered=[single-best, per-window-report, multi-aggregate]  skipped=[]  legacy=[N/A — taxonomy derived from in-section problem list]
08-pattern-fixed-sliding-window/02-problems/01-subarray-size-equals-k.md :: Intuition :: covered=[fixed-size-property, start-end-placement, naive-O(n*k)-shared-work]  skipped=[]  legacy=[01-data-structure/01-arrays/07-pattern-fixed-sized-sliding-window/03-subarray-size-equals-k.md]
08-pattern-fixed-sliding-window/02-problems/01-subarray-size-equals-k.md :: Applying the Diagnostic Questions :: covered=[Q1-fixed-size, Q2-O(1)-update, Q3-single-best, Q4-edges]  skipped=[]  legacy=[01-data-structure/01-arrays/07-pattern-fixed-sized-sliding-window/03-subarray-size-equals-k.md]
08-pattern-fixed-sliding-window/02-problems/01-subarray-size-equals-k.md :: Approach :: covered=[k>n-guard, init, expand, contract, process-min, advance, return]  skipped=[]  legacy=[N/A — derived from frozen solution code]
08-pattern-fixed-sliding-window/02-problems/02-maximum-ones.md :: Intuition :: covered=[binary-aggregate-count, conditional-O(1)-update, naive-O(n*k)]  skipped=[]  legacy=[N/A — legacy file 04-interview-recommended.md exists for the section but not this exact variant]
08-pattern-fixed-sliding-window/02-problems/02-maximum-ones.md :: Applying the Diagnostic Questions :: covered=[Q1-fixed-size, Q2-O(1)-conditional, Q3-single-best, Q4-edges]  skipped=[]  legacy=[N/A — derived from current-book Maximum Ones spec]
08-pattern-fixed-sliding-window/02-problems/02-maximum-ones.md :: Approach :: covered=[init, expand-conditional, contract-conditional, process-max, advance, return]  skipped=[]  legacy=[N/A — derived from frozen solution code]
08-pattern-fixed-sliding-window/02-problems/03-negative-window.md :: Intuition :: covered=[per-window-output, sign-check-O(1)-update, naive-O(n*k)]  skipped=[]  legacy=[01-data-structure/01-arrays/07-pattern-fixed-sized-sliding-window/05-negative-window.md]
08-pattern-fixed-sliding-window/02-problems/03-negative-window.md :: Applying the Diagnostic Questions :: covered=[Q1-fixed-size, Q2-O(1)-sign-check, Q3-per-window-report, Q4-edges]  skipped=[]  legacy=[01-data-structure/01-arrays/07-pattern-fixed-sized-sliding-window/05-negative-window.md]
08-pattern-fixed-sliding-window/02-problems/03-negative-window.md :: Approach :: covered=[init, expand-conditional, contract-conditional, process-append, advance, return]  skipped=[]  legacy=[N/A — derived from frozen solution code]
08-pattern-fixed-sliding-window/02-problems/04-even-odd-count.md :: Intuition :: covered=[multi-aggregate-partition, parity-O(1)-update, naive-O(n*k)]  skipped=[]  legacy=[01-data-structure/01-arrays/07-pattern-fixed-sized-sliding-window/06-even-odd-count.md]
08-pattern-fixed-sliding-window/02-problems/04-even-odd-count.md :: Applying the Diagnostic Questions :: covered=[Q1-fixed-size, Q2-O(1)-parity-check, Q3-per-window-pair, Q4-edges]  skipped=[]  legacy=[01-data-structure/01-arrays/07-pattern-fixed-sized-sliding-window/06-even-odd-count.md]
08-pattern-fixed-sliding-window/02-problems/04-even-odd-count.md :: Approach :: covered=[init, expand-parity-split, contract-parity-split, process-append-tuple, advance, return]  skipped=[]  legacy=[N/A — derived from frozen solution code]

## `02-linear-structures/01-arrays/pattern-09-pattern-variable-sliding-window`
09-pattern-variable-sliding-window/01-pattern.md :: Recognition Checklist :: covered=[single-result, O(1)-add, O(1)-remove, provable-skipping]  skipped=[]  legacy=[01-data-structure/01-arrays/08-pattern-variable-sized-sliding-window/02-identifying-the-variable-sized-sliding-window-pattern.md]
09-pattern-variable-sliding-window/01-pattern.md :: Canonical Example :: covered=[problem-statement, brute-force, key-insight-invariant, optimized-solution-leap, trace, fitting-template]  skipped=[full-proof — moved to problem-03-maximum-subarray-sum]  legacy=[01-data-structure/01-arrays/08-pattern-variable-sized-sliding-window/02-identifying-the-variable-sized-sliding-window-pattern.md]
09-pattern-variable-sliding-window/02-problems/01-consecutive-ones.md :: Intuition :: covered=[no-zeros-invariant, implicit-start-after-last-zero, brute-O(N^3)-vs-single-pass-O(N)]  skipped=[]  legacy=[01-data-structure/01-arrays/08-pattern-variable-sized-sliding-window/03-consecutive-ones.md]
09-pattern-variable-sliding-window/02-problems/01-consecutive-ones.md :: Approach :: covered=[init, ones-increment, zeros-reset-and-update, end-advance, final-check, return]  skipped=[]  legacy=[N/A — derived from frozen solution code]
09-pattern-variable-sliding-window/02-problems/01-consecutive-ones.md :: Edge Cases :: covered=[empty, all-zeros, all-ones, single-one, trailing-ones, single-zero]  skipped=[]  legacy=[N/A — derived from frozen Python test cases]
09-pattern-variable-sliding-window/02-problems/02-product-conundrum.md :: Intuition :: covered=[monotonic-growth-positive-ints, product<k-invariant, while-shrink-not-leap, end-start+1-counting-trick]  skipped=[]  legacy=[01-data-structure/01-arrays/08-pattern-variable-sized-sliding-window/04-product-conundrum.md]
09-pattern-variable-sliding-window/02-problems/02-product-conundrum.md :: Approach :: covered=[init, multiply-arr-end, while-shrink-divide, count-end-start+1, end-advance, return]  skipped=[]  legacy=[N/A — derived from frozen solution code]
09-pattern-variable-sliding-window/02-problems/02-product-conundrum.md :: Edge Cases :: covered=[k<=1, single-elem-ge-k, all-lt-k, empty, product=k, large-single-elem]  skipped=[]  legacy=[N/A — derived from frozen Python test cases]
09-pattern-variable-sliding-window/02-problems/03-maximum-subarray-sum.md :: Intuition :: covered=[sign-driven-dominance, sum>=0-invariant, leap-on-negative-prefix, seed-arr[0]-not-0]  skipped=[]  legacy=[01-data-structure/01-arrays/08-pattern-variable-sized-sliding-window/05-maximum-subarray-sum.md]
09-pattern-variable-sliding-window/02-problems/03-maximum-subarray-sum.md :: Approach :: covered=[empty-guard, seed-arr[0], leap-on-neg, extend-otherwise, update-max-sum, advance-end, return]  skipped=[]  legacy=[N/A — derived from frozen solution code]
09-pattern-variable-sliding-window/02-problems/03-maximum-subarray-sum.md :: Proof of Correctness :: covered=[case-1-start-extending-beyond-end, case-2-mid-start-extending-beyond, case-3-mid-start-ending-within, full-conclusion]  skipped=[]  legacy=[01-data-structure/01-arrays/08-pattern-variable-sized-sliding-window/02-identifying-the-variable-sized-sliding-window-pattern.md]
09-pattern-variable-sliding-window/02-problems/03-maximum-subarray-sum.md :: Edge Cases :: covered=[all-neg, single-elem, all-pos, huge-neg-prefix, zero-prefix, empty]  skipped=[]  legacy=[N/A — derived from frozen Python test cases]
09-pattern-variable-sliding-window/02-problems/04-consecutive-ones-with-k-flips.md :: Intuition :: covered=[at-most-k-zeros-invariant, generalises-consecutive-ones, while-shrink-multi-step, k=0-special-case]  skipped=[]  legacy=[01-data-structure/01-arrays/08-pattern-variable-sized-sliding-window/06-consecutive-ones-with-k-flips.md]
09-pattern-variable-sliding-window/02-problems/04-consecutive-ones-with-k-flips.md :: Approach :: covered=[init, count-zero-on-end, while-shrink-decrementing-on-zero, update-max-len, advance-end, return]  skipped=[]  legacy=[N/A — derived from frozen solution code]
09-pattern-variable-sliding-window/02-problems/04-consecutive-ones-with-k-flips.md :: Edge Cases :: covered=[k=0, k-ge-zeros, all-ones, all-zeros-k=0, all-zeros-k-ge-n, single-zero-k=1]  skipped=[]  legacy=[N/A — derived from frozen Python test cases]


## `02-linear-structures/01-arrays/pattern-10-pattern-interval-merging`

10-pattern-interval-merging/01-pattern.md :: Recognition Checklist :: covered=[1d-axis, overlap-matters, merged-form-suffices, single-pass-after-sort]  skipped=[]  legacy=[03-identifying-the-interval-merging-pattern.md]
10-pattern-interval-merging/01-pattern.md :: Canonical Example Brute Force :: covered=[pairwise-O(N^2)-per-pass, repeat-until-stable, O(N^3)-worst]  skipped=[]  legacy=[02-understanding-the-interval-merging-pattern.md]
10-pattern-interval-merging/01-pattern.md :: Canonical Example Key Insight :: covered=[sort-by-start-forces-consecutive-overlap, last-merged-as-state]  skipped=[]  legacy=[02-understanding-the-interval-merging-pattern.md]
10-pattern-interval-merging/01-pattern.md :: Fitting the Template :: covered=[sort, seed, compare, extend-or-append]  skipped=[]  legacy=[N/A — author-mode template recipe]
10-pattern-interval-merging/02-problems/01-verify-schedule.md :: Examples :: covered=[basic-conflict, sort-fixes-order, touching-allowed]  skipped=[]  legacy=[04-verify-schedule.md]
10-pattern-interval-merging/02-problems/01-verify-schedule.md :: Intuition :: covered=[1d-ordering, consecutive-pair-state, naive-O(N^2)]  skipped=[]  legacy=[04-verify-schedule.md]
10-pattern-interval-merging/02-problems/01-verify-schedule.md :: Approach :: covered=[sort, walk-i-from-1, strict-less-than-detect, return-false-early]  skipped=[]  legacy=[04-verify-schedule.md]
10-pattern-interval-merging/02-problems/01-verify-schedule.md :: Dry Run :: covered=[example-1-trace, touching-trace]  skipped=[]  legacy=[N/A — existing baseline traces re-used]
10-pattern-interval-merging/02-problems/02-overlap-reduction.md :: Intuition :: covered=[1d-ordering, last-merged-state, naive-O(N^3)]  skipped=[]  legacy=[05-overlap-reduction.md]
10-pattern-interval-merging/02-problems/02-overlap-reduction.md :: Approach :: covered=[sort, seed, walk, extend-on-overlap-with-max, append-otherwise, return-merged]  skipped=[]  legacy=[05-overlap-reduction.md]
10-pattern-interval-merging/02-problems/02-overlap-reduction.md :: Dry Run :: covered=[example-1-trace]  skipped=[]  legacy=[N/A — existing baseline trace re-used]
10-pattern-interval-merging/02-problems/03-employee-free-time.md :: Intuition :: covered=[complement-reduction, merge-then-gap-pass, naive-time-axis-O(T*N)]  skipped=[]  legacy=[06-employee-free-time.md]
10-pattern-interval-merging/02-problems/03-employee-free-time.md :: Approach :: covered=[sort, merge, gap-pass, return-free-times, no-tail-or-head-emit]  skipped=[]  legacy=[06-employee-free-time.md]
10-pattern-interval-merging/02-problems/03-employee-free-time.md :: Dry Run :: covered=[example-1-trace]  skipped=[]  legacy=[N/A — existing baseline trace re-used]
10-pattern-interval-merging/02-problems/04-insert-interval.md :: Examples :: covered=[basic-absorb, multi-absorb, empty-input]  skipped=[]  legacy=[07-insert-interval.md]
10-pattern-interval-merging/02-problems/04-insert-interval.md :: Intuition :: covered=[sorted-disjoint-precondition, three-phases-contiguous, naive-resort-O(N-log-N)]  skipped=[]  legacy=[07-insert-interval.md]
10-pattern-interval-merging/02-problems/04-insert-interval.md :: Approach :: covered=[phase1-copy-before, phase2-absorb-with-min-max, push-grown, phase3-copy-after]  skipped=[]  legacy=[07-insert-interval.md]
10-pattern-interval-merging/02-problems/04-insert-interval.md :: Dry Run :: covered=[multi-absorb-trace, no-overlap-trace]  skipped=[]  legacy=[N/A — existing baseline traces re-used]

## `02-linear-structures/01-arrays/pattern-11-pattern-maximum-overlap`
11-pattern-maximum-overlap/01-pattern.md :: Recognition Checklist :: covered=[interval-input, peak-concurrency-paraphrase, state-changes-at-events, O(1)-per-event]  skipped=[]  legacy=[01-data-structure/01-arrays/10-pattern-maximum-overlap/03-identifying-maximum-overlap-pattern.md]
11-pattern-maximum-overlap/01-pattern.md :: Canonical Example :: covered=[problem-statement, brute-force-O(N^2)-pairwise, key-insight-event-only-changes, optimized-solution-flowchart-and-impl, trace, fitting-template-4-row]  skipped=[]  legacy=[01-data-structure/01-arrays/10-pattern-maximum-overlap/04-minimum-meeting-rooms.md]
11-pattern-maximum-overlap/02-problems/01-minimum-meeting-rooms.md :: Intuition :: covered=[structure-single-time-axis, placement-as-pm1-events, what-breaks-pairwise-O(N^2)]  skipped=[]  legacy=[01-data-structure/01-arrays/10-pattern-maximum-overlap/04-minimum-meeting-rooms.md]
11-pattern-maximum-overlap/02-problems/01-minimum-meeting-rooms.md :: Approach :: covered=[split-tagged-points, sort-ties-e-before-s, init-counters, sweep-pm1-track-max, return-no-collapse]  skipped=[]  legacy=[N/A — derived from frozen solution code]
11-pattern-maximum-overlap/02-problems/02-remove-intervals.md :: Intuition :: covered=[structure-pairwise-disjoint-not-depth, placement-greedy-by-end, what-breaks-peak-overlap-answers-wrong-question]  skipped=[]  legacy=[01-data-structure/01-arrays/10-pattern-maximum-overlap/05-remove-intervals.md]
11-pattern-maximum-overlap/02-problems/02-remove-intervals.md :: Approach :: covered=[empty-guard, sort-by-end, init-count-end, walk-keep-or-skip, return-n-minus-count]  skipped=[]  legacy=[N/A — derived from frozen solution code]
11-pattern-maximum-overlap/02-problems/03-busiest-interval.md :: Intuition :: covered=[structure-where-not-how-high, placement-strict-gt-and-first-e, what-breaks-pairwise-intersection-O(N^2)]  skipped=[]  legacy=[01-data-structure/01-arrays/10-pattern-maximum-overlap/06-busiest-interval.md]
11-pattern-maximum-overlap/02-problems/03-busiest-interval.md :: Approach :: covered=[split-tagged-points, sort-e-before-s, init-counters-and-window-state, sweep-update-start-on-strict-peak-end-on-first-e, return-with-le-1-collapse]  skipped=[]  legacy=[N/A — derived from frozen solution code]
11-pattern-maximum-overlap/02-problems/04-peak-resource-requirement.md :: Intuition :: covered=[structure-weighted-counter, placement-pm-resources, what-breaks-pairwise-sums-miss-clusters-or-need-aux-counter]  skipped=[]  legacy=[01-data-structure/01-arrays/10-pattern-maximum-overlap/07-peak-resource-requirement.md]
11-pattern-maximum-overlap/02-problems/04-peak-resource-requirement.md :: Approach :: covered=[split-with-resource, sort-e-before-s, pre-scan-maxSingleJob, init-counters-and-window-state, sweep-weighted-update, return-with-single-job-collapse]  skipped=[]  legacy=[N/A — derived from frozen solution code]

## `02-linear-structures/01-arrays/design-12-design-a-dynamic-array`

12-design-a-dynamic-array/01-design-a-dynamic-array.md :: The Hook :: covered=[fixed-vs-dynamic-array-divide, geometric-doubling-thesis, standard-library-state-machine]  skipped=[]  legacy=[01-data-structure/01-arrays/11-design/01-design-a-dynamic-array.md]
12-design-a-dynamic-array/01-design-a-dynamic-array.md :: What Does "Amortised O(1)" Mean? :: covered=[whole-sequence-guarantee, average-case-vs-amortised-distinction, 8-push-cumulative-15-vs-64]  skipped=[]  legacy=[N/A — concept not present in legacy; derived from current-book frozen trace tables]
12-design-a-dynamic-array/01-design-a-dynamic-array.md :: The Core Insight — Grow by Doubling :: covered=[naive-grow-by-one-O(N^2), doubling-series-resize-count, doubling-series-copy-cost, growth-factor-tradeoff-1.01-vs-10]  skipped=[]  legacy=[N/A — derived from frozen Solution code's doubling logic]
12-design-a-dynamic-array/01-design-a-dynamic-array.md :: Applying the Diagnostic Questions (Q1–Q4 prose) :: covered=[grow-on-demand-rationale, arithmetic-vs-geometric-numbers-1000-push, contiguous-vs-linked-cache-cost, peak-100-percent-overhead-near-100-percent]  skipped=[]  legacy=[N/A — diagnostic question framing is current-book convention; numbers derived from frozen code's resize cadence]
12-design-a-dynamic-array/01-design-a-dynamic-array.md :: Solution & Analysis :: covered=[amortised-bound-3N-derivation, space-2x-trade-off, edge-cases-table-host-language-bounds]  skipped=[]  legacy=[01-data-structure/01-arrays/11-design/01-design-a-dynamic-array.md (Solution example list)]
12-design-a-dynamic-array/01-design-a-dynamic-array.md :: Why Reimplement What the Standard Library Already Gives You? :: covered=[performance-debugging, capacity-hints-reserve, interview-signal]  skipped=[]  legacy=[N/A — no equivalent legacy section; current-book framing]
12-design-a-dynamic-array/01-design-a-dynamic-array.md :: Final Takeaway :: covered=[size-vs-capacity-separation, doubling-on-overflow, headroom-cost-of-amortised-O(1)]  skipped=[]  legacy=[N/A — current-book takeaway framing]
## `02-linear-structures/01-arrays/synthesis-13-pattern-synthesis`

13-pattern-synthesis.md :: When to Use Which Pattern :: covered=[two-pointers, two-pointers-reduction, two-pointers-subproblem, simultaneous-traversal, fixed-sliding-window, variable-sliding-window, interval-merging, maximum-overlap]  legacy=[N/A — author mode]
13-pattern-synthesis.md :: Pattern Comparison Table :: covered=[two-pointers, two-pointers-reduction, two-pointers-subproblem, simultaneous-traversal, fixed-sliding-window, variable-sliding-window, interval-merging, maximum-overlap]  legacy=[N/A — author mode]
13-pattern-synthesis.md :: Common Confusions :: covered=[tp-vs-tp-reduction, tp-reduction-vs-tp-subproblem, tp-vs-simultaneous, fixed-vs-variable-window, interval-merging-vs-max-overlap, variable-window-vs-tp-reduction]  legacy=[N/A — author mode]
13-pattern-synthesis.md :: Synthesis Problems :: covered=[largest-container, remove-intervals, longest-subarray-sum-at-most-k]  legacy=[N/A — author mode]

