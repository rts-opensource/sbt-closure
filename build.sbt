import bintray.Keys._

sbtPlugin := true

name := "sbt-jsmanifest"

organization := "ch.srg"

version := "1.0.0-SNAPSHOT"

scalaVersion := "2.10.4"

scalacOptions += "-feature"

resolvers ++= Seq(
  "Typesafe Releases Repository" at "http://repo.typesafe.com/typesafe/releases/",
  Resolver.url("sbt snapshot plugins", url("http://repo.scala-sbt.org/scalasbt/sbt-plugin-snapshots"))(Resolver.ivyStylePatterns),
  Resolver.sonatypeRepo("snapshots"),
  "Typesafe Snapshots Repository" at "http://repo.typesafe.com/typesafe/snapshots/",
  Resolver.mavenLocal
)

addSbtPlugin("com.typesafe.sbt" %% "sbt-web" % "1.0.2")

publishMavenStyle := false

homepage := Some(url("https://github.com/rts-opensource"))

licenses += ("Apache-2.0", url("https://raw.github.com/rts-opensource/sbt-jsmanifest/master/LICENSE"))

bintrayPublishSettings

repository in bintray := "sbt-plugins"

bintrayOrganization in bintray := Some("rts")

libraryDependencies += "com.google.javascript" % "closure-compiler" % "r1741"

scriptedSettings

scriptedLaunchOpts <+= version apply { v => s"-Dproject.version=$v" }
