package ch.srg.sbt.jsmanifest

import java.nio.charset.Charset

import ch.srg.sbt.SbtWebSourceFilePlugin
import com.typesafe.sbt.web._
import sbt.Keys._
import sbt._

object Import {
  object JsManifestKeys {
    lazy val jsManifest = TaskKey[Seq[File]]("jsManifest", "Compiles .jsm javascript manifest files")
    lazy val charset    = SettingKey[Charset]("charset", "Sets the character encoding used in file IO. Defaults to utf-8")
  }
}

object SbtJsManifest extends SbtWebSourceFilePlugin {

  override def requires = SbtWeb

  override def trigger = AllRequirements

  val autoImport = Import

  import autoImport.JsManifestKeys._

  override def projectSettings: Seq[Setting[_]] = Seq(
    charset := Charset.forName("utf-8")
  ) ++ addSourceFileTasks(jsManifest)

  /**
   * Find all the js mannifest files in source directory and compile them.
   *
   * @param config Configuration, can be one of Assets or TestAssets
   * @return Task definition
   */
  protected def mainSourceFileTask(config: Configuration): Def.Initialize[Task[Seq[File]]] = Def.task {
    val logger: Logger = state.value.log
    val sourceDir = (sourceDirectory in jsManifest in config).value
    val sources = sourceDir ** ("*.jsm" | "*.jsmanifest")
    val downloadDir = (streams in config).value.cacheDirectory / "manifest-downloads"

    sources pair relativeTo(sourceDir) map {
      case (file, path) =>
        val manifest = new Manifest(file, downloadDir, (charset in jsManifest).value)
        val outFile = (resourceManaged in jsManifest in config).value / path.replaceAll("""[.]jsm(anifest)?$""", ".js")

        if (manifest newerThan outFile) {
          logger.info(s"Compiling javascript manifest: ${file.getAbsolutePath}")
          manifest.compileTo(outFile)
        }

        outFile
    }
  }
}
