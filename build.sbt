lazy val buildSettings = Seq(
  name := "elastic-http",
  organization := "com.blinkbox.books",
  version := scala.util.Try(scala.io.Source.fromFile("VERSION").mkString.trim).getOrElse("0.0.0"),
  scalaVersion := "2.11.4",
  crossScalaVersions := Seq("2.11.4"),
  scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-encoding", "utf8", "-target:jvm-1.7"),
  libraryDependencies ++= Seq(
    "com.sksamuel.elastic4s" %% "elastic4s"       % "1.4.0",
    "com.typesafe.akka"   %%  "akka-actor"        % "2.3.7",
    "io.spray"            %%  "spray-client"      % "1.3.2",
    "org.json4s"          %%  "json4s-jackson"    % "3.2.11",
    "com.blinkbox.books"  %%  "common-scala-test" % "0.3.0" % Test
  )
)

lazy val root = (project in file(".")).
  settings(buildSettings: _*)
