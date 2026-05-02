# Debian-based image (glibc) so we can pull pre-built static binaries for
# Scala CLI / scala-cli / coursier without fighting Alpine's musl linkage.
# This is heavier than alpine but covers the full DSA chapter language set
# (C/C++/Go/Java/JS/TS/Python/SQL plus Scala/Rust/Kotlin) without quirks.
FROM node:22-bookworm-slim

ENV DEBIAN_FRONTEND=noninteractive

# Base toolchains: C/C++, Python, Go, Java, SQLite, plus tools used by the
# Scala/Kotlin install steps below (wget, unzip, curl).
RUN apt-get update && apt-get install -y --no-install-recommends \
    bash wget curl unzip ca-certificates \
    python3 \
    build-essential \
    golang \
    openjdk-17-jdk-headless \
    sqlite3 \
    && rm -rf /var/lib/apt/lists/*

# Rust via the official rustup installer (gives us a real `rustc`).
RUN curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh -s -- -y --default-toolchain 1.83.0 --profile minimal
ENV PATH="/root/.cargo/bin:${PATH}"

# TypeScript + ts-node
RUN npm install -g typescript tsx @types/node

# Kotlin compiler (no apt package available). Pinned for reproducibility.
ENV KOTLIN_VERSION=2.1.0
RUN wget -q https://github.com/JetBrains/kotlin/releases/download/v${KOTLIN_VERSION}/kotlin-compiler-${KOTLIN_VERSION}.zip -O /tmp/kotlin.zip \
    && unzip -q /tmp/kotlin.zip -d /opt \
    && rm /tmp/kotlin.zip
ENV PATH="/opt/kotlinc/bin:${PATH}"

# scala-cli — single-file Scala CLI, glibc build (works on bookworm).
ENV SCALA_CLI_VERSION=1.5.4
RUN ARCH="$(uname -m)" \
    && case "$ARCH" in \
       x86_64)  SCALA_ARCH=x86_64-pc-linux ;; \
       aarch64) SCALA_ARCH=aarch64-pc-linux ;; \
       *) echo "unsupported arch: $ARCH"; exit 1 ;; \
    esac \
    && wget -q "https://github.com/VirtusLab/scala-cli/releases/download/v${SCALA_CLI_VERSION}/scala-cli-${SCALA_ARCH}.gz" -O /tmp/scala-cli.gz \
    && gunzip /tmp/scala-cli.gz \
    && mv /tmp/scala-cli /usr/local/bin/scala-cli \
    && chmod +x /usr/local/bin/scala-cli

WORKDIR /app
COPY server.js .

EXPOSE 2358
CMD ["node", "server.js"]
