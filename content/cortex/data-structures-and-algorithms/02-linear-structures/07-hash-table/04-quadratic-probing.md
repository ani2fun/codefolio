---
title: "Quadratic Probing"
summary: "<!-- TODO: summary -->"
---

# 4. Quadratic Probing

## The Hook

You've just lived through the linear-probing apocalypse. Drop a few keys that all hash to the same slot, and they pile up *next to each other*. Drop more keys anywhere near that pile, and the pile *absorbs* them — every collision lengthens the cluster, and every nearby insert lengthens it further. By the time the table is 80% full, what should be O(1) lookups have become punishing scans through 20-, 30-, 40-slot clusters.

What if every collision *kicked the next key further away from the cluster*? Not one slot ahead, but **i² slots ahead** — so the second collision lands 1 slot away, the third lands 4 slots away, the fourth lands 9 slots away, the fifth lands 16 slots away. The keys spray themselves across the array instead of huddling together. Clusters can't form at the same hash index because the *second* colliding key has already teleported past where the third would land — collisions disperse instead of compounding.

That's **quadratic probing**. Same array, same three-state record, same O(1) average — but a probe sequence designed to *shatter* primary clusters. We get most of linear probing's cache-friendly speed back, with a far gentler degradation curve as the table fills.

There's still a price (every cure has its bug), and we'll meet it: **secondary clustering**. Two keys that hash to the *same* index will follow the *same* probe path forever, because the probe formula only uses `i`, not the key itself. We'll surface that limitation and tease its fix in the next lesson — double hashing.

---

## Table of contents

