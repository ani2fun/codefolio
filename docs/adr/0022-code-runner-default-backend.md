# Code Runner as the default execution backend (Slice 8)

`CodeRunPipeline.live` previously listed Piston first, making it the winner whenever both
backends were configured. As of Slice 8 the order is reversed: Code Runner wins, Piston
falls back. The self-hosted runner is preferable for traced runs on two concrete grounds:

1. **stdout cap.** Piston's public service caps each stream at ~64 KB. The JVM tracer
   harness (Slice 4+) emits marker-delimited JSON that can exceed 64 KB for a medium-sized
   trace (20–30 steps × locals × heap). Code Runner's `max_output` is 1 MB — matching
   `BackendLimits.codeRunner.maxStdoutBytes` in the shared module.

2. **JDK control.** The JVM tracer needs `javax.tools.ToolProvider.getSystemJavaCompiler()`
   to return non-null at runtime — i.e., a full JDK, not just a JRE. The self-hosted
   `runner/Dockerfile` installs `openjdk-17-jdk-headless` (JDK 17, which ships
   `jdk.compiler`). Piston's Java image is third-party and could strip the compiler from
   the runtime at any point.

`runner/server.js` already raised its internal stdout/stderr accumulation cap to 1 MB in
this slice (from 512 KB) to be consistent with `BackendLimits.codeRunner.maxStdoutBytes`.

## Operator procedure — switching backends

| Scenario | `CODE_RUNNER_URL` | `PISTON_URL` | Winner |
|---|---|---|---|
| Self-hosted only (recommended) | `http://code-runner:2358` | *(unset)* | Code Runner |
| Public-Piston only | *(unset)* | `https://emkc.org/api/v2/piston` | Piston |
| Both configured | set | set | Code Runner (new default) |
| Neither set | *(unset)* | *(unset)* | 503 `NotConfigured` |

**docker-compose full-stack** (`docker compose up`): `CODE_RUNNER_URL` defaults to
`http://code-runner:2358`; `PISTON_URL` defaults to empty. Code Runner is the only
configured backend — no change to override needed.

**`bin/dev` (JVM on host, containers in Docker)**: `CODE_RUNNER_URL` is exported as
`http://localhost:2358` (the port forwarded by the `code-runner` container). `PISTON_URL`
is unset unless the caller overrides it. Same result: Code Runner only.

**Production (Kubernetes)**: point `CODE_RUNNER_URL` at the cluster's Code Runner service
URL (`http://code-runner:2358` or whatever the internal DNS name is). Leave `PISTON_URL`
unset to eliminate Piston entirely, or set it as a fallback for languages the self-hosted
image doesn't support.

To **force Piston as sole backend** (e.g. if the Code Runner container is down and you
need a quick workaround): set `CODE_RUNNER_URL=` (empty string) and
`PISTON_URL=https://emkc.org/api/v2/piston`. `liveBackends` filters out empty strings,
so only Piston is in the list.

## Considered alternatives (and why rejected)

- **Keep Piston as default, make Code Runner opt-in via a flag.** Rejected — the JVM
  tracer (the primary motivator for this slice) breaks silently on Piston when trace JSON
  exceeds 64 KB. The correct default should be the one that doesn't silently truncate.
  Operators who genuinely want Piston can leave `CODE_RUNNER_URL` unset.

- **Expose an `/api/runner-info` endpoint that tells the client which backend is active.**
  Deferred. `BackendLimits` is hard-coded on the client for now (matching the `piston` cap
  when `CODE_RUNNER_URL` is absent, matching `codeRunner` when it's present). A proper
  endpoint would let the client adapt without a redeploy — worth adding when the harness
  needs it, not before.

- **Make backend selection per-language rather than per-priority-list.** Rejected — the
  priority-list model is already in place (`CodeRunPipeline.pickAndRun`) and handles the
  "both configured" case cleanly without adding a per-language routing table. The only
  case where per-language routing would help is if one backend supports a language the
  other doesn't — that situation is currently non-existent (Code Runner supports all
  Judge0 language IDs we ship; Piston has its own runtime map). Revisit if a language
  ships only on one backend.

## Consequences worth calling out

- **Test coverage unchanged.** `CodeRunPipelineSpec` already tests abstract priority via
  `CodeRunPipeline.from(List(first, second))` — the order argument — so no test edits
  are needed. The test `"prefers the first backend when both back the same language"`
  validates the mechanism correctly regardless of which is first in `live`.

- **`BackendLimits.piston` is now the fallback-backend entry.** The comment is updated
  accordingly. The client reads `BackendLimits.codeRunner` when Code Runner is active and
  `BackendLimits.piston` when Piston is the sole backend.

- **runner stdout cap bump is a runner-container rebuild.** The `runner/server.js` change
  (512 KB → 1 MB) takes effect on the next `docker compose up --build code-runner`. No
  Scala recompile needed — this is purely the Node.js HTTP server inside the container.
