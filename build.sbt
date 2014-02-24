import bintray.Keys._

sbtPlugin := true

name := "sbt-jsmanifest"

organization := "ch.srg"

version := "0.0.1-SNAPSHOT"

publishMavenStyle := false

homepage := Some(url("https://github.com/eltimn/sbt-closure"))

licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0.html"))

bintrayPublishSettings

repository in bintray := "sbt-plugins"

bintrayOrganization in bintray := None

libraryDependencies += "com.google.javascript" % "closure-compiler" % "r1741"

libraryDependencies += "org.scalatest" %% "scalatest" % "1.9.1" % "test"

scriptedSettings

scriptedLaunchOpts <+= version { "-Dplugin.version=" + _ }

scriptedBufferLog := false

scalacOptions := Seq("-deprecation", "-unchecked", "-encoding", "utf8")
