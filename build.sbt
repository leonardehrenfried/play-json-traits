import ReleaseTransformations._

val scalacOpts = Seq(
  "-target:jvm-1.8",
  "-encoding",
  "UTF-8",
  "-deprecation", // warning and location for usages of deprecated APIs
  "-feature", // warning and location for usages of features that should be imported explicitly
  "-language:postfixOps",
  "-unchecked", // additional warnings where generated code depends on assumptions
  "-Xlint", // recommended additional warnings
  "-Xcheckinit", // runtime error when a val is not initialized due to trait hierarchies (instead of NPE somewhere else)
  "-Xfatal-warnings", // all warnings become errors
  "-Ywarn-adapted-args", // Warn if an argument list is modified to match the receiver
  "-Ywarn-value-discard", // Warn when non-Unit expression results are unused
  "-Ywarn-inaccessible",
  "-Ywarn-dead-code",
  "-Ywarn-unused"
  //"-Ywarn-unused-import"
)


val commonSettings = Seq(
  scalaVersion := "2.11.11",
  crossScalaVersions := Seq("2.11.11", "2.12.2"),
  organization := "io.leonard",
  scalacOptions ++= scalacOpts
)

releaseCrossBuild := true

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  ReleaseStep(action = "publishSigned" :: _, enableCrossBuild = true),
  setNextVersion,
  commitNextVersion,
  ReleaseStep(action = "sonatypeReleaseAll" :: _, enableCrossBuild = true),
  pushChanges
)

lazy val `play-json-traits` = project
  .in(file("."))
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.play" %% "play-json"   % "2.6.2",
      "org.joda"          % "joda-convert" % "1.8.2" % "provided",
      "org.scalatest"     %% "scalatest"   % "3.0.3" % "test"
    )
  )
