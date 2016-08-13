lazy val commonSettings = Seq(
  scalaVersion := "2.11.8",
  organization := "com.github.walfie",
  scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked", "-Xlint"),
  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % Versions.ScalaTest % "test"
  )
)

lazy val core = (project in file("core"))
  .settings(commonSettings: _*)
  .settings(
    name := "granblue-raid-finder",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-agent" % Versions.Akka,
      "io.monix" %% "monix" % Versions.Monix,
      "org.twitter4j" % "twitter4j-core" % Versions.Twitter4j
    )
  )

lazy val root = (project in file("."))
  .aggregate(core)
  .settings(
    aggregate in update := false
  )

