# `TracedCode` wraps user source on the client; the server stays unchanged

`Block.TracedCode` visualises the execution of a Python source as code + current-line cursor + locals panel.
The naive design — a new `/api/trace` endpoint backed by a server-side `sys.settrace` runner — was rejected
for v1: it would touch the OpenAPI contract, the `CodeExecutionBackend` trait, and require a new container or
extending Code Runner with a tracer mode. Instead, the **client** wraps the user source in a
`sys.settrace` harness, base64-encodes the original program, posts the wrapped program through the existing
`/api/run`, then parses the captured trace out of stdout (delimited by `__CFTRACE_BEGIN__` /
`__CFTRACE_END__` markers).

This means the *server learns nothing* about tracing — it executes the wrapped Python like any other Python
job. The "tracer" is a Python prelude + a stdout-marker convention. Adding Java tracing later will be
disruptive (JDI is not a stdout-friendly contract) and is deferred behind its own ADR. Step limits, repr
truncation, and the `_cf_`-prefix filter for harness-internal names all live in the same prelude so the
client decoder has nothing to compute beyond JSON parsing.

Trade: program output that contains the literal marker string would corrupt parsing. The risk is accepted —
the strings are intentionally distinctive and a chapter is unlikely to print them — and `extractTrace` uses
`lastIndexOf` on the begin marker so any legitimate output before the harness still survives.

The block is **collapsed by default**: the chapter shows the language label + a Trace button, nothing more,
until the reader opts in. Once a trace runs, a `Hide` toggle in the header re-collapses the body without
losing trace state. The reasoning: chapters often pair `python trace` with the equivalent prose explanation
in the surrounding markdown, and forcing every reader to scroll past a full code-and-locals panel by default
makes the page feel busy. Discoverability is preserved because the header — including the language label,
"🐍 Python · traced", and the Trace button — is always visible.
