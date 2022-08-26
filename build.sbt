import scala.sys.runtime

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.8"

lazy val root = (project in file("."))
  .settings(
    name := "KafkaHistory"
  )
val tethysVersion = "0.26.0"
libraryDependencies ++= Seq(
  "com.tethys-json" %% "tethys-core" % tethysVersion,
  "com.tethys-json" %% "tethys-jackson" % tethysVersion,
  "com.tethys-json" %% "tethys-derivation" % tethysVersion,
  "org.typelevel" %% "cats-effect" % "2.5.3",
  "co.fs2" %% "fs2-core" % "2.4.3",
  "com.github.fd4s" %% "fs2-kafka" % "1.1.0",
  "com.tethys-json" %% "tethys-json4s" % tethysVersion,
  "ch.qos.logback" % "logback-core" % "1.3.0-alpha16",
  "org.slf4j" % "slf4j-api" % "2.0.0",
  "org.slf4j" % "slf4j-jdk14" % "2.0.0" % Test,
  "org.slf4j" % "slf4j-log4j12" % "2.0.0" % Runtime,
  "ch.qos.logback" % "logback-classic" % "1.3.0-alpha16" % Test
)
