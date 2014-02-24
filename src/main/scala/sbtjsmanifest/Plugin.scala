package sbtjsmanifest

import java.nio.charset.Charset

import sbt._

import com.google.javascript.jscomp.CompilerOptions

object SbtJsManifestPlugin extends Plugin {
  import sbt.Keys._
  import JsManifestKeys._

  object JsManifestKeys {
    lazy val jsManifest         = TaskKey[Seq[File]]("jsManifest", "Compiles .jsm javascript manifest files")
    lazy val charset            = SettingKey[Charset]("charset", "Sets the character encoding used in file IO. Defaults to utf-8")
    lazy val downloadDirectory  = SettingKey[File]("download-dir", "Directory to download ManifestUrls to")
    lazy val prettyPrint        = SettingKey[Boolean]("pretty-print", "Whether to pretty print JavaScript (default false)")
    lazy val jsManifestOptions  = SettingKey[CompilerOptions]("options", "Compiler options")
    lazy val suffix             = SettingKey[String]("suffix", "String to append to output filename (before file extension)")
  }

  def jsManifestOptionsSetting: Def.Initialize[CompilerOptions] =
    (streams, prettyPrint in jsManifest) apply {
      (out, prettyPrint) =>
        val options = new CompilerOptions
        options.prettyPrint = prettyPrint
        options
    }

  def jsManifestSettings: Seq[Setting[_]] =
    jsManifestSettingsIn(Compile) ++ jsManifestSettingsIn(Test)

  def jsManifestSettingsIn(conf: Configuration): Seq[Setting[_]] =
    inConfig(conf)(jsManifestSettings0 ++ Seq(
      sourceDirectory in jsManifest <<= (sourceDirectory in conf) { _ / "javascript" },
      resourceManaged in jsManifest <<= (resourceManaged in conf) { _ / "js" },
      downloadDirectory in jsManifest <<= (target in conf) { _ / "jsManifest-downloads" },
      cleanFiles in jsManifest <<= (resourceManaged in jsManifest, downloadDirectory in jsManifest)(_ :: _ :: Nil),
      watchSources <<= (unmanagedSources in jsManifest)
    )) ++ Seq(
      cleanFiles <++= (cleanFiles in jsManifest in conf),
      watchSources <++= (watchSources in jsManifest in conf),
      resourceGenerators in conf <+= jsManifest in conf,
      compile in conf <<= (compile in conf).dependsOn(jsManifest in conf)
    )

  def jsManifestSettings0: Seq[Setting[_]] = Seq(
    charset in jsManifest := Charset.forName("utf-8"),
    prettyPrint := false,
    jsManifestOptions <<= jsManifestOptionsSetting,
    includeFilter in jsManifest := "*.jsm",
    excludeFilter in jsManifest := (".*" - ".") || HiddenFileFilter,
    suffix in jsManifest := "",
    unmanagedSources in jsManifest <<= jsManifestSourcesTask,
    clean in jsManifest <<= jsManifestCleanTask,
    jsManifest <<= jsManifestCompilerTask
  )

  private def jsManifestCleanTask =
    (streams, resourceManaged in jsManifest) map {
      (out, target) =>
        out.log.info("Cleaning generated JavaScript under " + target)
        IO.delete(target)
    }

  private def jsManifestCompilerTask =
    (streams, sourceDirectory in jsManifest, resourceManaged in jsManifest,
     includeFilter in jsManifest, excludeFilter in jsManifest, charset in jsManifest,
     downloadDirectory in jsManifest, jsManifestOptions in jsManifest, suffix in jsManifest) map {
      (out, sources, target, include, exclude, charset, downloadDir, options, suffix) => {
        // compile changed sources
        (for {
          manifest <- sources.descendantsExcept(include, exclude).get.map(new Manifest(_, downloadDir, charset))
          outFile <- computeOutFile(sources, manifest, target, suffix)
          if manifest newerThan outFile
        } yield { (manifest, outFile) }) match {
          case Nil =>
            out.log.debug("No JavaScript manifest files to compile")
          case xs =>
            out.log.info("Compiling %d jsm files to %s" format(xs.size, target))
            xs map doCompile(out.log, options)
            out.log.debug("Compiled %s jsm files" format xs.size)
        }
        compiled(target)
      }
    }

  private def jsManifestSourcesTask =
    (sourceDirectory in jsManifest, includeFilter in jsManifest, excludeFilter in jsManifest) map {
      (sourceDir, incl, excl) =>
         sourceDir.descendantsExcept(incl, excl).get
    }

  private def doCompile(log: Logger, options: CompilerOptions)(pair: (Manifest, File)) = {
    val (jsm, js) = pair
    log.debug("Compiling %s" format jsm)
    Compiler(options).compile(jsm, js, log)
  }

  private def compiled(under: File) = (under ** "*.js").get

  private def computeOutFile(sources: File, manifest: Manifest, targetDir: File, suffix: String): Option[File] = {
    val outFile = IO.relativize(sources, manifest.file).get.replaceAll("""[.]jsm(anifest)?$""", "") + {
      if (suffix.length > 0) "-%s.js".format(suffix)
      else ".js"
    }
    Some(new File(targetDir, outFile))
  }
}
