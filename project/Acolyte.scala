import sbt._
import Keys._

// Multi-module project build
object Acolyte extends Build with Core {

  lazy val scala = Project(id = "scala", base = file("scala")).dependsOn(core)

  lazy val root = Project(id = "acolyte", base = file(".")).
    aggregate(core, scala).settings(
      version := "1.0.1",
      scalaVersion := "2.10.0",
      publishTo := Some(Resolver.file("file", 
        new File(Path.userHome.absolutePath+"/.m2/repository"))))
}