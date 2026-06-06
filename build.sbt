lazy val akkaHttpVersion = "10.2.9"
lazy val akkaVersion = "2.6.19"
lazy val circeVersion = "0.14.1"

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
    // akka system
    "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,
    // circe for JSON support
    "de.heikoseeberger" %% "akka-http-circe" % "1.39.2",
    "io.circe" %% "circe-core" % circeVersion,
    "io.circe" %% "circe-generic" % circeVersion,
    "io.circe" %% "circe-parser" % circeVersion,
    // logging framework
    "ch.qos.logback" % "logback-classic" % "1.2.11",
    // testing frameworks
    "org.scalatest" %% "scalatest" % "3.2.12" % Test,
    "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test,
    "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test
  )
)
