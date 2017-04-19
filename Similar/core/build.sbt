name := "Core"

version := "1.0"

scalaVersion := "2.10.3"

libraryDependencies ++= Seq(
"org.apache.lucene" % "lucene-core"  % "6.4.1",
"org.apache.commons" % "commons-math3" % "3.6.1",
"org.slf4j"         % "slf4j-api"    % "1.7.5",
"org.slf4j"         % "slf4j-simple" % "1.7.5"
)

logLevel in assembly := Level.Debug
