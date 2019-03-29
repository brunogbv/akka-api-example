//import sbt.Resolver
//
//val processorModuleName = "transaction-processor"
//val processorTestModuleName = "transaction-processor-test"
//val pushModuleName = "transaction-pusher"
//val pushTestModuleName = "transaction-pusher-test"
//val apiModuleName = "hermes-api"
//val apiTestModuleName = "hermes-api-test"
//val commonModuleName = "common"
//val commonTestModuleName = "common-test"
//val merchantManagementModuleName = "merchant-management"
//val merchantManagementTestModuleName = "merchant-management-test"
//val stoneCodeManagementModuleName = "stone-code-management"
//val stoneCodeManagementTestModuleName = "stone-code-management-test"
//val userManagementModuleName = "user-management"
//val userManagementTestModuleName = "user-management-test"
//val pushedTransactionManagementModuleName = "pushed-transaction-management"
//val pushedTransactionManagementTestModuleName = "pushed-transaction-management-test"
//val transactionToPushManagementModuleName = "transaction-to-push-management"
//val transactionToPushManagementTestModuleName = "transaction-to-push-management-test"
//val namePrefix = "stone.fraud-detection.hermes"
//
//resolvers in ThisBuild ++= Seq(
//  Resolver.defaultLocal,
//  Resolver.mavenLocal,
//  Classpaths.typesafeReleases,
//  Classpaths.sbtPluginReleases,
//  "Apache Development Snapshot Repository" at "https://repository.apache.org/content/repositories/snapshots/",
//  "Stone Artifacts-snapshots" at "https://artifacts.stone.com.br:8443/artifactory/maven-local",
//  "Apache Staging" at "https://repository.apache.org/content/repositories/staging/",
//  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
//  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
//  "Java.net Maven2 Repository" at "http://download.java.net/maven/2/",
//  "Eclipse repositories" at "https://repo.eclipse.org/service/local/repositories/egit-releases/content/",
//  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
//  "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases",
//  "Typesafe Releases" at "http://repo.typesafe.com/typesafe/maven-releases/"
//)
//
//name := "hermes-cluster"
//scalaVersion in ThisBuild := "2.11.8"
//
//lazy val commonSettings = Seq(
//  version := "0.0.1",
//  organization := "stone.fraud-detection",
//  scalaVersion := "2.11.8"
//)
//
//lazy val root =
//  project.in(file("."))
//    .settings(commonSettings)
//    .settings(libraryDependencies ++= Dependencies.commonDependencies)
//    .aggregate(cepApi, flinkManager, flowManager)
//
//lazy val common = project
//  .in(file(commonModuleName))
//  .settings(commonSettings)
//  .settings(name := commonModuleName)
//  .settings(libraryDependencies ++= Dependencies.commonDependencies)
//  .enablePlugins(PackPlugin)
//
//lazy val processorWrapper = project
//  .in(file("flink-manager"))
//  .settings(commonSettings)
//  .settings(packMain := Map("Main" -> "com.stone.cep.flink.manager.FlinkManagerMain"))
//  .settings(mainClass in assembly := Some("com.stone.cep.flink.manager.FlinkManagerMain"))
//  .settings(name := "flink-manager")
//  .settings(libraryDependencies ++= Dependencies.generalDependencies)
//  .settings(libraryDependencies ++= Dependencies.akkaDependencies)
//  .settings(libraryDependencies ++= Dependencies.flinkDependencies)
//  .settings(libraryDependencies ++= Dependencies.testDependencies)
//  .dependsOn(common)
//  .enablePlugins(PackPlugin)
//
//lazy val flowManager = project
//  .in(file("flow-manager"))
//  .settings(commonSettings)
//  .settings(packMain := Map("Main" -> "com.stone.cep.flow.manager.FlowManagerMain"))
//  .settings(mainClass in assembly := Some("com.stone.cep.flow.manager.FlowManagerMain"))
//  .settings(name := "flow-manager")
//  .settings(libraryDependencies ++= Dependencies.generalDependencies)
//  .settings(libraryDependencies ++= Dependencies.akkaDependencies)
//  .settings(libraryDependencies ++= Dependencies.flowDependencies)
//  .settings(libraryDependencies ++= Dependencies.testDependencies)
//  .dependsOn(common)
//  .enablePlugins(PackPlugin)
//
//lazy val cepApi = project
//  .in(file("cep-api"))
//  .settings(commonSettings)
//  .settings(name := "cep-api")
//  .settings(packMain := Map("Main" -> "com.stone.cep.api.CEPMain"))
//  .settings(mainClass in assembly := Some("com.stone.cep.api.CEPMain"))
//  .settings(libraryDependencies ++= Dependencies.generalDependencies)
//  .settings(libraryDependencies ++= Dependencies.apiDependencies)
//  .settings(libraryDependencies ++= Dependencies.akkaDependencies)
//  .settings(libraryDependencies ++= Dependencies.akkaHttpDependencies)
//  .settings(libraryDependencies ++= Dependencies.testDependencies)
//  .dependsOn(common)
//  .enablePlugins(PackPlugin)
//
//lazy val cepApiClient = project
//  .in(file("cep-api-client"))
//  .settings(commonSettings)
//  .settings(name := "cep-api-client")
//  .settings(libraryDependencies ++= Dependencies.generalDependencies)
//  .settings(libraryDependencies ++= Dependencies.akkaHttpDependencies)
//  .settings(libraryDependencies ++= Dependencies.testDependencies)
//  .dependsOn(common)
//  .enablePlugins(PackPlugin)
//
//lazy val defaultRule = project
//  .in(file("default-rule"))
//  .settings(commonSettings)
//  .settings(packMain := Map("Main" -> "com.stone.default.rule.DefaultRule"))
//  .settings(mainClass in assembly := Some("com.stone.default.rule.DefaultRule"))
//  .enablePlugins(AssemblyPlugin)
//  .settings(assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false))
//  .settings(libraryDependencies ++= Dependencies.ruleDependencies)
//  .settings(assemblyMergeStrategy in assembly := {
//    case PathList("META-INF", xs@_*) => MergeStrategy.discard
//    case PathList("reference.conf") => MergeStrategy.concat // includes .conf in assembly
//    case x => MergeStrategy.first
//  })
//  .dependsOn(common)
//  .dependsOn(cepApiClient)
//
//lazy val ruleRunner = project
//  .in(file("ruleRunner"))
//  .dependsOn(defaultRule)
//  .settings(
//    // we set all provided dependencies to none, so that they are included in the classpath of mainRunner
//    libraryDependencies := (libraryDependencies in defaultRule).value.map {
//      module =>
//        if (module.configurations.contains("provided")) {
//          module.withConfigurations(None)
//        } else {
//          module
//        }
//    }
//  )
//
//// make run command include the provided dependencies
//run in Compile := Defaults.runTask(fullClasspath in Compile,
//  mainClass in(Compile, run),
//  runner in(Compile, run)
//).evaluated
//
//// exclude Scala library from assembly
//assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)
