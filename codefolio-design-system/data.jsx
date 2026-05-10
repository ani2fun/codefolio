// Portfolio data — distilled from /client/src/data and copy-pulled from the live site.
// Kept as plain JS objects on window so each Babel script can read it.

const PORTFOLIO_DATA = {
  selectedWork: [
    {
      company: "Europcar",
      meta: "Aug 2022 — Present · Paris",
      role: "Backend / Identity",
      what:
        "Customer authentication and account lifecycle on Kotlin + Spring Boot with Keycloak — for both consumer and B2B partner organisations.",
      tech: ["Kotlin", "Spring Boot", "Keycloak", "Kafka", "AWS"],
      anchor: "europcar"
    },
    {
      company: "Audi AG",
      meta: "2018 — 2021 · Remote (via Lunatech)",
      role: "Full-stack",
      what:
        "Set the early backend architecture for Audi's in-house labeling platform — ground-truth video data feeding autonomous-driving model training.",
      tech: ["Scala", "Akka-HTTP", "ScalaJS", "GKE", "GCP"],
      anchor: "audi"
    },
    {
      company: "Disneyland Paris",
      meta: "2017 — 2018 · On-site",
      role: "Backend",
      what:
        "Co-led the technical pilot for D4th — the new marketing platform integrating Tridion CMS with the public site (~10M annual visitors). Pilot funded a multi-million-euro replacement of the legacy app.",
      tech: ["Java", "Scala", "Play", "Tridion", "GitLab CI"],
      anchor: "disney"
    }
  ],
  experience: [
    {
      short: "Europcar",
      company: "Europcar International",
      location: "Paris, France",
      url: "https://www.europcar.com/",
      position: "Software Engineer — Backend",
      time: "Aug 2022 — Present",
      summary:
        "Working on customer authentication and account lifecycle for both consumer and partner channels, plus the wider reservation platform around it.",
      bullets: [
        "Built the customer auth + account lifecycle on Kotlin/Spring Boot with Keycloak / Red Hat SSO — account creation, email verification, password reset, login/logout, recovery, profile updates, account deletion, and email-based MFA.",
        "Built the token / API-security layer — JWT issuance and validation, refresh-token rotation, introspection, silent renewal, gateway integration.",
        "Set up the B2B identity surface — partner and corporate provisioning with role/permission management — running on a single Keycloak instance shared with B2C.",
        "Worked across the wider reservation platform — Kotlin microservices on Spring Boot, hexagonal/DDD, AWS through GitLab CI/CD.",
        "Rewrote slow PostgreSQL queries on the auth services and added composite indexes where lookups were doing full table scans."
      ],
      results: [
        "Modernised the team's Gradle build setup with multi-level CI caching — cut build times by ~50% (≈12 min → ≈5 min). Pattern adopted as the internal reference by 3+ adjacent JVM teams.",
        "Authored a C4-model architecture POC using Structurizr DSL; presented as a proposal for living architecture documentation."
      ],
      primary: ["Kotlin", "Spring Boot", "Keycloak", "PostgreSQL"],
      secondary: ["Java", "OAuth2 / OIDC", "JWT", "Kafka", "AWS", "GitLab CI/CD", "Gradle", "Structurizr", "Hexagonal / DDD"]
    },
    {
      short: "Dassault",
      company: "Dassault Systèmes",
      location: "Paris, France",
      url: "https://www.3ds.com/",
      position: "Software Engineer P.II",
      time: "Mar 2022 — Jul 2022",
      summary:
        "Short engagement building a data-quality tool that scanned global online sales and product-referential systems for missing fields, broken references, and inconsistent values across millions of records.",
      bullets: [
        "Started as a Python + SQL prototype on the internal stack, then rewrote it as a Java/Spring Boot service so it could plug into the regular release pipeline.",
        "Worked with business analysts to turn vague data-quality rules into concrete checks the release process could run on its own."
      ],
      results: [],
      primary: ["Java", "Spring Boot"],
      secondary: ["Python", "SQL", "Oracle"]
    },
    {
      short: "Audi AG",
      company: "Audi AG, Ingolstadt — via Lunatech",
      location: "Remote · Paris",
      url: "https://www.audi.com/",
      position: "Software Consultant — Full-stack",
      time: "Aug 2018 — Dec 2021",
      summary:
        "Built an in-house labeling platform that produced ground-truth video data for Audi's autonomous-driving model training. Worked remotely on a fast-growing team.",
      bullets: [
        "Set the backend architecture early on — applied Play Framework's architectural patterns to the Akka-HTTP codebase to give it the structure a fast-growing team would need.",
        "Wrote the React + ScalaJS frontends used by labelers, wired directly into the labeling backend.",
        "Set up role-based access with Forgerock IAM across the labeling tools and admin surfaces.",
        "Packaged services in Docker, ran them on GKE, and integrated with Cloud TPU and Cloud AI Platform so model predictions could pre-label data and speed up annotation."
      ],
      results: [
        "That early architectural call paid off as the team grew — multiple developers came onto the project later and could ship features quickly without fighting the codebase or relitigating structure."
      ],
      primary: ["Scala", "Akka-HTTP", "GKE"],
      secondary: ["ScalaJS", "Play Framework", "React", "Kafka", "Elasticsearch", "Forgerock IAM", "Docker", "GCP", "Argo Workflows"]
    },
    {
      short: "Disney",
      company: "Disneyland Paris — via Lunatech",
      location: "On-site · Paris",
      url: "https://www.disneylandparis.com/",
      position: "Software Consultant — Backend",
      time: "Jan 2017 — Aug 2018",
      summary:
        "Co-led the technical pilot for D4th — the new marketing platform integrating Tridion CMS with the Disneyland Paris public site (~10M annual visitors).",
      bullets: [
        "The pilot landed well enough that Disney funded a multi-million-euro programme on the back of it to fully replace the legacy marketing application.",
        "Wrote supporting microservices for browser push notifications and the SOAP integration with the older internal systems."
      ],
      results: [
        "Took on GitLab CI/CD setup and production deployment myself, even though both were outside my original scope — figured out the existing infrastructure, ran initial deployments, and handed it off cleanly when a more experienced infra engineer joined."
      ],
      primary: ["Java", "Scala", "Play"],
      secondary: ["Tridion CMS", "SOAP", "GitLab CI/CD", "Microservices"]
    },
    {
      short: "UPS",
      company: "UPS France — via Lunatech",
      location: "Remote",
      url: "https://www.ups.com/",
      position: "Software Consultant — Full-stack",
      time: "Aug 2016 — Jan 2017",
      summary:
        "Worked remotely on a B2B shipment-lifecycle platform — new features, maintenance, and day-to-day operations.",
      bullets: [
        "Added a Quartz-based scheduler that pushed automated reports to operations so delivery issues could be caught earlier.",
        "Deployed to AWS EC2 and handled day-to-day production: log analysis and incident triage."
      ],
      results: [],
      primary: ["Java", "AWS"],
      secondary: ["Quartz", "EC2", "Linux"]
    },
    {
      short: "Nokia",
      company: "Nokia Bell Labs (Alcatel-Lucent)",
      location: "Paris, France",
      url: "https://www.bell-labs.com/",
      position: "Software Developer Intern",
      time: "Jul 2014 — Dec 2014",
      summary:
        "Internship in the research division working on video-quality estimation and streaming protocols.",
      bullets: [
        "Built a video-quality estimation tool in Java implementing the ITU-T P.1201.1 model.",
        "Analysed streaming traffic with Wireshark and tcpdump as part of research into QUIC, HTTP/2, and H.264."
      ],
      results: [],
      primary: ["Java"],
      secondary: ["ITU-T P.1201.1", "Wireshark", "tcpdump", "QUIC", "HTTP/2", "H.264"]
    }
  ],
  projects: [
    {
      name: "Self-hosted homelab on K3s",
      featured: true,
      category: "Infra",
      tags: ["Kubernetes", "K3s", "Linux", "Networking", "Observability"],
      githubUrl: "https://github.com/ani2fun",
      projectUrl: "https://kakde.eu",
      description:
        "Four-node K3s cluster on commodity hardware sitting at home. Hands-on with the full delivery path — packaging, deployment, networking, observability — for the services I actually use, including this site.",
      art: "diagram"
    },
    {
      name: "Sonatype Maven Central Publisher",
      category: "OSS",
      tags: ["Kotlin", "Gradle", "Maven Central"],
      githubUrl: "https://github.com/ani2fun/sonatype-maven-central-publisher.git",
      projectUrl: "https://plugins.gradle.org/plugin/eu.kakde.gradle.sonatype-maven-central-publisher",
      description:
        "Open-source Gradle plugin on the official Plugin Portal. Publishes JVM artefacts to Maven Central through Sonatype's Central Portal API.",
      art: "stripe-a"
    },
    {
      name: "Food Ordering System (DDD + Hexagonal)",
      category: "Backend",
      tags: ["Kotlin", "Spring Boot", "Kafka", "PostgreSQL", "DDD"],
      githubUrl: "https://github.com/ani2fun/food-ordering-system.git",
      projectUrl: "https://github.com/ani2fun/food-ordering-system",
      description:
        "End-to-end e-commerce prototype written to practise hexagonal architecture and domain-driven design — Kotlin services on Spring Boot, Kafka events, PostgreSQL storage, Next.js frontend.",
      photo: "assets/food-ordering.webp",
      art: "photo"
    },
    {
      name: "Technical notebook (mdBook)",
      category: "OSS",
      tags: ["mdBook", "Rust", "Markdown"],
      githubUrl: "https://github.com/ani2fun/note-book.git",
      projectUrl: "https://notebook.kakde.eu",
      description:
        "An mdBook-based reference of in-depth technical guides — data structures, algorithms, system design, infra setup notes. Self-hosted on the homelab cluster.",
      art: "stripe-b"
    },
    {
      name: "Ray-tracer renderer (weekend project)",
      category: "Backend",
      tags: ["Scala", "Graphics"],
      githubUrl: "https://github.com/ani2fun",
      description:
        "Building a small CPU ray tracer in Scala 3 to chew through the Peter Shirley books — currently on volume two.",
      art: "stripe-c"
    },
    {
      name: "Gradle plugin demo apps",
      category: "OSS",
      tags: ["Gradle", "Kotlin", "Java"],
      githubUrl: "https://github.com/ani2fun/plugin-demo",
      projectUrl: "https://github.com/ani2fun/plugin-demo",
      description:
        "Reference projects that exercise the Sonatype publisher plugin from a fresh Gradle build — the canonical 'does it actually work' integration suite.",
      art: "stripe-a"
    }
  ],
  cortex: [
    {
      chap: "Chapter 03",
      title: "Kafka rebalancing internals",
      excerpt: "A close read of the cooperative-sticky assignor — what actually happens when a consumer joins or leaves, and why most production incidents are misconfigured rebalances.",
      meta: ["12 min read", "Apr 2026"]
    },
    {
      chap: "Chapter 02",
      title: "Hexagonal in practice",
      excerpt: "The diagram everyone draws is wrong about adapters. Notes from migrating two real services from a layered architecture, and what actually paid off.",
      meta: ["18 min read", "Mar 2026"]
    },
    {
      chap: "Chapter 01",
      title: "Postgres index choices that matter",
      excerpt: "B-tree, hash, GIN, GiST — when each one is the right answer, and the back-of-envelope reasoning I run before writing the migration.",
      meta: ["9 min read", "Feb 2026"]
    }
  ]
};

window.PORTFOLIO_DATA = PORTFOLIO_DATA;
