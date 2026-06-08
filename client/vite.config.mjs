import { defineConfig } from "vite";
import tailwindcss from "@tailwindcss/vite";
import scalaJSPlugin from "@scala-js/vite-plugin-scalajs";
import path from "node:path";

export default defineConfig({
  plugins: [
    // Tailwind v4 runs through @tailwindcss/vite. PostCSS is no longer in the loop.
    tailwindcss(),
    scalaJSPlugin({
      // sbt root is the parent dir (this `client/` is a sub-project).
      cwd: "..",
      // sbt module name — must match `lazy val client` in build.sbt.
      projectID: "client",
    }),
  ],

  resolve: {
    alias: {
      // `@data` → `client/src/data`. Lets the Scala.js client `@JSImport` the portfolio data bridge
      // (portfolioData.ts + the JSON files) without brittle relative paths into the linker output dir.
      "@data": path.resolve(import.meta.dirname, "src/data"),
    },
  },

  server: {
    port: 5173,
    proxy: {
      // Forward the health check to the ZIO backend during development.
      "/api": "http://localhost:8080",
    },
  },

  build: {
    outDir: "dist",
    emptyOutDir: true,
  },
});
