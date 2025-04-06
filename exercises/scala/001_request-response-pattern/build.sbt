ThisBuild / version := "0.0.1"

ThisBuild / scalaVersion := "3.6.4"

lazy val root = (project in file("."))
  .settings(
    name := "coffee-house-ex1-scala"
  )

val akkaVersion = "2.8.8"
lazy val scalaTestVersion = "3.2.19"

fork := true

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "ch.qos.logback" % "logback-classic" % "1.5.18",
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
  "org.scalatest" %% "scalatest" % scalaTestVersion % Test,
)
