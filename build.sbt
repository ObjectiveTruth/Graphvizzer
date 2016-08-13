name := """graphviz-slack-app"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file("."))
    .enablePlugins(PlayScala, GatlingPlugin)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
    jdbc,
    cache,
    // Play Web Service wrapper for the underlying AsyncHttpClient
    ws,
    // Play Filters for handling requests as they come in
    filters,
    // Play Slick Functional Relational Mapper
    "com.typesafe.play" %% "play-slick" % "2.0.0",
    // In-memory databsae H2
    "com.h2database" % "h2" % "1.4.192",

    // Test Dependancies
    "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test,
    "io.gatling.highcharts" % "gatling-charts-highcharts" % "2.2.2" % Test,
    "io.gatling"            % "gatling-test-framework"    % "2.2.2" % Test
)

// Removes API Docs from binary
sources in (Compile, doc) := Seq.empty
publishArtifact in (Compile, packageDoc) := false
