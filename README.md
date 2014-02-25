# sbt-jsmanifest

[Simple Build Tool](http://www.scala-sbt.org/ "simple build tool") plugin for compiling JavaScript files from multiple sources using Google's Closure Compiler.

## Settings

## Installation

If you have not already added the sbt community plugin resolver to your plugin definition file, add this
Add the following to your sbt `project/plugins.sbt` file:

```scala
resolvers += Resolver.url(
  "bintray-rts-sbt-plugins",
    url("http://dl.bintray.com/content/rts/sbt-plugins"))(
        Resolver.ivyStylePatterns)

addSbtPlugin("ch.srg" % "sbt-jsmanifest" % "0.0.1")
```

Then in your build definition, add

```scala
seq(jsManifestSettings:_*)
```

This will append `sbt-jsmanifest`'s settings for the `Compile` and `Test` configurations.

To add them to other configurations, use the provided `jsManifestSettingsIn(config)` method.

```scala
seq(jsManifestSettingsIn(SomeOtherConfig):_*)
```

## Usage

The plugin scans your `src/main/javascript` directory
and looks for files of extension `.jsm`. These files
should contain ordered lists of JavaScript source locations. For example:

    # You can specify remote files using URLs...
    http://code.jquery.com/jquery-1.5.1.js

    # ...and local files using regular paths
    #    (relative to the location of the manifest):
    lib/foo.js
    bar.js
    ../bar.js

    # Blank lines and bash-style comments are ignored.

The plugin compiles this in three phases:
1. It downloads and caches any remote script
2. It generates a combined javascript file and outputs it to `path/to/resource_managed/main/js` (same filename but with `.js` extension)
3. Then it passes the combined javascript file to the Closure compiler and generates the file with `.min.js` extension

For example, if your manifest
file is at `src/main/javascript/foo.jsm` in the source tree, the combined file would
be `resource_managed/main/js/foo.js` and the compiled one `resource_managed/main/js/foo.min.js`
in the source tree.

If, on compilation, the plugin finds remote scripts already cached on your
filesystem, it won't try to download them again. Running `sbt clean` will
delete the cache.

## Tasks

The plugin is tied to the compile task and will run whenver compile is run. You can
also run `closure` to run it on its own. `clean(for closure)` will delete the generated files.

## Customization

If you're using [xsbt-web-plugin](https://github.com/JamesEarlDouglas/xsbt-web-plugin "xsbt-web-plugin"), add the output files to the webapp with:

    // add managed resources to the webapp
    (webappResources in Compile) <+= (resourceManaged in Compile)

### Changing the directory that is scanned, use:

    (sourceDirectory in (Compile, JsManifestKeys.closure)) <<= (sourceDirectory in Compile)(_ / "path" / "to" / "jsmfiles")

### Changing target js destination:

To change the default location of compiled js files, add the following to your build definition

    (resourceManaged in (Compile, JsManifestKeys.closure)) <<= (resourceManaged in Compile)(_ / "your_preference" / "js")

### Example sbt configuration for [Play Framework](http://www.playframework.com/)

```scala
// Enables sbt-jsmanifest.
jsManifestSettings

// Custom settings.
Seq(
  // Indicate the plugin to scan the Play javascript assets directory:
  (sourceDirectory in (Compile, JsManifestKeys.jsManifest)) <<= (sourceDirectory in Compile)(_ / "assets" / "javascripts"),
  // Put output files in the same place as Play does:
  (resourceManaged in (Compile, JsManifestKeys.jsManifest)) <<= (resourceManaged in Compile)(_ / "public" / "javascripts"),
  // Make sure the plugins are called by the compile command and
  // the continuous recompilation in the run command:
  resourceGenerators in Compile <+= (JsManifestKeys.jsManifest in Compile)
)
```

## File versioning

The plugin has a setting for a file suffix that is appended to the output file name before the file extension.
This allows you to update the version whenever you make changes to your Javascript files. Useful when you are
caching your js files in production. To use, add the following to your build.sbt:

    (JsManifestKeys.suffix in (Compile, JsManifestKeys.closure)) := "4"

Then if you have manifest file `src/main/javascript/script.jsm` it will be output as
`resource_managed/src/main/js/script-4.js`

This is only half of the puzzle, though. In order to know what that suffix is in your code,
you can use the [sbt-buildinfo](https://github.com/sbt/sbt-buildinfo) plugin. Add the plugin
to your project, then add the following to your build.sbt:

    seq(buildInfoSettings: _*)

    buildInfoPackage := "mypackage"

    buildInfoKeys := Seq[Scoped](JsManifestKeys.suffix in (Compile, JsManifestKeys.closure))

    sourceGenerators in Compile <+= buildInfo

This will generate a Scala file with your suffix in `src_managed/main/BuildInfo.scala` and
you can access it in your code like this:

    mypackage.BuildInfo.closure_suffix

In my Lift project I have the following snippet:

    package mypackage
    package snippet

    import net.liftweb._
    import util.Helpers._

    object JavaScript {
      def render = "* [src]" #> "/js/script-%s.js".format(BuildInfo.closure_suffix)
    }

Which is called in my template like:

    <script lift="JavaScript"></script>

## Acknowledgements

This plugin has been forked from the [sbt-closure](https://github.com/eltimn/sbt-closure) plugin.
