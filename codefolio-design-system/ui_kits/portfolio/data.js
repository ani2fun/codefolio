// Subset of the live data — enough to make the UI kit feel real.
window.PORTFOLIO_DATA = {
  experiences: [
    {
      company: "Europcar International",
      location: "Paris, France",
      position: "Software Engineer — Backend",
      time: "August 2022 – Present",
      summary: "Customer authentication and account lifecycle for both consumer and partner channels, plus the wider reservation platform around it.",
      stack: ["Kotlin", "Spring Boot", "Kafka", "PostgreSQL", "AWS", "Hexagonal / DDD"],
      results: "Modernised the team's Gradle build setup with multi-level CI caching, cutting build times by ~50% (≈12 min → ≈5 min) — pattern adopted as the internal reference by 3+ adjacent JVM teams."
    },
    {
      company: "Dassault Systèmes",
      location: "Vélizy, France",
      position: "R&D Software Engineer",
      time: "January 2020 – July 2022",
      summary: "Backend services for the 3DEXPERIENCE collaboration platform — content sync, change tracking, audit pipelines.",
      stack: ["Java", "Spring", "Cassandra", "Elasticsearch", "Docker"],
      results: "Designed an event-sourced audit log adopted across two product lines, retiring three brittle CRUD services."
    },
    {
      company: "Audi AG",
      location: "Ingolstadt, Germany",
      position: "Software Engineer",
      time: "October 2017 – December 2019",
      summary: "Telemetry pipeline for prototype fleet vehicles. Real-time ingestion, schema evolution, on-call.",
      stack: ["Scala", "Akka", "Kafka", "Spark"],
      results: "Cut median ingestion latency from 12s to under 1s by reshaping the partitioning scheme."
    },
    {
      company: "Disney",
      location: "Paris, France",
      position: "Engineer (pilot)",
      time: "April 2016 – September 2017",
      summary: "Pilot programme building a multi-million-euro internal tooling platform.",
      stack: ["Java", "AngularJS", "MongoDB"],
      results: "The pilot landed well enough that Disney funded a multi-million-euro programme off the back of it."
    }
  ],
  projects: [
    {
      title: "Self-hosted homelab on K3s",
      description: "Four-node K3s cluster on commodity hardware sitting at home. Hands-on with the full delivery path — packaging, deployment, networking, observability.",
      image: "../../assets/macbook.webp",
      tags: ["Kubernetes", "K3s", "Linux", "Self-hosted"],
      links: { github: "#", external: "#" }
    },
    {
      title: "Codefolio (this site)",
      description: "Scala.js + React 19 SPA with a Markdown reader for long-form notes. Self-hosted on the K3s cluster above.",
      image: "../../assets/portfolio-webapp.webp",
      tags: ["Scala.js", "React", "Tailwind", "shadcn"],
      links: { github: "#", external: "#" }
    },
    {
      title: "Gradle multi-level CI cache plugin",
      description: "Internal Gradle plugin that layers remote, branch, and local build caches. Cut team CI time roughly in half.",
      image: "../../assets/gradle-plugin.webp",
      tags: ["Gradle", "Kotlin", "CI"],
      links: { github: "#", external: "#" }
    },
    {
      title: "Food-ordering reference system",
      description: "Hexagonal-architecture reference implementation of an order pipeline — DDD aggregates, sagas, outbox.",
      image: "../../assets/food-ordering-system.webp",
      tags: ["Java", "Spring Boot", "Hexagonal", "DDD"],
      links: { github: "#", external: "#" }
    }
  ],
  certifications: [
    { issuer: "Sorbonne University · DataScientest", name: "Diplôme Data Engineer (RNCP Niveau 7)", date: "April 2026", length: "9-month programme", highlight: true, badge: "Diploma" },
    { issuer: "Udemy · Baraa Khatib Salkini", name: "The Complete SQL Bootcamp — Zero to Hero", date: "December 2025", length: "29 hours" },
    { issuer: "Confluent", name: "Apache Kafka 101", date: "October 2024", length: "12 hours" },
    { issuer: "AWS", name: "Solutions Architect Associate", date: "March 2023", length: "Exam" }
  ],
  cortexChapters: [
    { book: "Designing data-intensive applications", chapters: 12, title: "Replication, partitioning, and consistency", desc: "Long-form notes on how distributed datastores trade off availability, latency and ordering.", tags: ["Kafka", "PostgreSQL", "CAP"] },
    { book: "Database internals", chapters: 14, title: "B-trees, LSM-trees and the storage layer", desc: "Why your index choice silently shapes your write amplification and read latency.", tags: ["Storage", "Indexing"] },
    { book: "The Linux programming interface", chapters: 64, title: "Processes, signals, and the file system", desc: "Going to first principles for the system calls every backend leans on.", tags: ["Linux", "Systems"] },
    { book: "Architecture patterns", chapters: 9, title: "Hexagonal architecture in practice", desc: "Ports, adapters, and what actually changes when you take dependency inversion seriously.", tags: ["DDD", "Hexagonal"] }
  ]
};
