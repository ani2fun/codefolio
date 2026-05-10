window.CORTEX_DATA = {
  book: {
    title: "Designing data-intensive applications",
    author: "Long-form notes",
    chapters: [
      { id: "ch1",  num: 1, title: "Reliability, scalability, maintainability" },
      { id: "ch2",  num: 2, title: "Data models and query languages" },
      { id: "ch3",  num: 3, title: "Storage and retrieval" },
      { id: "ch4",  num: 4, title: "Encoding and evolution" },
      { id: "ch5",  num: 5, title: "Replication", active: true },
      { id: "ch6",  num: 6, title: "Partitioning" },
      { id: "ch7",  num: 7, title: "Transactions" },
      { id: "ch8",  num: 8, title: "Trouble with distributed systems" },
      { id: "ch9",  num: 9, title: "Consistency and consensus" }
    ]
  },
  chapter: {
    num: 5,
    title: "Replication",
    summary: "Three reasons to replicate, three ways to do it, and the trade-offs each one forces on you.",
    sections: [
      { id: "intro", level: 1, title: "Replication" },
      { id: "why", level: 2, title: "Why replicate?" },
      { id: "leader", level: 2, title: "Leader-based replication" },
      { id: "sync-async", level: 3, title: "Synchronous vs asynchronous followers" },
      { id: "lag", level: 3, title: "Replication lag and read-your-writes" },
      { id: "multi-leader", level: 2, title: "Multi-leader replication" },
      { id: "leaderless", level: 2, title: "Leaderless replication" },
      { id: "conflict", level: 3, title: "Detecting concurrent writes" },
      { id: "summary", level: 2, title: "Summary" }
    ]
  }
};
