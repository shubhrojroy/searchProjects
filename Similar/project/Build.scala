import sbt._
import Keys._

object SimilarBuild extends Build {
  
  lazy val root = Project(id = "similar", base = file(".")) aggregate(core, service) dependsOn(core, service)

  lazy val core = Project(id = "similar-core", base = file("core"))

  lazy val service = Project(id = "similar-service", base = file("service")) dependsOn (core)
  
}
