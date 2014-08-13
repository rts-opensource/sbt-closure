package ch.srg.sbt

import com.typesafe.sbt.web.Import.WebKeys._
import com.typesafe.sbt.web.Import._
import sbt.Keys._
import sbt._

trait SbtWebSourceFilePlugin extends AutoPlugin {
  private def addUnscopedSourceFileTasks(sourceFileTask: TaskKey[Seq[File]]): Seq[Setting[_]] = {
    Seq(
      resourceGenerators <+= sourceFileTask,
      managedResourceDirectories += (resourceManaged in sourceFileTask).value
    ) ++ inTask(sourceFileTask)(Seq(
      sourceDirectories := unmanagedSourceDirectories.value ++ managedSourceDirectories.value,
      sources := unmanagedSources.value ++ managedSources.value
    ))
  }

  protected def addSourceFileTasks(sourceFileTask: TaskKey[Seq[File]]): Seq[Setting[_]] = {
    Seq(
      sourceFileTask in Assets := mainSourceFileTask(Assets).value,
      sourceFileTask in TestAssets := mainSourceFileTask(TestAssets).value,
      resourceManaged in sourceFileTask in Assets := webTarget.value / sourceFileTask.key.label / "main",
      resourceManaged in sourceFileTask in TestAssets := webTarget.value / sourceFileTask.key.label / "test",
      sourceFileTask := (sourceFileTask in Assets).value
    ) ++
      inConfig(Assets)(addUnscopedSourceFileTasks(sourceFileTask)) ++
      inConfig(TestAssets)(addUnscopedSourceFileTasks(sourceFileTask))
  }

  protected def mainSourceFileTask(config: Configuration): Def.Initialize[Task[Seq[File]]]
}
