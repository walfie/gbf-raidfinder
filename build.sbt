scalaVersion := "2.11.8"

organization := "com.github.walfie"

name := "granblue-raid-finder"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream" % Versions.Akka,
  "org.twitter4j" % "twitter4j-core" % Versions.Twitter4j
)

scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked", "-Xlint")

