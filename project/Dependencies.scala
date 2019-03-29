import sbt._

object Dependencies {
  private val akkaVersion                 = "2.5.13"
  private val akkaHttpVersion             = "10.1.1" //10.0.7
  private val scalaLoggingVersion         = "3.1.0"
  private val logbackVersion              = "1.1.7"
  private val json4sVersion               = "3.5.2"
  private val akkaJson4sVersion           = "1.18.1"
  private val akkaHttpCors                = "0.3.0"

  val akkaDependencies: Seq[ModuleID] = Seq(
    "com.typesafe.akka"           %% "akka-actor"                 % akkaVersion,
    "com.typesafe.akka"           %% "akka-stream"                % akkaVersion,
    "com.typesafe.akka"           %% "akka-http"                  % akkaHttpVersion,
    "com.typesafe.akka"           %% "akka-remote"                % akkaVersion
  )

  val jsonDependencies: Seq[ModuleID] = Seq(
    "org.json4s"                  %% "json4s-jackson"             % json4sVersion,
    "de.heikoseeberger"           %% "akka-http-json4s"           % akkaJson4sVersion exclude("org.slf4j", "slf4j-log4j12")
  )

  val corsDependencies: Seq[ModuleID] = Seq(
    "ch.megard"                   %% "akka-http-cors"             % akkaHttpCors exclude("org.slf4j", "slf4j-log4j12")
  )

  val loggingDependencies: Seq[ModuleID] = Seq(
    "com.typesafe.scala-logging"  %% "scala-logging"              % scalaLoggingVersion,
    "ch.qos.logback"              %  "logback-classic"            % logbackVersion,
    "com.typesafe.akka"           %  "akka-slf4j_2.11"            % akkaVersion
  )
}
