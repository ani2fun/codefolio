"use strict";
const http = require("http");
const { spawn } = require("child_process");
const { writeFileSync, mkdirSync, rmSync } = require("fs");
const { join } = require("path");
const crypto = require("crypto");

// Maps Judge0 language IDs to local execution config.
const RUNTIMES = {
  71: { ext: "script.py",  run:     ["python3", "script.py"] },
  63: { ext: "script.js",  run:     ["node",    "script.js"] },
  74: { ext: "script.ts",  run:     ["tsx", "script.ts"] },
  50: { ext: "script.c",   compile: ["gcc",  "-O2", "script.c",   "-o", "prog", "-lm"], run: ["./prog"] },
  54: { ext: "script.cpp", compile: ["g++",  "-O2", "script.cpp", "-o", "prog"],         run: ["./prog"] },
  60: { ext: "script.go",  run:     ["go",   "run", "script.go"] },
  62: { ext: "Main.java",  compile: ["javac", "Main.java"],  run: ["java", "-cp", ".", "Main"] },
  73: { ext: "main.rs",    compile: ["rustc", "-O", "main.rs", "-o", "prog"],          run: ["./prog"], compileTimeoutMs: 30_000 },
  78: { ext: "Main.kt",    compile: ["sh", "-c", "kotlinc -include-runtime -d Main.jar Main.kt 2>&1"], run: ["java", "-jar", "Main.jar"], compileTimeoutMs: 60_000 },
  81: { ext: "Main.scala", run:     ["scala-cli", "run", "Main.scala", "--quiet", "--server=false"], runTimeoutMs: 90_000 },
  82: { ext: "script.sql", run:     ["sh", "-c", "sqlite3 :memory: < script.sql"] },
};

// Judge0 status IDs
const STATUS = {
  ACCEPTED:       { id: 3,  description: "Accepted" },
  COMPILE_ERROR:  { id: 6,  description: "Compilation Error" },
  RUNTIME_ERROR:  { id: 11, description: "Runtime Error (NZEC)" },
  TLE:            { id: 5,  description: "Time Limit Exceeded" },
  INTERNAL_ERROR: { id: 13, description: "Internal Error" },
};

function b64dec(s) { return s ? Buffer.from(s, "base64").toString("utf8") : ""; }
function b64enc(s) { return s ? Buffer.from(s, "utf8").toString("base64") : null; }

function runProcess(cmd, args, cwd, stdin, timeoutMs) {
  return new Promise((resolve) => {
    const child = spawn(cmd, args, { cwd, stdio: ["pipe", "pipe", "pipe"] });
    let out = "", err = "";
    let timedOut = false;

    const timer = setTimeout(() => {
      timedOut = true;
      child.kill("SIGKILL");
    }, timeoutMs);

    child.stdout.on("data", (d) => { if (out.length < 512 * 1024) out += d; });
    child.stderr.on("data", (d) => { if (err.length < 512 * 1024) err += d; });
    child.on("close", (code) => {
      clearTimeout(timer);
      resolve({ out, err, code: code ?? -1, timedOut });
    });
    child.on("error", (e) => {
      clearTimeout(timer);
      resolve({ out: "", err: e.message, code: -1, timedOut: false });
    });

    if (stdin) child.stdin.write(stdin);
    child.stdin.end();
  });
}

const server = http.createServer((req, res) => {
  // Health check
  if (req.method === "GET") {
    res.writeHead(200, { "Content-Type": "application/json" });
    res.end(JSON.stringify({ name: "code-runner", ready: true }));
    return;
  }

  if (req.method !== "POST" || !req.url.startsWith("/submissions")) {
    res.writeHead(404);
    res.end("Not found");
    return;
  }

  const url = new URL(req.url, "http://localhost");
  const isBase64 = url.searchParams.get("base64_encoded") === "true";

  let body = "";
  req.on("data", (c) => (body += c));
  req.on("end", async () => {
    let data;
    try { data = JSON.parse(body); } catch {
      res.writeHead(400); res.end("Bad JSON"); return;
    }

    const lang = RUNTIMES[data.language_id];
    if (!lang) {
      res.writeHead(400, { "Content-Type": "application/json" });
      res.end(JSON.stringify({ error: `Unsupported language_id: ${data.language_id}` }));
      return;
    }

    const source = isBase64 ? b64dec(data.source_code) : (data.source_code ?? "");
    const stdin  = isBase64 ? b64dec(data.stdin)       : (data.stdin ?? "");
    const dir = join("/tmp", `run-${crypto.randomBytes(6).toString("hex")}`);
    mkdirSync(dir, { recursive: true });

    const t0 = Date.now();
    let status = STATUS.ACCEPTED;
    let stdout = "", stderr = "", compileOut = "";

    try {
      writeFileSync(join(dir, lang.ext), source, "utf8");

      if (lang.compile) {
        // Some compilers (kotlinc, rustc, scalac) need more headroom than gcc/javac.
        const compileTimeout = lang.compileTimeoutMs ?? 15_000;
        const r = await runProcess(lang.compile[0], lang.compile.slice(1), dir, "", compileTimeout);
        if (r.code !== 0) {
          status = STATUS.COMPILE_ERROR;
          compileOut = r.stderr || r.out;
        }
      }

      if (status === STATUS.ACCEPTED) {
        const runTimeout = lang.runTimeoutMs ?? 10_000;
        const r = await runProcess(lang.run[0], lang.run.slice(1), dir, stdin, runTimeout);
        stdout = r.out;
        stderr = r.err;
        if (r.timedOut) status = STATUS.TLE;
        else if (r.code !== 0) status = STATUS.RUNTIME_ERROR;
      }
    } catch (e) {
      stderr = e.message;
      status = STATUS.INTERNAL_ERROR;
    } finally {
      rmSync(dir, { recursive: true, force: true });
    }

    const elapsed = ((Date.now() - t0) / 1000).toFixed(3);
    res.writeHead(201, { "Content-Type": "application/json" });
    res.end(JSON.stringify({
      stdout:         b64enc(stdout),
      stderr:         b64enc(stderr),
      compile_output: compileOut ? b64enc(compileOut) : null,
      message:        null,
      status,
      time:           elapsed,
      memory:         null,
    }));
  });
});

server.listen(2358, () => console.log("code-runner ready on :2358"));
