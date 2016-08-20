lazy val commonSettings = Seq(
  scalaVersion := "2.11.8",
  organization := "com.github.walfie",
  scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked", "-Xlint"),
  Scalariform.settings
)

lazy val core = (project in file("core"))
  .settings(commonSettings: _*)
  .settings(
    name := "granblue-raid-finder-core",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-agent" % Versions.Akka,
      "io.monix" %% "monix" % Versions.Monix,
      "org.twitter4j" % "twitter4j-core" % Versions.Twitter4j,
      "org.scalatest" %% "scalatest" % Versions.ScalaTest % "test",
      "org.mockito" % "mockito-all" % Versions.Mockito % "test"
    )
  )

lazy val protocol = (crossProject.crossType(CrossType.Pure) in file("protocol"))
  .settings(commonSettings: _*)
  .settings(ScalaPB.settings: _*)
  .settings(name := "granblue-raid-finder-protocol")
lazy val protocolJVM = protocol.jvm
lazy val protocolJS = protocol.js

lazy val server = (project in file("server"))
  .settings(commonSettings: _*)
  .settings(
    name := "granblue-raid-finder-server",
    libraryDependencies ++= Seq(
      "com.trueaccord.scalapb" %% "scalapb-json4s" % Versions.ScalaPB_json4s,
      "com.typesafe.play" %% "play-netty-server" % Versions.Play
    )
  )
  .dependsOn(core, protocolJVM)

lazy val root = (project in file("."))
  .aggregate(core, server)
  .settings(aggregate in update := false)
  .enablePlugins(ScalaJSPlugin)

