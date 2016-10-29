// TODO: Put all dependencies in a Dependencies.scala file with versions

lazy val commonSettings = Seq(
  scalaVersion := "2.11.8",
  organization := "com.github.walfie",
  scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked", "-Xlint"),

  // Disable publishing of {java,scala}doc
  publishArtifact in (Compile, packageDoc) := false,
  publishArtifact in packageDoc := false,
  sources in (Compile, doc) := Seq.empty,
  Scalariform.settings
)

lazy val buildInfo = (crossProject.crossType(CrossType.Pure) in file(".build-info"))
  .enablePlugins(ScalaJSPlugin, BuildInfoPlugin)
  .settings(commonSettings: _*)
  .settings(
    buildInfoPackage := "walfie.gbf.raidfinder",
    buildInfoKeys := Seq[BuildInfoKey](version, git.gitHeadCommit)
  )
lazy val buildInfoJVM = buildInfo.jvm
lazy val buildInfoJS = buildInfo.js

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

lazy val protocol = (crossProject in file("protocol"))
  .settings(name := "gbf-raidfinder-protocol")
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings: _*)
  .settings(ScalaPB.settings: _*)
lazy val protocolJVM = protocol.jvm
lazy val protocolJS = protocol.js

lazy val server = (project in file("server"))
  .settings(commonSettings: _*)
  .settings(Defaults.itSettings: _*)
  .configs(IntegrationTest)
  .settings(
    name := "gbf-raidfinder-server",
    resolvers += Resolver.jcenterRepo, // for ficus
    libraryDependencies ++= Seq(
      "com.iheart" %% "ficus" % "1.2.6",
      "com.trueaccord.scalapb" %% "scalapb-json4s" % Versions.ScalaPB_json4s,
      "com.typesafe.play" %% "filters-helpers" % Versions.Play,
      "com.typesafe.play" %% "play-logback" % Versions.Play,
      "com.typesafe.play" %% "play-netty-server" % Versions.Play,
      "org.scalatest" %% "scalatest" % Versions.ScalaTest % "it,test",
      "redis.clients" % "jedis" % "2.8.1"
    )
  )
  .dependsOn(stream, protocolJVM, buildInfoJVM)

val jsPath = settingKey[File]("Output directory for scala.js compiled files")
lazy val client = (project in file("client"))
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings: _*)
  .settings(
    name := "gbf-raidfinder-client",

    // Put output JS files in `target/scala_2.11/classes/public/js`
    jsPath := crossTarget.value / "classes" / "public" / "js",
    crossTarget in (Compile, fullOptJS) := jsPath.value,
    crossTarget in (Compile, fastOptJS) := jsPath.value,
    crossTarget in (Compile, packageJSDependencies) := jsPath.value,
    crossTarget in (Compile, packageScalaJSLauncher) := jsPath.value,
    crossTarget in (Compile, packageMinifiedJSDependencies) := jsPath.value,

    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.9.1",
      "com.thoughtworks.binding" %%% "dom" % "9.0.2",
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
  .dependsOn(protocolJS, buildInfoJS)

// TODO: Running `test` on the root project doesn't test `stream` project
lazy val root = (project in file("."))
  .enablePlugins(JavaServerAppPackaging)
  .dependsOn(server, client)
  .settings(commonSettings: _*)
  .settings(
    name := "gbf-raidfinder",
    publish := (),
    releaseProcess -= ReleaseTransformations.publishArtifacts,
    mainClass in Compile := Some("walfie.gbf.raidfinder.server.Application"),

    run in Compile <<= (run in Compile) dependsOn (fastOptJS in (client, Compile)),
    stage <<= stage dependsOn (fullOptJS in (client, Compile)),
    herokuAppName in Compile := "gbf-raidfinder",
    herokuSkipSubProjects in Compile := false,
    herokuProcessTypes in Compile := Map(
      "web" -> Seq(
        s"target/universal/stage/bin/${name.value}",
        "-Dhttp.port=$PORT",
        "-Dapplication.cache.redisUrl=$REDISCLOUD_URL",
        "-Dapplication.mode=prod"
      ).mkString(" ")
    )
  )

