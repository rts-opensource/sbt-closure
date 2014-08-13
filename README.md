# sbt-jsmanifest

[Simple Build Tool](http://www.scala-sbt.org/ "simple build tool") plugin to compile JavaScript manifest sources to JavaScript.

To use this plugin use the addSbtPlugin command within your project's `plugins.sbt` file:

    addSbtPlugin("ch.srg" % "sbt-jsmanifest" % "1.0.0")

Your project's build file also needs to enable [sbt-web](https://github.com/sbt/sbt-web) plugins. For example with build.sbt:

    lazy val root = (project in file(".")).enablePlugins(SbtWeb)

If you have not already added the rts bintray plugin resolver to your plugin definition file, 
add the following to your sbt `project/plugins.sbt` file:

```scala
resolvers += Resolver.url(
  "bintray-rts-sbt-plugins",
    url("http://dl.bintray.com/content/rts/sbt-plugins"))(
        Resolver.ivyStylePatterns)
```

Once configured, any `*.jsm` or `*.jsmanifest` files placed in `src/main/assets` will be compiled to JavaScript code in `target/web/public`.

Supported settings:

* `charset` Defines which charset to use when reading manifest files. Defaults to `UTF-8`.

  `JsManifestKeys.charset := Charset.forName("latin1")`

## Manifest files

A javascript manifest file is a text file containing an ordered list of JavaScript source locations to combine in
one JavaScript source file.
For example:

    # You can specify remote files using URLs...
    http://code.jquery.com/jquery-1.5.1.js

    # ...and local files using regular paths
    #    (relative to the location of the manifest):
    lib/foo.js
    bar.js
    ../bar.js

    # Blank lines and bash-style comments are ignored.

The plugin compiles the manifest in two phases:
1. It downloads and caches any remote script
2. It generates a combined javascript file and outputs it to `target/web/public` (same filename but with `.js` extension)

For example, if your manifest
file is at `src/main/assets/javascript/foo.jsm` in the source tree, the combined file would
be `target/web/public/javascript/foo.js` in the source tree.

If, on compilation, the plugin finds remote scripts already cached on your
filesystem, it won't try to download them again. Running `sbt clean` will
delete the cache.
