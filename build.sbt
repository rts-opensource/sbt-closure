import bintray.Keys._

sbtPlugin := true

name := "sbt-jsmanifest"

organization := "ch.srg"

version := "0.1.0"

publishMavenStyle := false

homepage := Some(url("https://github.com/rts-opensource"))

licenses += ("Apache-2.0", url("https://raw.github.com/rts-opensource/sbt-jsmanifest/master/LICENSE"))

bintrayPublishSettings

repository in bintray := "sbt-plugins"

bintrayOrganization in bintray := Some("rts")

libraryDependencies += "com.google.javascript" % "closure-compiler" % "r1741"

libraryDependencies += "org.scalatest" %% "scalatest" % "1.9.1" % "test"

scriptedSettings

scriptedLaunchOpts <+= version { "-Dplugin.version=" + _ }

scriptedBufferLog := false

scalacOptions := Seq("-deprecation", "-unchecked", "-encoding", "utf8")
