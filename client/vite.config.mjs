import { defineConfig } from "vite";
import scalaJSPlugin from "@scala-js/vite-plugin-scalajs";
import tailwindcss from "@tailwindcss/vite";

export default defineConfig({
  plugins: [
    scalaJSPlugin({
      // sbt root is the parent dir (this `client/` is a sub-project).
      cwd: "..",
      // sbt module name — must match `lazy val client` in build.sbt.
      projectID: "client",
    }),
    tailwindcss(),
  ],

  server: {
    port: 5173,
    proxy: {
      // Forward API calls to the ZIO backend during development.
      "/api": "http://localhost:8080",
      "/docs": "http://localhost:8080",
    },
  },

  build: {
    outDir: "dist",
    emptyOutDir: true,
  },
});
