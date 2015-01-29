lazy val buildSettings = Seq(
  name := "elastic-http",
  organization := "com.blinkbox.books",
  version := scala.util.Try(scala.io.Source.fromFile("VERSION").mkString.trim).getOrElse("0.0.0"),
  scalaVersion := "2.11.4",
  scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-encoding", "utf8", "-target:jvm-1.7"),
  libraryDependencies ++= Seq(
    "com.sksamuel.elastic4s" %%  "elastic4s"            % "1.4.9",
    "com.typesafe.akka"      %%  "akka-actor"           % "2.3.8",
    "io.spray"               %%  "spray-client"         % "1.3.2",
    "org.json4s"             %%  "json4s-jackson"       % "3.2.11",
    "org.elasticsearch"      %   "elasticsearch-groovy" % "1.4.2",
    "org.scalatest"          %%  "scalatest"            % "2.2.1" % Test
  ),
  publishMavenStyle := true,
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshot")
    else
      Some("releases" at nexus + "service/local/staging/deploy/maven2")
  },
  publishArtifact in Test := false,
  homepage := Some(url("http://github.com/blinkboxbooks/elastic-http")),
  licenses := Seq("MIT" -> url("http://www.opensource.org/licenses/mit-license.php")),
  pomExtra := (
    <scm>
      <url>git@github.com:blinkboxbooks/elastic-http.git</url>
      <connection>scm:git:git@github.com:blinkboxbooks/elastic-http.git</connection>
    </scm>
    <developers>
      <developer>
        <id>astrac</id>
        <name>Aldo Stracquadanio</name>
        <url>http://blog.astrac.me</url>
      </developer>
    </developers>)
)

lazy val root = (project in file(".")).
  settings(buildSettings: _*)
