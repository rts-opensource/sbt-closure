package sbtclosure

import com.google.javascript.jscomp.{Compiler => ClosureCompiler, CompilerOptions, JSError, JSSourceFile}

import sbt._

object Compiler {
  def apply(options: CompilerOptions): Compiler = {
    new Compiler(options)
  }
}

class Compiler(options: CompilerOptions) {

  lazy val compiler = new ClosureCompiler

  /**
   * Concatenate source files to target and generate a compiled .min.js file.
   *
   * @param manifest Manifest file
   * @param target Destination for concatenated source files
   * @param log The logger
   */
  def compile(manifest: Manifest, target: File, log: Logger): Unit = {

    // Create file parent directory.
    IO.createDirectory(file(target.getParent))

    // Concatenate file list into target.
    IO.write(target, (manifest.sources.map(_.file).foldLeft(Array[Byte]()))((content, source) => {
      content ++ IO.readBytes(source)
    }))

    // Compile concatenated JS (we do not handle externs)
    val result = compiler.compile(
      Array[JSSourceFile](),
      Array[JSSourceFile](JSSourceFile.fromFile(target)),
      options
    )

    val errors = result.errors.toList
    val warnings = result.warnings.toList

    if (!errors.isEmpty) {
      errors.foreach { (err: JSError) => log.error(err.toString) }
    }
    else {
      if (!warnings.isEmpty) {
        warnings.foreach { (err: JSError) => log.warn(err.toString) }
      }

      // Write compiled output to minified JS file.
      IO.write(
        new File(target.getCanonicalPath.replaceAll("""\.js$""", ".min.js")),
        compiler.toSource
      )
    }
  }
}
