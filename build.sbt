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

lazy val protocol = (project in file("protocol"))
  .settings(commonSettings: _*)
  .settings(ScalaPB.settings)
  .settings(name := "granblue-raid-finder-protocol")

lazy val server = (project in file("server"))
  .settings(commonSettings: _*)
  .settings(
    name := "granblue-raid-finder-server",
    libraryDependencies ++= Seq(
      "com.typesafe.play" %% "play-netty-server" % Versions.Play
    )
  )
  .dependsOn(core)

lazy val root = (project in file("."))
  .aggregate(core, server)
  .settings(aggregate in update := false)

