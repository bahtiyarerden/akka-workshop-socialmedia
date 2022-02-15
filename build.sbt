name := "akka-workshop-socialmedia"

version := "0.1"

scalaVersion := "2.13.4"
val akkaHttpVersion     = "10.2.4"
val akkaVersion         = "2.6.12"
val scalaTestVersion    = "3.1.4"
val mockitoTestVersion  = "1.16.37"
val logbackVersion      = "1.2.3"
val scalaLoggingVersion = "3.9.3"
val akkaHttpJsonVersion = "1.36.0"

libraryDependencies ++= Seq(
  "ch.qos.logback"              % "logback-classic"          % logbackVersion,
  "com.typesafe.scala-logging" %% "scala-logging"            % scalaLoggingVersion,
  "com.typesafe.akka"          %% "akka-http"                % akkaHttpVersion,
  "com.typesafe.akka"          %% "akka-http-caching"        % akkaHttpVersion,
  "com.typesafe.akka"          %% "akka-actor"               % akkaVersion,
  "com.typesafe.akka"          %% "akka-actor-typed"         % akkaVersion,
  "com.typesafe.akka"          %% "akka-stream"              % akkaVersion,
  "de.heikoseeberger"          %% "akka-http-play-json"      % akkaHttpJsonVersion,
  "com.typesafe.akka"          %% "akka-stream-testkit"      % akkaVersion,
  "com.typesafe.akka"          %% "akka-http-testkit"        % akkaHttpVersion,
  "com.typesafe.akka"          %% "akka-slf4j"               % akkaVersion        % Test,
  "com.typesafe.akka"          %% "akka-actor-testkit-typed" % akkaVersion        % Test,
  "org.scalatest"              %% "scalatest"                % scalaTestVersion   % Test,
  "org.mockito"                %% "mockito-scala"            % mockitoTestVersion % Test
)
