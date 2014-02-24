seq(closureSettings:_*)

TaskKey[Unit]("checkCombined") <<= (baseDirectory, resourceManaged) map { (baseDirectory, resourceManaged) =>
  val fixture = sbt.IO.read(baseDirectory / "fixtures" / "script.js")
  val out = sbt.IO.read(resourceManaged / "main" / "js" / "script.js")
  if (out != fixture) sys.error("Combined JS is not the same as fixture: \n\n" + out + "\n\n" + fixture)
}

TaskKey[Unit]("checkCompiled") <<= (baseDirectory, resourceManaged) map { (baseDirectory, resourceManaged) =>
  val fixture = sbt.IO.read(baseDirectory / "fixtures" / "script.min.js")
  val out = sbt.IO.read(resourceManaged / "main" / "js" / "script.min.js")
  if (out.trim != fixture.trim) sys.error("Compiled JS is not the same as fixture: \n\n" + out + "\n\n" + fixture)
}
