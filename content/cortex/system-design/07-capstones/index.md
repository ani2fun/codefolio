# Part 7 — Capstones

> **Design ten famous systems end-to-end.** Each capstone ships all three diagram views (Mermaid hot-path sequence, D2 topology, full Structurizr workspace), a runnable prototype, and a "what would you do differently at 100×?" extension.

Each capstone follows this longer template:

> Functional reqs → Non-functional reqs → Capacity estimation → API design → High-level design (with all three diagram types) → Data model → Two deep-dives → Trade-offs → How `<BigCompany>` actually does it → "At 100×" stretch → Link to a real OSS reference implementation.

## Capstones

37. [URL shortener](/cortex/system-design/capstones-url-shortener) — the canonical first design.
38. [News feed / timeline (Twitter, Instagram)](/cortex/system-design/capstones-news-feed) — fan-out-on-write vs fan-out-on-read.
39. [Chat system (WhatsApp, Slack)](/cortex/system-design/capstones-chat-system) — long-lived connections, presence, read-receipts.
40. [Video streaming (YouTube, Netflix)](/cortex/system-design/capstones-video-streaming) — encoding ladders, CDNs, ABR.
41. [Ride-sharing dispatch (Uber, Lyft)](/cortex/system-design/capstones-ride-sharing-dispatch) — geospatial indexing, dispatch algorithms.
42. [Search autocomplete (Google)](/cortex/system-design/capstones-search-autocomplete) — tries, n-grams, prefix scoring.
43. [Distributed file storage (Dropbox, S3)](/cortex/system-design/capstones-distributed-file-storage) — chunking, dedup, durability.
44. [Payment system (Stripe-style)](/cortex/system-design/capstones-payment-system) — money is special.
45. [Online multiplayer game backend](/cortex/system-design/capstones-multiplayer-game-backend) — real-time, low-latency, lockstep vs server-authoritative.
46. [Recommendation system serving layer](/cortex/system-design/capstones-recommendation-serving) — separating training from inference.

> **Status:** capstone 37 (URL shortener) is the dry-run for the format and ships in **Wave 2**. Capstones 38–46 are **Wave 5**.
