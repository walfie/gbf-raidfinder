// sbt-scalapb must be before sbt-scalajs
// https://github.com/trueaccord/ScalaPB/issues/150#issuecomment-236232402
addSbtPlugin("com.trueaccord.scalapb" % "sbt-scalapb" % "0.5.38")

addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.6.0")

addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.12")

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.1.5")

addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.3")

addSbtPlugin("com.heroku" % "sbt-heroku" % "1.0.1")

addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.6.1")

addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "0.8.5")

