ThisBuild / resolvers ++= Seq(
  "Apache Development Snapshot Repository" at "https://repository.apache.org/content/repositories/snapshots/",
  Resolver.mavenLocal
)

name := "CloudtrailFlinkAnalytics"

version := "0.1-SNAPSHOT"

organization := "com.ms"

ThisBuild / scalaVersion := "2.12.12"

val flinkVersion = "1.12.0"

val flinkDependencies = Seq(
  "org.apache.flink" %% "flink-clients" % flinkVersion % "provided",
  "org.apache.flink" %% "flink-scala" % flinkVersion % "provided",
  "org.apache.flink" %% "flink-streaming-scala" % flinkVersion % "provided",
  "org.apache.flink" %% "flink-connector-kinesis" % flinkVersion % "provided"
)

val playJsonVersion = "2.9.1"
libraryDependencies += "com.typesafe.play" %% "play-json" % playJsonVersion

lazy val root = (project in file(".")).
  settings(
    libraryDependencies ++= flinkDependencies
  )

assembly / mainClass := Some("com.ms.streaming.kinesis.KinesisFlinkConsumer")

// make run command include the provided dependencies
Compile / run := Defaults.runTask(Compile / fullClasspath,
  Compile / run / mainClass,
  Compile / run / runner
).evaluated

// stays inside the sbt console when we press "ctrl-c" while a Flink programme executes with "run" or "runMain"
Compile / run / fork := true
Global / cancelable := true

// exclude Scala library from assembly
assembly / assemblyOption := (assembly / assemblyOption).value.copy(includeScala = false)

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs@_*) => MergeStrategy.discard
  case x => MergeStrategy.first
}

// Commands
// sbt clean
// sbt assembly
