package ch.srg.sbt.jsmanifest

import java.nio.charset.Charset

import ch.srg.sbt.SbtWebSourceFilePlugin
import com.typesafe.sbt.web._
import sbt.Keys._
import sbt._

object Import {
  object JsManifestKeys {
    lazy val jsManifest         = TaskKey[Seq[File]]("jsManifest", "Compiles .jsm javascript manifest files")
    lazy val charset            = SettingKey[Charset]("charset", "Sets the character encoding used in file IO. Defaults to utf-8")
  }
}

object SbtJsManifest extends SbtWebSourceFilePlugin {

  override def requires = SbtWeb

  override def trigger = AllRequirements

  val autoImport = Import

  import ch.srg.sbt.jsmanifest.SbtJsManifest.autoImport.JsManifestKeys._

  override def projectSettings: Seq[Setting[_]] = Seq(
    charset := Charset.forName("utf-8")
  ) ++ addSourceFileTasks(jsManifest)

  protected def mainSourceFileTask(config: Configuration): Def.Initialize[Task[Seq[File]]] = Def.task {
    val sourceDir = (sourceDirectory in jsManifest in config).value
    val sources = sourceDir ** ("*.jsm" | "*.jsmanifest")
    val mappings = sources pair relativeTo(sourceDir)
    val downloadDir = (streams in config).value.cacheDirectory / "manifest-downloads"

    // Find out which manifest needs to be compiled.
    val manifests = for {
      (manifest, outFile) <- mappings map {
        case (file, path) =>
          new Manifest(file, downloadDir, (charset in jsManifest).value) ->
            (resourceManaged in jsManifest in config).value / path.replaceAll("""[.]jsm(anifest)?$""", ".js")
      }
      if manifest newerThan outFile
    }
    yield { (manifest, outFile) }

    // Compile manifests.
    manifests match {
      case Nil =>
        streams.value.log.debug("No JavaScript manifest files to compile")
        Nil
      case tmp =>
        streams.value.log.info("Compiling %d jsm files." format tmp.size)

        val compiled = for {
          (manifest, outFile) <- tmp if manifest.compileTo(outFile)
        } yield outFile

        if (compiled.size < tmp.size)
          streams.value.log.warn("Failed to compile %d jsm files" format (tmp.size - compiled.size))

        compiled
    }
  }
}
