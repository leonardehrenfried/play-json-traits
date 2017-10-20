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


val scala212 = "2.12.3"
val scala211 = "2.11.11"

val commonSettings = Seq(
  scalaVersion := scala212,
  crossScalaVersions := Seq(scala212, scala211),
  organization := "io.leonard",
  scalacOptions ++= scalacOpts
)

import ReleaseTransformations._

releaseCrossBuild := true

releasePublishArtifactsAction := PgpKeys.publishSigned.value // Use publishSigned in publishArtifacts step

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  publishArtifacts,
  setNextVersion,
  commitNextVersion,
  releaseStepCommand("sonatypeReleaseAll"),
  pushChanges
)

lazy val `play-json-traits` = project
  .in(file("."))
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.play" %% "play-json"   % "2.6.3",
      "org.joda"          % "joda-convert" % "1.8.2" % "provided",
      "org.scalatest"     %% "scalatest"   % "3.0.4" % "test"
    )
  )
