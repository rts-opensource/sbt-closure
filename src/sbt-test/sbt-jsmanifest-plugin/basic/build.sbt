lazy val root = (project in file(".")).enablePlugins(SbtWeb)

val checkCombined = taskKey[Unit]("check that manifest is combined")

checkCombined := {
  val fixture = IO.read(baseDirectory.value / "fixtures" / "script.js")
  val out = IO.read((WebKeys.public in Assets).value / "javascript" / "script.js")
  if (out != fixture) sys.error("Combined JS is not the same as fixture: \n\n" + out + "\n\n" + fixture)
}
