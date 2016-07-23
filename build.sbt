name := """graphviz-slack-app"""

version := "1.0"

scalaVersion := "2.11.8"

// Change this to another test framework if you prefer
libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.4" % "test"

// AWS only supports java 1.8 this is required
javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint")

// Uncomment to use Akka
//libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.3.11"

lazy val root = (project in file(".")).
    settings(
        name := "graphviz-slack-app",
        version := "1.0",
        scalaVersion := "2.11.8",
        retrieveManaged := true,
        libraryDependencies += "com.amazonaws" % "aws-lambda-java-core" % "1.0.0",
        libraryDependencies += "com.amazonaws" % "aws-lambda-java-events" % "1.0.0",
        libraryDependencies += "com.fasterxml.jackson.module" % "jackson-module-scala_2.11" % "2.7.2",
        libraryDependencies +=  "org.scalaj" %% "scalaj-http" % "2.3.0"
    )

