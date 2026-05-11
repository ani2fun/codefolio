workspace "Book Tracker — same product, two architectures" {

    model {
        # ----------------------------------------------------------------
        # Two scenarios from Lesson 1: a personal app (10 readers) and the
        # same product when the book club has 50,000 readers worldwide.
        # We model BOTH in one workspace so the C4 views render side-by-side.
        # ----------------------------------------------------------------

        reader = person "Reader" "Tracks books they have read; rates and reviews them."

        # ===== Architecture A — personal scale (10 readers) =====
        small = softwareSystem "Book Tracker (personal)" "10 readers. Built in an evening." {
            smallWeb   = container "FastAPI app"        "Serves the UI and handles reads/writes."             "Python 3.11 / FastAPI"
            smallDb    = container "SQLite file"        "Single file on the same VM. Nightly backup to S3."   "SQLite"
            smallAlert = container "Disk-full alert"    "The one piece of operations the design needs."       "Cron + cloud monitor"

            smallWeb   -> smallDb "SQL queries (same VM, microseconds)"
            smallAlert -> smallDb "Watches disk usage"
        }

        reader -> small "Reads / writes book reviews via HTTPS"

        # ===== Architecture B — scaled (50k readers, real-time leaderboard) =====
        large = softwareSystem "Book Tracker (scaled)" "50,000 readers, real-time leaderboard, multi-region." {
            cdn          = container "CDN"                  "Serves static assets and cache-friendly API responses." "Cloudflare"
            lb           = container "Load balancer"        "Health-checks and steers traffic to app instances."     "L7 reverse proxy"
            apiFleet     = container "API fleet"            "Stateless. Horizontally scaled."                        "Python / FastAPI on Kubernetes"
            cache        = container "Read cache"           "Hot timeline / book metadata."                          "Redis Cluster"
            primaryDb    = container "Primary database"     "Source of truth for reviews, users, books."             "PostgreSQL with read replicas"
            queue        = container "Event queue"          "Decouples writes from leaderboard updates."             "Kafka"
            worker       = container "Leaderboard worker"   "Aggregates reads from the queue into the cache."        "Python consumer"
            search       = container "Search index"         "Title / author search over the corpus."                 "OpenSearch"
            metrics      = container "Observability"        "Logs, metrics, traces. Drives alerting and oncall."     "OpenTelemetry + Prometheus"

            cdn      -> lb         "Cache miss / dynamic API"
            lb       -> apiFleet   "Routes to a healthy app instance"
            apiFleet -> cache      "Reads cached timelines (~100 µs)"
            apiFleet -> primaryDb  "Reads / writes through ORM"
            apiFleet -> queue      "Publishes review-created events"
            apiFleet -> search     "Full-text queries"
            queue    -> worker     "Consumes review events"
            worker   -> cache      "Updates leaderboard slabs"
            worker   -> primaryDb  "Reads denormalised aggregates"
            metrics  -> apiFleet   "Scrapes app metrics"
            metrics  -> primaryDb  "Watches replication lag"
            metrics  -> queue      "Watches consumer lag"
        }

        reader -> large "HTTPS to nearest edge"
    }

    views {
        systemContext small "PersonalContext" "Personal-scale book tracker — context view." {
            include *
            autolayout lr
        }
        container small "PersonalContainers" "Personal-scale book tracker — container view." {
            include *
            autolayout lr
        }

        systemContext large "ScaledContext" "Scaled book tracker (50k users) — context view." {
            include *
            autolayout lr
        }
        container large "ScaledContainers" "Scaled book tracker — container view." {
            include *
            autolayout lr
        }

        styles {
            element "Person" {
                background "#dbeafe"
                color      "#1e3a5f"
                shape      Person
            }
            element "Software System" {
                background "#ede9fe"
                color      "#1e3a5f"
            }
            element "Container" {
                background "#fef9c3"
                color      "#1e3a5f"
            }
        }
    }
}
