const ChapterContent = ({ chapter }) => (
  <article className="cx-prose">
    <header className="cx-prose__head">
      <div className="cx-prose__num">Chapter {String(chapter.num).padStart(2, "0")}</div>
      <h1 id="intro" className="cx-h1">{chapter.title}</h1>
      <p className="cx-prose__lede">{chapter.summary}</p>
    </header>

    <h2 id="why" className="cx-h2">Why replicate?</h2>
    <p>Three reasons keep showing up. <strong>Latency:</strong> serve reads close to the user. <strong>Availability:</strong> survive a node failure. <strong>Throughput:</strong> spread the read load. Most real systems want all three at once, which is where the trade-offs start to bite.</p>
    <p>Whatever you replicate, the central question is what to do when nodes disagree — and the answer shapes everything else.</p>

    <h2 id="leader" className="cx-h2">Leader-based replication</h2>
    <p>One node is designated the leader. Writes go to it; the leader streams a change log to its followers. Reads can come from either, depending on your tolerance for stale data.</p>

    <h3 id="sync-async" className="cx-h3">Synchronous vs asynchronous followers</h3>
    <p>A synchronous follower acknowledges every write before the leader returns success. Durability ↑, write latency ↑, availability ↓. Nearly every production system runs <em>semi</em>-synchronous: one synchronous, the rest async.</p>

    <pre className="cx-code"><code>{`-- Read-your-writes from a Postgres replica
SELECT pg_last_wal_replay_lsn() AS replica_lsn;
-- Compare to the leader's LSN at write time;
-- if behind, route the read back to the leader.`}</code></pre>

    <h3 id="lag" className="cx-h3">Replication lag and read-your-writes</h3>
    <p>Async replication is the default for a reason — and so is the user reading their freshly-posted comment from a slightly stale replica and seeing nothing. Mitigations cluster into three patterns:</p>
    <ul>
      <li>Route the user's own reads to the leader for a window after their writes.</li>
      <li>Carry a logical timestamp with the request and pick a replica caught up to it.</li>
      <li>Defer the offending read to a job that can wait.</li>
    </ul>

    <aside className="cx-callout">
      <div className="cx-callout__title">Note</div>
      <p>"Eventual consistency" is the easy answer to a hard question. It's also a guarantee about the limit, not the journey — the path between writes and reads is what your users actually feel.</p>
    </aside>

    <h2 id="multi-leader" className="cx-h2">Multi-leader replication</h2>
    <p>Two leaders, often one per data centre. Writes are accepted at either, then asynchronously cross-replicated. The whole architecture exists so a regional outage doesn't take writes down — but you inherit conflict resolution as a permanent design problem.</p>

    <h2 id="leaderless" className="cx-h2">Leaderless replication</h2>
    <p>Dynamo-style: every replica accepts writes, every read fans out to a quorum. No single point of failure, no single source of truth. Reconciling concurrent writes is now <em>your</em> job.</p>

    <h3 id="conflict" className="cx-h3">Detecting concurrent writes</h3>
    <p>Version vectors are the textbook answer; last-writer-wins is the practical one most systems quietly use, with all the data loss that implies under skewed clocks.</p>

    <h2 id="summary" className="cx-h2">Summary</h2>
    <p>Pick the topology by the failure mode you can't tolerate, not by the throughput you wish for. Then design the application around the consistency the chosen topology can actually deliver — not the one you'd like.</p>
  </article>
);
window.ChapterContent = ChapterContent;
