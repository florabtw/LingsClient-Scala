name := "LingsClient-Scala"

version := "1.0"

scalaVersion := "2.12.1"

// libraryDependencies += "com.typesafe.play" %% "play-json" % "2.6.0-M1"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http"            % "10.0.3",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.3"
)
