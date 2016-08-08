scalaVersion := "2.11.8"

organization := "com.github.walfie"

name := "granblue-raid-tracker"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % Versions.Akka,
  "com.typesafe.akka" %% "akka-cluster-tools" % Versions.Akka,
  "com.typesafe.akka" %% "akka-http-experimental" % Versions.Akka,
  "com.typesafe.play" %% "play-json" % Versions.Play,
  "org.twitter4j" % "twitter4j-core" % Versions.Twitter4j
)

scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked", "-Xlint")

