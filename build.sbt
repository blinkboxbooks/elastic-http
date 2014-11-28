import AssemblyKeys._

name := "elastic-http"

lazy val buildSettings = Seq(
  organization := "com.blinkbox.books",
  version := scala.util.Try(scala.io.Source.fromFile("VERSION").mkString.trim).getOrElse("0.0.0"),
  scalaVersion := "2.11.4",
  scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-encoding", "utf8", "-target:jvm-1.7"),
  libraryDependencies ++= Seq(
    "org.elasticsearch"   %   "elasticsearch"     % "1.4.1",
    "com.blinkbox.books"  %%  "common-scala-test" % "0.3.0"   % Test
  )
)

lazy val root = (project in file(".")).
  settings(buildSettings: _*)
