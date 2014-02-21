package sbtclosure

import java.nio.charset.Charset

import scala.io.Source

import sbt._

class Manifest(val file: File, downloadDir: File, charset: Charset) {
  lazy val sources: List[ManifestObject] = {
    IO.readLines(file, charset)
      .map(line => "#.*$".r.replaceAllIn(line, "").trim)
      .filterNot(_.isEmpty)
      .map(line => {
        if (line.matches("^https?:"))
          ManifestUrl(downloadDir, line)
        else
          ManifestFile(sbt.file(file.getParent), line)
      })
  }

  def newerThan(other : java.io.File): Boolean = {
    (file newerThan other) || (other.lastModified < sources.foldLeft(0L)((i, mo) => i max mo.file.lastModified))
  }
}

sealed abstract class ManifestObject(parent: File) {
  def file(): File
}

case class ManifestFile(parent: File, filename: String) extends ManifestObject(parent) {
  def file(): File = parent / filename
}

case class ManifestUrl(parent: File, url: String) extends ManifestObject(parent) {
  lazy val filename: String = """[^A-Za-z0-9.]""".r.replaceAllIn(url, "_")

  protected def content: String = Source.fromInputStream(new URL(url).openStream).mkString

  def file(): File = {
    val f = parent / filename

    if (!f.exists()) {
      IO.createDirectory(parent)
      IO.write(f, content)
    }

    f
  }
}
