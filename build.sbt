import org.scalajs.linker.interface.ModuleKind

ThisBuild / scalaVersion := "3.6.2"
ThisBuild / organization := "codefolio"
ThisBuild / version      := "0.1.0-SNAPSHOT"

ThisBuild / scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-unchecked",
  "-Wunused:all",
  "-Wvalue-discard",
  // Scala 3's E198 (unused local definition) sometimes mis-attributes a warning to a chained method's
  // closing paren. Silence locally; revisit when upstream improves the diagnostic.
  "-Wconf:msg=unused local definition:s"
)

// ---- Versions ------------------------------------------------------------

val zioV          = "2.1.14"
val zioHttpV      = "3.0.1"
val zioConfigV    = "4.0.3"
val scalajsReactV = "3.0.0"
val logbackV      = "1.5.12"
val zioLogbackV   = "2.4.0"

// ---- shared --------------------------------------------------------------
//
// `shared` is cross-compiled for the JVM (server) and JS (client). For the static portfolio it carries only
// `AppRoutes` (pure-Scala route + public-asset path segments), so it needs no library dependencies and no
// OpenAPI codegen.

lazy val shared = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("shared"))
  .settings(
    name := "codefolio-shared"
  )

lazy val sharedJVM = shared.jvm
lazy val sharedJS  = shared.js

// ---- server --------------------------------------------------------------
//
// ZIO + zio-http. The portfolio server serves the Vite bundle from `client/dist` plus a trivial
// `/api/health` for the Kubernetes probe — no databases, no auth, no tapir.

lazy val server = (project in file("server"))
  .enablePlugins(JavaAppPackaging)
  .dependsOn(sharedJVM)
  .settings(
    name                := "codefolio-server",
    Compile / mainClass := Some("codefolio.server.Main"),
    // Run the server with the build root as its working directory so the default `./client/dist` static
    // path resolves the same under `sbt server/run`, `sbt server/reStart`, and `bin/dev`.
    Compile / run / baseDirectory := (LocalRootProject / baseDirectory).value,
    reStart / baseDirectory       := (LocalRootProject / baseDirectory).value,
    libraryDependencies ++= Seq(
      "dev.zio"       %% "zio"                 % zioV,
      "dev.zio"       %% "zio-http"            % zioHttpV,
      "dev.zio"       %% "zio-config"          % zioConfigV,
      "dev.zio"       %% "zio-config-typesafe" % zioConfigV,
      "dev.zio"       %% "zio-config-magnolia" % zioConfigV,
      "dev.zio"       %% "zio-logging-slf4j2"  % zioLogbackV,
      "ch.qos.logback" % "logback-classic"     % logbackV,
      "dev.zio"       %% "zio-test"            % zioV % Test,
      "dev.zio"       %% "zio-test-sbt"        % zioV % Test
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )

// ---- client --------------------------------------------------------------
//
// ScalaJS module. The `client/` directory also hosts the Vite project (package.json, vite.config.mjs,
// index.html, main.js, tailwind.css). The `@scala-js/vite-plugin-scalajs` plugin imports the linker output.

lazy val client = (project in file("client"))
  .enablePlugins(ScalaJSPlugin)
  .dependsOn(sharedJS)
  .settings(
    name                            := "codefolio-client",
    scalaJSUseMainModuleInitializer := true,
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.ESModule) },
    libraryDependencies ++= Seq(
      "com.github.japgolly.scalajs-react" %%% "core"  % scalajsReactV,
      "com.github.japgolly.scalajs-react" %%% "extra" % scalajsReactV
    )
  )

// ---- root aggregate ------------------------------------------------------

lazy val root = (project in file("."))
  .aggregate(sharedJVM, sharedJS, server, client)
  .settings(
    name           := "codefolio",
    publish / skip := true
  )