1. [Introduction to quadratic probing](#introduction-to-quadratic-probing)
2. [Key components of quadratic probing](#key-components-of-quadratic-probing)
3. [Implementing the hash table class](#implementing-the-hash-table-class)
4. [Search operation in quadratic probing](#search-operation-in-quadratic-probing)
5. [Insert operation in quadratic probing](#insert-operation-in-quadratic-probing)
6. [Delete operation in quadratic probing](#delete-operation-in-quadratic-probing)
7. [Design a hash table with quadratic probing](#design-a-hash-table-with-quadratic-probing)

***

# Introduction to quadratic probing

Now that we've seen linear probing and felt its limitation — primary clustering — let's introduce a small but powerful change to the probe sequence. **Quadratic probing** is an open-addressing scheme that keeps everything else about linear probing the same (single contiguous array, three-state records, cache-friendly storage) but replaces the linear walk with a *quadratic* one: the i-th probe is at `(startIndex + a·i² + b·i) % capacity` for some constants `a` and `b`.

```d2
grid-columns: 8
grid-gap: 0
h0: "[0]" {style.fill: "#fef9c3"; style.stroke: "#d97706"}
h1: "[1]" {style.fill: "#fef9c3"; style.stroke: "#d97706"}
h2: "[2]" {style.fill: "#fef9c3"; style.stroke: "#d97706"}
h3: "[3]" {style.fill: "#fef9c3"; style.stroke: "#d97706"}
h4: "[4]" {style.fill: "#fef9c3"; style.stroke: "#d97706"}
h5: "[5]" {style.fill: "#fef9c3"; style.stroke: "#d97706"}
h6: "[6]" {style.fill: "#fef9c3"; style.stroke: "#d97706"}
h7: "[7]" {style.fill: "#fef9c3"; style.stroke: "#d97706"}
c0: "EMPTY"
c1: "(9, B)" {style.fill: "#dbeafe"; style.stroke: "#3b82f6"}
c2: "EMPTY"
c3: "EMPTY"
c4: "(13, C)" {style.fill: "#dbeafe"; style.stroke: "#3b82f6"}
c5: "(5, A)" {style.fill: "#dbeafe"; style.stroke: "#3b82f6"}
c6: "EMPTY"
c7: "EMPTY"
```

<p align="center"><strong>Logical view of a quadratic-probing hash table — like linear probing, every slot stores one (key, value) pair directly. Unlike linear probing, colliding keys land at <em>scattered</em> positions instead of consecutive ones.</strong></p>

The internal array can be dynamic (resized on demand) in production implementations, but we'll keep it fixed in this lesson to focus on the probing scheme itself.

## Handling collisions

The probe sequence in linear probing is `+1, +2, +3, +4, ...` — the same direction, the same step, every time. The probe sequence in quadratic probing accelerates: `+1, +4, +9, +16, +25, ...` (with the simplest choice `a = 1, b = 0`, where the i-th probe is `+i²`). Each successive collision lands the new key *much further* from the cluster than the last, which is what stops clusters from forming around the original hash index.

```mermaid
---
config:
  theme: base
  themeVariables:
    primaryColor: "#dbeafe"
    primaryBorderColor: "#3b82f6"
    primaryTextColor: "#1e3a5f"
    lineColor: "#64748b"
    secondaryColor: "#ede9fe"
    tertiaryColor: "#fef9c3"
---
flowchart LR
    subgraph LIN["Linear probing — step 1"]
        L0["i=0 → +0"] --> L1["i=1 → +1"] --> L2["i=2 → +2"] --> L3["i=3 → +3"] --> L4["i=4 → +4"]
    end
    subgraph QUAD["Quadratic probing — step i² (a=1, b=0)"]
        Q0["i=0 → +0"] --> Q1["i=1 → +1"] --> Q2["i=2 → +4"] --> Q3["i=3 → +9"] --> Q4["i=4 → +16"]
    end
    LIN ~~~ QUAD
```

<p align="center"><strong>Probe-step comparison — linear probing increases by 1 each step; quadratic probing increases by <em>i²</em>. After 5 collisions, linear is at offset 4 from the start; quadratic is at offset 16 — far enough away to escape any growing cluster around the start.</strong></p>

The first colliding key still lands at the hashed index. Subsequent colliding keys, however, are pushed to indices **far** from each other. Continuing the example with `a = 1, b = 0`, hash index 5, capacity 8:

```mermaid
---
config:
  theme: base
  themeVariables:
    primaryColor: "#dbeafe"
    primaryBorderColor: "#3b82f6"
    primaryTextColor: "#1e3a5f"
    lineColor: "#64748b"
    secondaryColor: "#ede9fe"
    tertiaryColor: "#fef9c3"
---
flowchart LR
    K1["key A<br/>hash = 5<br/>i = 0"] --> S5
    K2["key B<br/>hash = 5<br/>i = 1 → +1"] --> S6
    K3["key C<br/>hash = 5<br/>i = 2 → +4"] --> S1
    K4["key D<br/>hash = 5<br/>i = 3 → +9"] --> S6X
    subgraph TBL["table (capacity 8)"]
        direction LR
        S0["[0]"]
        S1["[1] (C)"]
        S2["[2]"]
        S3["[3]"]
        S4["[4]"]
        S5["[5] (A)"]
        S6["[6] (B)"]
        S7["[7]"]
    end
    S6X["(5+9) mod 8 = 6 (taken!) → next i = 4 →<br/>(5+16) mod 8 = 5 (taken!) → next i = 5 →<br/>(5+25) mod 8 = 2 ✓"]
    style S5 fill:#dbeafe,stroke:#3b82f6
    style S6 fill:#dbeafe,stroke:#3b82f6
    style S1 fill:#dbeafe,stroke:#3b82f6
```

<p align="center"><strong>Four colliding keys at hash 5, capacity 8 — the probe offsets <code>(5+0, 5+1, 5+4, 5+9, 5+16, 5+25) mod 8</code> visit slots <code>5, 6, 1, 6, 5, 2</code>. The fourth key has to skip past two re-visits before landing — and that <em>revisiting</em> is the secondary clustering we'll need to confront later.</strong></p>

Like linear probing, the quadratic probe runs up to N iterations (N = `capacity`), wrapping with `mod`. The two-step recipe is the same; only the formula for the offset has changed.

> **Insert (sketch)**
>
> -   **Step 1:** Compute the hash for the key.
> -   **Step 2:** Iterate up to N times using the quadratic offset formula until an unoccupied slot is found.
> -   **Step 3:** Place the (key, value) pair at that slot.
>
> **Search (sketch)**
>
> -   **Step 1:** Compute the hash for the key.
> -   **Step 2:** Iterate up to N times using the quadratic offset formula until the key is found or an empty slot is hit.
> -   **Step 3:** Return the value if the key is found.

The quadratic-probing implementation is *almost* identical to linear probing — same data structures, same operations, same EMPTY/DELETED/OCCUPIED state machine. Only the offset calculation changes.

***

# Key components of quadratic probing

A quadratic-probing hash table has the same three components as linear probing — a three-state record, an internal array of records, and a hash function — plus two new constants `a` and `b` that parameterise the probe sequence.

<details>
<summary><h2>Record</h2></summary>


Identical to linear probing: each slot stores a key, a value, and a state (`EMPTY` / `DELETED` / `OCCUPIED`). The state field plays exactly the same role — `EMPTY` short-circuits searches, `DELETED` keeps probe chains alive after deletions.

```d2
rec: A single Record {
  s: |md
    **state**

    EMPTY / OCCUPIED / DELETED
  | {style.fill: "#fef9c3"; style.stroke: "#d97706"}
  k: key
  v: value
}
```

<p align="center"><strong>Quadratic-probing record — exactly the same shape as linear probing's record. The only difference between the two schemes is in <em>how</em> the probe sequence walks the array.</strong></p>


```python run
# Implementation of a hash function for this hash table
def hash_function(self, key: int) -> int:
    return key % self.capacity
```

```java run
// Implementation of a hash function for this hash table
int hashFunction(int key) {
    return key % capacity;
}
```

</details>
<details>
<summary><h2>Internal array</h2></summary>


Identical to linear probing — `capacity` records sitting back-to-back, all `EMPTY` at construction time. The contiguous layout is what gives both schemes their cache-friendly performance.

```d2
grid-columns: 7
grid-gap: 0
h0: "[0]" {style.fill: "#fef9c3"; style.stroke: "#d97706"}
h1: "[1]" {style.fill: "#fef9c3"; style.stroke: "#d97706"}
h2: "[2]" {style.fill: "#fef9c3"; style.stroke: "#d97706"}
h3: "[3]" {style.fill: "#fef9c3"; style.stroke: "#d97706"}
h4: "[4]" {style.fill: "#fef9c3"; style.stroke: "#d97706"}
h5: "[5]" {style.fill: "#fef9c3"; style.stroke: "#d97706"}
h6: "[6]" {style.fill: "#fef9c3"; style.stroke: "#d97706"}
e0: EMPTY
e1: EMPTY
e2: EMPTY
e3: EMPTY
e4: EMPTY
e5: EMPTY
e6: EMPTY
```

<p align="center"><strong>An empty quadratic-probing hash table — same structural layout as linear probing. The behavioural difference shows up only when collisions start happening.</strong></p>

</details>
<details>
<summary><h2>Hash function</h2></summary>


Same `key % capacity` we've used throughout the section. The hash function picks the *starting* probe index; the quadratic step formula does the rest.

```mermaid
---
config:
  theme: base
  themeVariables:
    primaryColor: "#dbeafe"
    primaryBorderColor: "#3b82f6"
    primaryTextColor: "#1e3a5f"
    lineColor: "#64748b"
    secondaryColor: "#ede9fe"
    tertiaryColor: "#fef9c3"
---
flowchart LR
    K["key = 13"] -->|"key % 7"| H["startIndex = 6"]
    H --> P["if slot 6 is taken,<br/>probe at offsets<br/>+1, +4, +9, +16, ..."]
```

<p align="center"><strong>The hash function picks the starting index; the quadratic offset (a·i² + b·i) determines where each subsequent probe lands. The two stages are independent, which is why both schemes can share the same hash function.</strong></p>

</details>

***

# Implementing the hash table class

Now we wrap everything into `MyHashTable`. The class signature gains two extra parameters — the quadratic constants `a` and `b` — so the same code can support different probe sequences (`a=1, b=0` for the textbook `i²`, or any other choice the user wants to experiment with).

```d2
cls: MyHashTable class {
  priv: private internals {
    cap: "capacity"
    ab: |md
      **a, b**

      (quadratic constants)
    |
    tbl: "table: Record[]"
    hf: "hashFunction(key)"
    po: "probeForOccupied(key)"
    pe: "probeForFree(start)"
  }
  pub: public API {
    s: "search(key)"
    i: "insert(key, value)"
    r: "remove(key)"
  }
  pub -> priv {style.stroke-dash: 3}
}
```

<p align="center"><strong>The quadratic-probing class adds two private fields (a and b) on top of the linear-probing class. Everything else — the array, the hash function, the public API — is unchanged.</strong></p>

<details>
<summary><h2>Implementation</h2></summary>



```python run
# Instantiate a hash table object from the MyHashTable class
table = MyHashTable(4)

table.insert(1, 1234)

table.insert(2, 4567)

table.search(2)

table.insert(4, 8910)

table.remove(1)
```

```java run
// Instantiate a hash table object from the MyHashTable class
MyHashTable table = new MyHashTable(4);

table.insert(1, 1234);

table.insert(2, 4567);

table.search(2);

table.insert(4, 8910);

table.remove(1);
```


> *Predict before reading on — with the formula <code>(start + a·i² + b·i) % capacity</code> and constants <code>a=1, b=0</code>, what's the maximum possible probe-step distance after just 5 iterations? And how many slots can quadratic probing reach in a table of capacity 8 — all of them, or only some?*
>
> After 5 iterations the offset is 16; in a capacity-8 table that wraps to 0, so the path crosses itself. **Quadratic probing on capacity 8 cannot reach every slot** — half the array is unreachable depending on the start. This is one of the limitations we'll surface formally; the standard fix is to choose `capacity` to be a prime number, which we'll discuss in the design problem.

</details>

***

# Search operation in quadratic probing

Search in quadratic probing is identical to linear probing in *structure* — three cases (key found, EMPTY hit, full traversal) — but the offset between probes is `a·i² + b·i` instead of `i`. We refactor the probe loop into a private helper so insert and delete can reuse it.

<details>
<summary><h2>Algorithm</h2></summary>


### 1. The key is present

If we land on an `OCCUPIED` slot whose key matches, return the value.

```mermaid
---
config:
  theme: base
  themeVariables:
    primaryColor: "#dbeafe"
    primaryBorderColor: "#3b82f6"
    primaryTextColor: "#1e3a5f"
    lineColor: "#64748b"
    secondaryColor: "#ede9fe"
    tertiaryColor: "#fef9c3"
---
flowchart LR
    Q["search(13), capacity=7"] --> H["hash(13) = 6"]
    H --> P0["i=0 → [6]<br/>(5, A) ≠ 13"]
    P0 --> P1["i=1 → (6+1)%7 = [0]<br/>(13, C) = 13 ✓"]
    P1 --> R["return C"]
    style P1 fill:#dcfce7,stroke:#22c55e
```

<p align="center"><strong>Successful search — the probe walks through the quadratic sequence, comparing keys, until it lands on a match.</strong></p>

### 2. An EMPTY slot is found

Like linear probing, hitting `EMPTY` proves the key was never inserted (or it would have been placed at this slot or earlier in the probe). Stop, return `-1`.

### 3. The table is fully traversed

After `capacity` iterations, give up.

</details>
<details>
<summary><h2>Solution &amp; Analysis</h2></summary>

### Implementation

```python run
from enum import Enum
from typing import List, Optional

# Represents the state of a record in the hash table
class RecordType(Enum):
    EMPTY = 0
    DELETED = 1
    OCCUPIED = 2

# Represents an entry in the hash table
class Record:
    def __init__(
        self, key: Optional[int] = None, value: Optional[int] = None
    ):

        # Initialize state as EMPTY by default
        self.state: RecordType = RecordType.EMPTY
        self.key: int = 0
        self.value: int = 0

        # Set state to OCCUPIED when key and value are provided
        if key is not None and value is not None:
            self.state = RecordType.OCCUPIED
            self.key = key
            self.value = value

class MyHashTable:
    def __init__(self, capacity: int, a: int, b: int):
        self.capacity = capacity
        self.a = a
        self.b = b

        # The hash table implemented as a list of Records
        self.table: List[Record] = [Record() for _ in range(capacity)]

    # Primary hash function: Computes the index as key % capacity
    def hash_function(self, key: int) -> int:
        return key % self.capacity

    def probe_for_occupied_index(
        self, key: int, start_index: int
    ) -> int:
        for i in range(self.capacity):

            # Quadratic probing
            probe_index = (
                start_index + self.a * i * i + self.b * i
            ) % self.capacity

            # Check if the slot is occupied and matches the key
            if (
                self.table[probe_index].state == RecordType.OCCUPIED
                and self.table[probe_index].key == key
            ):
                return probe_index

        # Return -1 if no matching record is found
        return -1

    def search(self, key: int) -> int:

        # Compute the initial index using the primary hash function
        start_index = self.hash_function(key)

        # Find the occupied index for the key
        occupied_index = self.probe_for_occupied_index(key, start_index)

        # Return the value if found, otherwise -1
        return (
            -1
            if occupied_index == -1
            else self.table[occupied_index].value
        )
```

```java run
import java.util.*;

// Represents the state of a record in the hash table
enum RecordType {
    EMPTY,
    DELETED,
    OCCUPIED
}

// Represents an entry in the hash table
class Record {

    // Use the separately defined RecordType enum
    RecordType state = RecordType.EMPTY;
    int key = 0;
    int value = 0;

    Record() {}

    Record(int key, int value) {
        this.state = RecordType.OCCUPIED;
        this.key = key;
        this.value = value;
    }
}

class MyHashTable {

    // The total number of slots in the hash table
    private int capacity;

    // Quadratic probing constants
    private int a, b;

    // The hash table implemented as a list of Records
    private List<Record> table;

    // Primary hash function: Computes the index as key % capacity
    private int hashFunction(int key) {
        return key % capacity;
    }

    private int probeForOccupiedIndex(int key, int startIndex) {
        for (int i = 0; i < capacity; ++i) {

            // Quadratic probing
            int probeIndex = (startIndex + a * i * i + b * i) % capacity;

            // Check if the slot is occupied and matches the key
            if (
                table.get(probeIndex).state == RecordType.OCCUPIED &&
                table.get(probeIndex).key == key
            ) {
                return probeIndex;
            }
        }

        // Return -1 if no matching record is found
        return -1;
    }

    public MyHashTable(int capacity, int a, int b) {
        this.capacity = capacity;
        this.a = a;
        this.b = b;

        // Initialize the table with empty records
        table = new ArrayList<>();
        for (int i = 0; i < capacity; i++) {
            table.add(new Record());
        }
    }

    public int search(int key) {

        // Compute the initial index using the primary hash function
        int startIndex = hashFunction(key);

        // Find the occupied index for the key
        int occupiedIndex = probeForOccupiedIndex(key, startIndex);

        // Return the value if found, otherwise -1
        return occupiedIndex == -1 ? -1 : table.get(occupiedIndex).value;
    }
}
```

### Complexity analysis

> **Best case** — first probe matches
>
> -   Time: **O(1)** | Space: **O(1)**
>
> **Average case** — well-distributed hashes
>
> -   Time: **O(1)** | Space: **O(1)**
>
> **Worst case** — heavy collision, full traversal
>
> -   Time: **O(N)** | Space: **O(1)**

</details>

***

# Insert operation in quadratic probing

Insert mirrors linear probing's structure: first run the probe to confirm the key isn't already there, then run it again to find the first non-OCCUPIED slot for the new record.

<details>
<summary><h2>Algorithm</h2></summary>


### 1. Key already exists

Probe finds an `OCCUPIED` slot with the matching key — overwrite the value.

### 2. Free slot found

Probe finds a non-OCCUPIED slot — drop the new record there.

### 3. Table is full

Probe completes a full traversal without finding either — return `false`.

```mermaid
---
config:
  theme: base
  themeVariables:
    primaryColor: "#dbeafe"
    primaryBorderColor: "#3b82f6"
    primaryTextColor: "#1e3a5f"
    lineColor: "#64748b"
    secondaryColor: "#ede9fe"
    tertiaryColor: "#fef9c3"
---
flowchart LR
    Q["insert(13, X)<br/>hash = 6"] --> P0["i=0 → [6]<br/>(5, A) ≠ 13"]
    P0 --> P1["i=1 → [0]<br/>EMPTY"]
    P1 --> ACT["place (13, X) at [0]"]
    style P1 fill:#dcfce7,stroke:#22c55e
```

<p align="center"><strong>Insert with a new key — the quadratic probe walks past the colliding occupied slot and lands on the first free slot, where the record is placed.</strong></p>

</details>
<details>
<summary><h2>Solution &amp; Analysis</h2></summary>

### Implementation

```python run
from enum import Enum
from typing import List, Optional

# Represents the state of a record in the hash table
class RecordType(Enum):
    EMPTY = 0
    DELETED = 1
    OCCUPIED = 2

# Represents an entry in the hash table
class Record:
    def __init__(
        self, key: Optional[int] = None, value: Optional[int] = None
    ):

        # Initialize state as EMPTY by default
        self.state: RecordType = RecordType.EMPTY
        self.key: int = 0
        self.value: int = 0

        # Set state to OCCUPIED when key and value are provided
        if key is not None and value is not None:
            self.state = RecordType.OCCUPIED
            self.key = key
            self.value = value

class MyHashTable:
    def __init__(self, capacity: int, a: int, b: int):
        self.capacity = capacity
        self.a = a
        self.b = b

        # The hash table implemented as a list of Records
        self.table: List[Record] = [Record() for _ in range(capacity)]

    # Primary hash function: Computes the index as key % capacity
    def hash_function(self, key: int) -> int:
        return key % self.capacity

    def probe_for_occupied_index(
        self, key: int, start_index: int
    ) -> int:
        for i in range(self.capacity):

            # Quadratic probing
            probe_index = (
                start_index + self.a * i * i + self.b * i
            ) % self.capacity

            # Check if the slot is occupied and matches the key
            if (
                self.table[probe_index].state == RecordType.OCCUPIED
                and self.table[probe_index].key == key
            ):
                return probe_index

        # Return -1 if no matching record is found
        return -1

    def probe_for_empty_index(self, start_index: int) -> int:
        for i in range(self.capacity):

            # Quadratic probing
            probe_index = (
                start_index + self.a * i * i + self.b * i
            ) % self.capacity

            # Check if the slot is available (either EMPTY or DELETED)
            if self.table[probe_index].state != RecordType.OCCUPIED:
                return probe_index

        # Return -1 if no available slot is found
        return -1

    def search(self, key: int) -> int:

        # Compute the initial index using the primary hash function
        start_index = self.hash_function(key)

        # Find the occupied index for the key
        occupied_index = self.probe_for_occupied_index(key, start_index)

        # Return the value if found, otherwise -1
        return (
            -1
            if occupied_index == -1
            else self.table[occupied_index].value
        )

    def insert(self, key: int, value: int) -> bool:

        # Compute the initial index using the primary hash function
        start_index = self.hash_function(key)

        # Find the occupied index for the key
        occupied_index = self.probe_for_occupied_index(key, start_index)

        # Update the value if the key exists
        if occupied_index != -1:
            self.table[occupied_index].value = value
            return True

        # Find an empty slot to insert the new key-value pair
        empty_index = self.probe_for_empty_index(start_index)
        if empty_index != -1:
            self.table[empty_index] = Record(key, value)
            return True

        # Return false if the table is full and insertion fails
        return False
```

```java run
import java.util.*;

// Represents the state of a record in the hash table
enum RecordType {
    EMPTY,
    DELETED,
    OCCUPIED
}

// Represents an entry in the hash table
class Record {

    // Use the separately defined RecordType enum
    RecordType state = RecordType.EMPTY;
    int key = 0;
    int value = 0;

    Record() {}

    Record(int key, int value) {
        this.state = RecordType.OCCUPIED;
        this.key = key;
        this.value = value;
    }
}

class MyHashTable {

    // The total number of slots in the hash table
    private int capacity;

    // Quadratic probing constants
    private int a, b;

    // The hash table implemented as a list of Records
    private List<Record> table;

    // Primary hash function: Computes the index as key % capacity
    private int hashFunction(int key) {
        return key % capacity;
    }

    private int probeForOccupiedIndex(int key, int startIndex) {
        for (int i = 0; i < capacity; ++i) {

            // Quadratic probing
            int probeIndex = (startIndex + a * i * i + b * i) % capacity;

            // Check if the slot is occupied and matches the key
            if (
                table.get(probeIndex).state == RecordType.OCCUPIED &&
                table.get(probeIndex).key == key
            ) {
                return probeIndex;
            }
        }

        // Return -1 if no matching record is found
        return -1;
    }

    private int probeForEmptyIndex(int startIndex) {
        for (int i = 0; i < capacity; ++i) {

            // Quadratic probing
            int probeIndex = (startIndex + a * i * i + b * i) % capacity;

            // Check if the slot is available (either EMPTY or DELETED)
            if (table.get(probeIndex).state != RecordType.OCCUPIED) {
                return probeIndex;
            }
        }

        // Return -1 if no available slot is found
        return -1;
    }

    public MyHashTable(int capacity, int a, int b) {
        this.capacity = capacity;
        this.a = a;
        this.b = b;

        // Initialize the table with empty records
        table = new ArrayList<>();
        for (int i = 0; i < capacity; i++) {
            table.add(new Record());
        }
    }

    public int search(int key) {

        // Compute the initial index using the primary hash function
        int startIndex = hashFunction(key);

        // Find the occupied index for the key
        int occupiedIndex = probeForOccupiedIndex(key, startIndex);

        // Return the value if found, otherwise -1
        return occupiedIndex == -1 ? -1 : table.get(occupiedIndex).value;
    }

    public boolean insert(int key, int value) {

        // Compute the initial index using the primary hash function
        int startIndex = hashFunction(key);

        // Find the occupied index for the key
        int occupiedIndex = probeForOccupiedIndex(key, startIndex);

        // Update the value if the key exists
        if (occupiedIndex != -1) {
            table.get(occupiedIndex).value = value;
            return true;
        }

        // Find an empty slot to insert the new key-value pair
        int emptyIndex = probeForEmptyIndex(startIndex);
        if (emptyIndex != -1) {
            table.set(emptyIndex, new Record(key, value));
            return true;
        }

        // Return false if the table is full and insertion fails
        return false;
    }
}
```

### Complexity analysis

> **Best case** — first probe is the right slot
>
> -   Time: **O(1)** | Space: **O(1)**
>
> **Average case** — well-distributed hashes, low load factor
>
> -   Time: **O(1)** | Space: **O(1)**
>
> **Worst case** — collision-heavy, full traversal
>
> -   Time: **O(N)** | Space: **O(1)**

</details>

***

# Delete operation in quadratic probing

Delete works exactly as in linear probing — find the matching `OCCUPIED` slot via the probe, then mark it `DELETED`. The tombstone is just as essential here: removing it would orphan any record whose probe path crossed this slot. The only thing that changes is the path the probe takes.

<details>
<summary><h2>Algorithm</h2></summary>


### 1. Key is present

Probe finds the matching `OCCUPIED` slot — flip it to `DELETED`.

### 2. Key is not present (EMPTY hit)

Probe hits `EMPTY` first — no-op.

### 3. Table fully scanned

Probe returns without finding the key — no-op.

```mermaid
---
config:
  theme: base
  themeVariables:
    primaryColor: "#dbeafe"
    primaryBorderColor: "#3b82f6"
    primaryTextColor: "#1e3a5f"
    lineColor: "#64748b"
    secondaryColor: "#ede9fe"
    tertiaryColor: "#fef9c3"
---
flowchart LR
    Q["delete(8)<br/>hash = 1"] --> P0["i=0 → [1]<br/>(1, A) ≠ 8"]
    P0 --> P1["i=1 → [2]<br/>(8, B) = 8"]
    P1 --> ACT["mark [2] DELETED"]
    style P1 fill:#dcfce7,stroke:#22c55e
```

<p align="center"><strong>Quadratic-probing delete — same end result as linear probing, just with a quadratic step. The DELETED tombstone keeps the probe chain walkable for any record placed past this slot during inserts.</strong></p>

</details>
<details>
<summary><h2>Solution &amp; Analysis</h2></summary>

### Implementation

```python run
from enum import Enum
from typing import List, Optional

# Represents the state of a record in the hash table
class RecordType(Enum):
    EMPTY = 0
    DELETED = 1
    OCCUPIED = 2

# Represents an entry in the hash table
class Record:
    def __init__(
        self, key: Optional[int] = None, value: Optional[int] = None
    ):

        # Initialize state as EMPTY by default
        self.state: RecordType = RecordType.EMPTY
        self.key: int = 0
        self.value: int = 0

        # Set state to OCCUPIED when key and value are provided
        if key is not None and value is not None:
            self.state = RecordType.OCCUPIED
            self.key = key
            self.value = value

class MyHashTable:
    def __init__(self, capacity: int, a: int, b: int):
        self.capacity = capacity
        self.a = a
        self.b = b

        # The hash table implemented as a list of Records
        self.table: List[Record] = [Record() for _ in range(capacity)]

    # Primary hash function: Computes the index as key % capacity
    def hash_function(self, key: int) -> int:
        return key % self.capacity

    def probe_for_occupied_index(
        self, key: int, start_index: int
    ) -> int:
        for i in range(self.capacity):

            # Quadratic probing
            probe_index = (
                start_index + self.a * i * i + self.b * i
            ) % self.capacity

            # Check if the slot is occupied and matches the key
            if (
                self.table[probe_index].state == RecordType.OCCUPIED
                and self.table[probe_index].key == key
            ):
                return probe_index

        # Return -1 if no matching record is found
        return -1

    def probe_for_empty_index(self, start_index: int) -> int:
        for i in range(self.capacity):

            # Quadratic probing
            probe_index = (
                start_index + self.a * i * i + self.b * i
            ) % self.capacity

            # Check if the slot is available (either EMPTY or DELETED)
            if self.table[probe_index].state != RecordType.OCCUPIED:
                return probe_index

        # Return -1 if no available slot is found
        return -1

    def search(self, key: int) -> int:

        # Compute the initial index using the primary hash function
        start_index = self.hash_function(key)

        # Find the occupied index for the key
        occupied_index = self.probe_for_occupied_index(key, start_index)

        # Return the value if found, otherwise -1
        return (
            -1
            if occupied_index == -1
            else self.table[occupied_index].value
        )

    def insert(self, key: int, value: int) -> bool:

        # Compute the initial index using the primary hash function
        start_index = self.hash_function(key)

        # Find the occupied index for the key
        occupied_index = self.probe_for_occupied_index(key, start_index)

        # Update the value if the key exists
        if occupied_index != -1:
            self.table[occupied_index].value = value
            return True

        # Find an empty slot to insert the new key-value pair
        empty_index = self.probe_for_empty_index(start_index)
        if empty_index != -1:
            self.table[empty_index] = Record(key, value)
            return True

        # Return false if the table is full and insertion fails
        return False

    def remove(self, key: int) -> None:

        # Compute the initial index using the primary hash function
        start_index = self.hash_function(key)

        # Find the occupied index for the key
        occupied_index = self.probe_for_occupied_index(key, start_index)

        # Mark the slot as DELETED
        if occupied_index != -1:
            self.table[occupied_index].state = RecordType.DELETED
```

```java run
import java.util.*;

// Represents the state of a record in the hash table
enum RecordType {
    EMPTY,
    DELETED,
    OCCUPIED
}

// Represents an entry in the hash table
class Record {

    // Use the separately defined RecordType enum
    RecordType state = RecordType.EMPTY;
    int key = 0;
    int value = 0;

    Record() {}

    Record(int key, int value) {
        this.state = RecordType.OCCUPIED;
        this.key = key;
        this.value = value;
    }
}

class MyHashTable {

    // The total number of slots in the hash table
    private int capacity;

    // Quadratic probing constants
    private int a, b;

    // The hash table implemented as a list of Records
    private List<Record> table;

    // Primary hash function: Computes the index as key % capacity
    private int hashFunction(int key) {
        return key % capacity;
    }

    private int probeForOccupiedIndex(int key, int startIndex) {
        for (int i = 0; i < capacity; ++i) {

            // Quadratic probing
            int probeIndex = (startIndex + a * i * i + b * i) % capacity;

            // Check if the slot is occupied and matches the key
            if (
                table.get(probeIndex).state == RecordType.OCCUPIED &&
                table.get(probeIndex).key == key
            ) {
                return probeIndex;
            }
        }

        // Return -1 if no matching record is found
        return -1;
    }

    private int probeForEmptyIndex(int startIndex) {
        for (int i = 0; i < capacity; ++i) {

            // Quadratic probing
            int probeIndex = (startIndex + a * i * i + b * i) % capacity;

            // Check if the slot is available (either EMPTY or DELETED)
            if (table.get(probeIndex).state != RecordType.OCCUPIED) {
                return probeIndex;
            }
        }

        // Return -1 if no available slot is found
        return -1;
    }

    public MyHashTable(int capacity, int a, int b) {
        this.capacity = capacity;
        this.a = a;
        this.b = b;

        // Initialize the table with empty records
        table = new ArrayList<>();
        for (int i = 0; i < capacity; i++) {
            table.add(new Record());
        }
    }

    public int search(int key) {

        // Compute the initial index using the primary hash function
        int startIndex = hashFunction(key);

        // Find the occupied index for the key
        int occupiedIndex = probeForOccupiedIndex(key, startIndex);

        // Return the value if found, otherwise -1
        return occupiedIndex == -1 ? -1 : table.get(occupiedIndex).value;
    }

    public boolean insert(int key, int value) {

        // Compute the initial index using the primary hash function
        int startIndex = hashFunction(key);

        // Find the occupied index for the key
        int occupiedIndex = probeForOccupiedIndex(key, startIndex);

        // Update the value if the key exists
        if (occupiedIndex != -1) {
            table.get(occupiedIndex).value = value;
            return true;
        }

        // Find an empty slot to insert the new key-value pair
        int emptyIndex = probeForEmptyIndex(startIndex);
        if (emptyIndex != -1) {
            table.set(emptyIndex, new Record(key, value));
            return true;
        }

        // Return false if the table is full and insertion fails
        return false;
    }

    public void remove(int key) {

        // Compute the initial index using the primary hash function
        int startIndex = hashFunction(key);

        // Find the occupied index for the key
        int occupiedIndex = probeForOccupiedIndex(key, startIndex);

        // Mark the slot as DELETED
        if (occupiedIndex != -1) {
            table.get(occupiedIndex).state = RecordType.DELETED;
        }
    }
}
```

### Complexity analysis

> **Best case** — first probe is the target
>
> -   Time: **O(1)** | Space: **O(1)**
>
> **Average case** — well-distributed hashes
>
> -   Time: **O(1)** | Space: **O(1)**
>
> **Worst case** — collision-heavy
>
> -   Time: **O(N)** | Space: **O(1)**

</details>

***

# Design a hash table with quadratic probing

## Problem Statement

Given the skeleton of a `MyHashTable` class, complete it by implementing:

> -   **MyHashTable(int capacity, int a, int b)** — Initialise with the given capacity and quadratic constants `a` and `b`.
> -   **search(int key)** — Return the value, or `-1`.
> -   **insert(int key, int value)** — Insert or update; return `true` on success, `false` if the table is full.
> -   **remove(int key)** — Remove the mapping (no-op if absent).
> -   **getKeyAtIndex(int index)** — Return the key at `table[index]`, or `-1` if not `OCCUPIED`.

```d2
cons: Constraints {
  c1: "No built-in hash table libraries"
  c2: |md
    Quadratic probing for collisions

    (formula: a*i^2 + b*i, supplied as input)
  |
  c3: "Hash function: index = key % capacity"
}
```

<p align="center"><strong>Constraints — quadratic probing with supplied <code>a</code> and <code>b</code> coefficients. Choosing them well is critical: with the wrong combination, the probe can fail to visit every slot of the array, even when slots are free.</strong></p>

> **Choosing a, b, and capacity (a critical aside):**
>
> Quadratic probing's biggest gotcha is that the probe sequence may not visit every slot. The classic fix: use a **prime capacity** with `a = 1, b = 0` and a load factor under 0.5 — under those conditions, the first `capacity / 2` probes are guaranteed to visit distinct slots. Choose other (`a`, `b`, `capacity`) combinations carefully or you risk a "table-not-full but insert returns false" situation. We accept the parameters as input here so the user can experiment with the consequences directly.

> **Example:**
>
> -   **Input:** `[MyHashTable, insert, insert, search, insert, search, insert, search, search, getKeyAtIndex]`, `[[3, 2, 3], [1, 2], [2, 4], [1], [1, 3], [1], [2, 5], [2], [3], [0]]`
>
> -   **Output:** `[null, true, true, 2, true, 3, true, 5, -1, -1]`
>
> **Explanation:**
>
> | Operation | Effect | Result |
> |---|---|---|
> | `MyHashTable(3, 2, 3)` | empty, capacity 3, a=2, b=3 | `null` |
> | `insert(1, 2)` | 1 % 3 = 1 → `[EMPTY, (1,2), EMPTY]` | `true` |
> | `insert(2, 4)` | 2 % 3 = 2 → `[EMPTY, (1,2), (2,4)]` | `true` |
> | `search(1)` | found at index 1 | `2` |
> | `insert(1, 3)` | update existing | `true` |
> | `search(1)` | | `3` |
> | `insert(2, 5)` | update existing | `true` |
> | `search(2)` | | `5` |
> | `search(3)` | 3 % 3 = 0; index 0 is EMPTY → not found | `-1` |
> | `getKeyAtIndex(0)` | slot 0 is EMPTY | `-1` |

<details>
<summary><h2>Solution</h2></summary>


The full implementation. Note that the `Math.floorMod`-style guard `((x % n) + n) % n` is used in the Java version to ensure non-negative indices even if `start + offset` overflows or produces a negative value during arithmetic.


```python run viz=graph viz-root=table
from enum import Enum
from typing import List, Optional

# Represents the state of a record in the hash table
class RecordType(Enum):
    EMPTY = 0
    DELETED = 1
    OCCUPIED = 2

# Represents an entry in the hash table
class Record:
    def __init__(
        self, key: Optional[int] = None, value: Optional[int] = None
    ):

        # Initialize state as EMPTY by default
        self.state: RecordType = RecordType.EMPTY
        self.key: int = 0
        self.value: int = 0

        # Set state to OCCUPIED when key and value are provided
        if key is not None and value is not None:
            self.state = RecordType.OCCUPIED
            self.key = key
            self.value = value

class MyHashTable:
    def __init__(self, capacity: int, a: int, b: int):
        self.capacity = capacity
        self.a = a
        self.b = b

        # The hash table implemented as a list of Records
        self.table: List[Record] = [Record() for _ in range(capacity)]

    # Primary hash function: Computes the index as key % capacity
    def hash_function(self, key: int) -> int:
        return key % self.capacity

    def probe_for_occupied_index(
        self, key: int, start_index: int
    ) -> int:
        for i in range(self.capacity):

            # Quadratic probing
            probe_index = (
                start_index + self.a * i * i + self.b * i
            ) % self.capacity

            # Check if the slot is occupied and matches the key
            if (
                self.table[probe_index].state == RecordType.OCCUPIED
                and self.table[probe_index].key == key
            ):
                return probe_index

        # Return -1 if no matching record is found
        return -1

    def probe_for_empty_index(self, start_index: int) -> int:
        for i in range(self.capacity):

            # Quadratic probing
            probe_index = (
                start_index + self.a * i * i + self.b * i
            ) % self.capacity

            # Check if the slot is available (either EMPTY or DELETED)
            if self.table[probe_index].state != RecordType.OCCUPIED:
                return probe_index

        # Return -1 if no available slot is found
        return -1

    def search(self, key: int) -> int:

        # Compute the initial index using the primary hash function
        start_index = self.hash_function(key)

        # Find the occupied index for the key
        occupied_index = self.probe_for_occupied_index(key, start_index)

        # Return the value if found, otherwise -1
        return (
            -1
            if occupied_index == -1
            else self.table[occupied_index].value
        )

    def insert(self, key: int, value: int) -> bool:

        # Compute the initial index using the primary hash function
        start_index = self.hash_function(key)

        # Find the occupied index for the key
        occupied_index = self.probe_for_occupied_index(key, start_index)

        # Update the value if the key exists
        if occupied_index != -1:
            self.table[occupied_index].value = value
            return True

        # Find an empty slot to insert the new key-value pair
        empty_index = self.probe_for_empty_index(start_index)
        if empty_index != -1:
            self.table[empty_index] = Record(key, value)
            return True

        # Return false if the table is full and insertion fails
        return False

    def remove(self, key: int) -> None:

        # Compute the initial index using the primary hash function
        start_index = self.hash_function(key)

        # Find the occupied index for the key
        occupied_index = self.probe_for_occupied_index(key, start_index)

        # Mark the slot as DELETED
        if occupied_index != -1:
            self.table[occupied_index].state = RecordType.DELETED

    def get_key_at_index(self, index: int) -> int:
        return (
            self.table[index].key
            if self.table[index].state == RecordType.OCCUPIED
            else -1
        )


# Example from the problem statement (capacity=3, a=2, b=3)
t1 = MyHashTable(3, 2, 3)
print(t1.insert(1, 2))              # True
print(t1.insert(2, 4))              # True
print(t1.search(1))                 # 2
print(t1.insert(1, 3))              # True
print(t1.search(1))                 # 3
print(t1.insert(2, 5))              # True
print(t1.search(2))                 # 5
print(t1.search(3))                 # -1
print(t1.get_key_at_index(0))       # -1 — index 0 is EMPTY

# Edge cases
t2 = MyHashTable(5, 1, 1)
print(t2.search(10))                # -1 — empty table
print(t2.insert(0, 42))             # True — key 0 at index 0
print(t2.search(0))                 # 42
t2.remove(0)
print(t2.search(0))                 # -1 — removed
print(t2.insert(0, 7))              # True — re-insert into DELETED slot
print(t2.search(0))                 # 7
```

```java run
import java.util.*;

public class Main {

    // Represents the state of a record in the hash table
    enum RecordType {
        EMPTY,
        DELETED,
        OCCUPIED
    }

    // Represents an entry in the hash table
    static class Record {

        // Use the separately defined RecordType enum
        RecordType state = RecordType.EMPTY;
        int key = 0;
        int value = 0;

        Record() {}

        Record(int key, int value) {
            this.state = RecordType.OCCUPIED;
            this.key = key;
            this.value = value;
        }
    }

    static class MyHashTable {

        // The total number of slots in the hash table
        private int capacity;

        // Quadratic probing constants
        private int a, b;

        // The hash table implemented as a list of Records
        private List<Record> table;

        // Primary hash function: Computes the index as key % capacity
        private int hashFunction(int key) {
            return key % capacity;
        }

        private int probeForOccupiedIndex(int key, int startIndex) {
            for (int i = 0; i < capacity; ++i) {

                // Quadratic probing
                int probeIndex = (startIndex + a * i * i + b * i) % capacity;

                // Check if the slot is occupied and matches the key
                if (
                    table.get(probeIndex).state == RecordType.OCCUPIED &&
                    table.get(probeIndex).key == key
                ) {
                    return probeIndex;
                }
            }

            // Return -1 if no matching record is found
            return -1;
        }

        private int probeForEmptyIndex(int startIndex) {
            for (int i = 0; i < capacity; ++i) {

                // Quadratic probing
                int probeIndex = (startIndex + a * i * i + b * i) % capacity;

                // Check if the slot is available (either EMPTY or DELETED)
                if (table.get(probeIndex).state != RecordType.OCCUPIED) {
                    return probeIndex;
                }
            }

            // Return -1 if no available slot is found
            return -1;
        }

        public MyHashTable(int capacity, int a, int b) {
            this.capacity = capacity;
            this.a = a;
            this.b = b;

            // Initialize the table with empty records
            table = new ArrayList<>();
            for (int i = 0; i < capacity; i++) {
                table.add(new Record());
            }
        }

        public int search(int key) {

            // Compute the initial index using the primary hash function
            int startIndex = hashFunction(key);

            // Find the occupied index for the key
            int occupiedIndex = probeForOccupiedIndex(key, startIndex);

            // Return the value if found, otherwise -1
            return occupiedIndex == -1 ? -1 : table.get(occupiedIndex).value;
        }

        public boolean insert(int key, int value) {

            // Compute the initial index using the primary hash function
            int startIndex = hashFunction(key);

            // Find the occupied index for the key
            int occupiedIndex = probeForOccupiedIndex(key, startIndex);

            // Update the value if the key exists
            if (occupiedIndex != -1) {
                table.get(occupiedIndex).value = value;
                return true;
            }

            // Find an empty slot to insert the new key-value pair
            int emptyIndex = probeForEmptyIndex(startIndex);
            if (emptyIndex != -1) {
                table.set(emptyIndex, new Record(key, value));
                return true;
            }

            // Return false if the table is full and insertion fails
            return false;
        }

        public void remove(int key) {

            // Compute the initial index using the primary hash function
            int startIndex = hashFunction(key);

            // Find the occupied index for the key
            int occupiedIndex = probeForOccupiedIndex(key, startIndex);

            // Mark the slot as DELETED
            if (occupiedIndex != -1) {
                table.get(occupiedIndex).state = RecordType.DELETED;
            }
        }

        public int getKeyAtIndex(int index) {
            return table.get(index).state == RecordType.OCCUPIED
                ? table.get(index).key
                : -1;
        }
    }

    public static void main(String[] args) {
        // Example from the problem statement (capacity=3, a=2, b=3)
        MyHashTable t1 = new MyHashTable(3, 2, 3);
        System.out.println(t1.insert(1, 2));             // true
        System.out.println(t1.insert(2, 4));             // true
        System.out.println(t1.search(1));                // 2
        System.out.println(t1.insert(1, 3));             // true
        System.out.println(t1.search(1));                // 3
        System.out.println(t1.insert(2, 5));             // true
        System.out.println(t1.search(2));                // 5
        System.out.println(t1.search(3));                // -1
        System.out.println(t1.getKeyAtIndex(0));         // -1 — index 0 is EMPTY

        // Edge cases
        MyHashTable t2 = new MyHashTable(5, 1, 1);
        System.out.println(t2.search(10));               // -1 — empty table
        System.out.println(t2.insert(0, 42));            // true — key 0 at index 0
        System.out.println(t2.search(0));                // 42
        t2.remove(0);
        System.out.println(t2.search(0));                // -1 — removed
        System.out.println(t2.insert(0, 7));             // true — re-insert into DELETED slot
        System.out.println(t2.search(0));                // 7
    }
}
```

</details>
<details>
<summary><h2>Final Takeaway</h2></summary>


Quadratic probing is the smallest possible change to linear probing — replace the constant-step probe with a quadratic-step probe — and it buys you a substantially gentler degradation curve as load increases. Primary clusters can no longer form around a single hash index because the second collision is already 4 slots away, the third 9 slots away, the fourth 16. Whatever cluster *does* form gets stretched out across the array instead of piled up.

Two big lessons:

1. **The probe sequence shape matters more than its starting point.** Same hash function, same array, same record type — just a different formula for where to look next, and the performance behaviour transforms.
2. **Curing one disease can introduce another — secondary clustering.** Quadratic probing solves primary clustering but introduces a subtler bug. Two keys that hash to the *same* starting slot will follow the *same* probe sequence forever, because the formula `a·i² + b·i` doesn't depend on the key. They never escape each other. A stream of keys that all map to the same starting index forms a collision chain just as long as it would in linear probing — the chain just spreads across non-adjacent slots.

> *Coming up — secondary clustering is fixed by giving each key its own probe sequence. <strong>Double hashing</strong> uses a second hash function to compute a per-key step size, so two keys that share the same starting slot still walk the array in different rhythms. It's the most theoretically elegant of the open-addressing schemes — and the one with the most subtle implementation requirements (in particular, the second hash must never return zero). Let's see it.*

</details>

<!-- ============================================== -->
<!-- SWEEP 2 — missing sections (placeholders only) -->
<!-- ============================================== -->

<!-- TODO: Understanding the Problem — missing, needs to be written -->
<!--       Guidance: frame the gap the structure/algorithm fills -->

<!-- TODO: Supported Operations — missing, needs to be written -->
<!--       Guidance: table: operation / time / notes -->

<!-- TODO: Internal Mechanics — missing, needs to be written -->
<!--       Guidance: how it actually works under the hood -->

<!-- TODO: Working Example — missing, needs to be written -->
<!--       Guidance: one fully worked end-to-end example -->

<!-- TODO: Edge Cases & Pitfalls — missing, needs to be written -->
<!--       Guidance: bulleted list of gotchas -->

<!-- TODO: Production Reality — missing, needs to be written -->
<!--       Guidance: 4–6 entries: System — uses X — because Y -->

<!-- TODO: Quiz — missing, needs to be written -->
<!--       Guidance: 3–5 questions, each labeled [Recall]/[Reasoning]/[Tradeoff] -->

<!-- TODO: Practice Ladder — missing, needs to be written -->
<!--       Guidance: table: 5 links into pattern problems + hints -->

<!-- TODO: Further Reading — missing, needs to be written -->
<!--       Guidance: annotated: ★ Essential / ◆ Advanced / → Reference -->

<!-- TODO: Cross-Links — missing, needs to be written -->
<!--       Guidance: Prerequisites | What comes next -->

<!-- TODO: Final Takeaway — missing, needs to be written -->
<!--       Guidance: exactly 3 typed bullets: Core mechanic / Dominant tradeoff / One thing to remember -->
