import com.github.os72.protocjar.Protoc
import com.trueaccord.scalapb.{ScalaPbPlugin => PB}
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt._
import sbt.Keys._

object ScalaPB {
  lazy val settings =
    PB.protobufSettings ++
    inConfig(PB.protobufConfig)(protobufConfigSettings) ++
    Seq(dependencies)

  lazy val dependencies = libraryDependencies ++= Seq(
    "com.trueaccord.scalapb" %%% "scalapb-runtime" % Versions.ScalaPB,
    "com.trueaccord.scalapb" %%% "scalapb-runtime" % Versions.ScalaPB % PB.protobufConfig
  )

  val protobufSharedDirectory = file("protocol") / "shared" / "src" / "main" / "protobuf"
  private lazy val protobufConfigSettings = Seq(
    PB.flatPackage := true,
    PB.runProtoc := (args => Protoc.runProtoc("-v300" +: args.toArray)),
    sourceDirectories += protobufSharedDirectory,
    PB.includePaths += protobufSharedDirectory
  )
}

