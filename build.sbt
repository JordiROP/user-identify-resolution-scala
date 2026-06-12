val PekkoVersion     = "1.0.2"
val PekkoHttpVersion = "1.0.1"
lazy val circeVersion = "0.14.1"
val ZIOVersion     = "2.1.1"  // Versión estable de ZIO 2.x
val ZIOHttpVersion = "3.0.0-RC6" // Versión recomendada de zio-http para ZIO 2.x

// Run in a separate JVM, to make sure sbt waits until all threads have
// finished before returning.
// If you want to keep the application running while executing other
// sbt tasks, consider https://github.com/spray/sbt-revolver/
fork := true

lazy val root = (project in file(".")).settings(
  inThisBuild(
    List(organization := "com.resolution", scalaVersion := "2.13.18")
  ),
  name := "scala-user-identify-resolution",
  libraryDependencies ++= Seq(
    "io.circe" %% "circe-core" % circeVersion,
    "io.circe" %% "circe-generic" % circeVersion,
    "io.circe" %% "circe-parser" % circeVersion,
    // logging framework
    "ch.qos.logback" % "logback-classic" % "1.5.6",
    // testing frameworks
    "org.scalatest" %% "scalatest" % "3.2.12" % Test,

    "dev.zio" %% "zio"         % ZIOVersion,
    "dev.zio" %% "zio-streams" % ZIOVersion,

    // 2. Servidor Web de ZIO HTTP (Netty no bloqueante integrado)
    "dev.zio" %% "zio-http" % ZIOHttpVersion,
  )
)
