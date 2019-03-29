import sbt.Keys._
import sbt._
import xerial.sbt.Pack._

object Build extends Build {

  val commonModuleName = "common"
  val apiModuleName = "akka-api"
  val namePrefix = "brunogbv"

  name := namePrefix + "."

  private val autoSettings = Seq(crossPaths := false, packGenerateWindowsBatFile := true, packJarNameConvention := "default")

  lazy val commonWrapper: Project = Project(
    id = commonModuleName,
    base = file(commonModuleName)
  ).settings(Common.settings: _*)
    .settings(libraryDependencies ++= Dependencies.akkaDependencies ++ Dependencies.jsonDependencies ++ Dependencies.corsDependencies ++ Dependencies.loggingDependencies)

  lazy val apiWrapper: Project = Project(
    id = apiModuleName,
    base = file(apiModuleName)
  ).settings(Common.settings: _*)
    .settings(packAutoSettings ++ autoSettings)
    .settings(packMain := Map("Main" -> "stone.hermes.api.HermesApiMain"))
    .settings(mainClass in Compile := Some("stone.hermes.api.HermesApiMain"))
    .settings(packJvmOpts := Map("Main" -> Seq("-Xmx2G -Xms1G ")))
    .settings(javaOptions in run += "-Xmx2G -Xms1G")
    .dependsOn(commonWrapper)

}