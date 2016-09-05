// TODO: Put all dependencies in a Dependencies.scala file with versions

lazy val commonSettings = Seq(
  scalaVersion := "2.11.8",
  organization := "com.github.walfie",
  scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked", "-Xlint"),
  Scalariform.settings
)

lazy val stream = (project in file("stream"))
  .settings(commonSettings: _*)
  .settings(
    name := "gbf-raidfinder-stream",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-agent" % Versions.Akka,
      "io.monix" %% "monix" % Versions.Monix,
      "org.twitter4j" % "twitter4j-stream" % Versions.Twitter4j,
      "org.scalatest" %% "scalatest" % Versions.ScalaTest % "test",
      "org.mockito" % "mockito-all" % Versions.Mockito % "test"
    )
  )

lazy val protocol = (crossProject.crossType(CrossType.Pure) in file("protocol"))
  .settings(name := "gbf-raidfinder-protocol")
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings: _*)
  .settings(ScalaPB.settings: _*)
lazy val protocolJVM = protocol.jvm
lazy val protocolJS = protocol.js

lazy val server = (project in file("server"))
  .enablePlugins(JavaServerAppPackaging)
  .settings(commonSettings: _*)
  .settings(
    name := "gbf-raidfinder-server",
    herokuSkipSubProjects in Compile := false,
    herokuAppName in Compile := "gbf-raidfinder",
    herokuProcessTypes in Compile := Map(
      "web" -> s"target/universal/stage/bin/${name.value} -Dhttp.port=$$PORT"
    ),
    libraryDependencies ++= Seq(
      "com.trueaccord.scalapb" %% "scalapb-json4s" % Versions.ScalaPB_json4s,
      "com.typesafe.play" %% "play-netty-server" % Versions.Play
    )
  )
  .dependsOn(stream, protocolJVM)

lazy val client = (project in file("client"))
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings: _*)
  .settings(
    name := "gbf-raidfinder-client",
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.9.1",
      "com.thoughtworks.binding" %%% "dom" % "9.0.0",
      "org.webjars.npm" % "moment" % Versions.MomentJS,
      "org.webjars.bower" % "dialog-polyfill" % Versions.DialogPolyfillJS,
      compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
    ),
    jsDependencies ++= Seq(
      "org.webjars.npm" % "moment" % Versions.MomentJS
        / s"${Versions.MomentJS}/moment.js"
        minified "min/moment.min.js",

      "org.webjars.bower" % "dialog-polyfill" % Versions.DialogPolyfillJS
        / s"${Versions.DialogPolyfillJS}/dialog-polyfill.js"
    )
  )
  .dependsOn(protocolJS)

lazy val root = (project in file("."))
  .aggregate(client, stream, server)
  .settings(aggregate in update := false)


