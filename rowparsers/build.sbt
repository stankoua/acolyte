name := "acolyte-rowparsers"

organization := "acolyte"

version := "1.0.2"

scalaVersion := "2.10.2"

javacOptions in Test ++= Seq("-Xlint:unchecked", "-Xlint:deprecation")

scalacOptions += "-feature"

resolvers += "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots/"

libraryDependencies ++= Seq(
  "acolyte" % "acolyte-core" % "1.0.1",
  "org.specs2" %% "specs2" % "1.14" % "test")

publishTo := Some(Resolver.file("file",  new File(Path.userHome.absolutePath+"/.m2/repository")))