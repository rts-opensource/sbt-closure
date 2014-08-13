addSbtPlugin("ch.srg" % "sbt-jsmanifest" % sys.props("project.version"))

resolvers ++= Seq(
  Resolver.mavenLocal,
  Resolver.url("sbt snapshot plugins", url("http://repo.scala-sbt.org/scalasbt/sbt-plugin-snapshots"))(Resolver.ivyStylePatterns),
  Resolver.sonatypeRepo("snapshots"),
  "Typesafe Snapshots Repository" at "http://repo.typesafe.com/typesafe/snapshots/"
)

addSbtPlugin("com.typesafe.sbt" %% "sbt-web" % "1.0.2")
