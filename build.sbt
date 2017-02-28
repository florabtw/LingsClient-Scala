name := "LingsClient-Scala"

version := "1.0"

scalaVersion := "2.12.1"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http"                   % "10.0.3",
  "com.typesafe.play" %% "play-json"                   % "2.6.0-M1",
  "org.scalactic"     %% "scalactic"                   % "3.0.1",
  "org.scalatest"     %% "scalatest"                   % "3.0.1"     % Test,
  "org.mockito"        % "mockito-core"                % "2.7.11"    % Test
)

mainClass in Compile := Some("runner.ClientRunner")
