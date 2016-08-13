scalaVersion := "2.11.8"

organization := "com.github.walfie"

name := "granblue-raid-finder"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-agent" % Versions.Akka,
  "io.monix" %% "monix" % Versions.Monix,
  "org.twitter4j" % "twitter4j-core" % Versions.Twitter4j
)

scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked", "-Xlint")

