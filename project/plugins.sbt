addSbtPlugin("org.scala-js"       % "sbt-scalajs"              % "1.22.0")
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.4.0")
addSbtPlugin("com.github.sbt"     % "sbt-native-packager"      % "1.11.7")
addSbtPlugin("io.spray"           % "sbt-revolver"             % "0.10.0")
// Pinned: sbt-scalafmt 2.6.x requires sbt 1.12.9+, and this build is on sbt 1.10.7
// (project/build.properties + .tool-versions + the Dockerfile builder image tag).
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.6")
