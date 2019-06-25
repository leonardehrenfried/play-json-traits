import ReleaseTransformations._
import xerial.sbt.Sonatype._

def scalacOpts(scalaVersion: String) = Seq(
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
) ++
  (CrossVersion.partialVersion(scalaVersion) match {
    case Some((2, 11)) => Seq(
      "-Ywarn-adapted-args", // Warn if an argument list is modified to match the receiver
      "-Ywarn-value-discard", // Warn when non-Unit expression results are unused
      "-Ywarn-inaccessible",
      "-Ywarn-dead-code",
      "-Ywarn-unused"
    )
    case _ => Seq(
      "-Xlint:adapted-args", // Warn if an argument list is modified to match the receiver
      "-Ywarn-value-discard", // Warn when non-Unit expression results are unused
      "-Xlint:inaccessible",
      "-Ywarn-dead-code",
      "-Ywarn-unused:_"
    )
  })


val scala213 = "2.13.0"
val scala212 = "2.12.8"

val commonSettings = Seq(
  scalaVersion := scala212,
  crossScalaVersions := Seq(scala213, scala212),
  organization := "io.leonard",
  scalacOptions ++= scalacOpts(scalaVersion.value),
  sonatypeProjectHosting := Some(GithubHosting("leonardehrenfried", "play-json-traits", "mail@leonard.io")),
  licenses := Seq("Apache2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))
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

publishTo := Some(
  if (isSnapshot.value)
    Opts.resolver.sonatypeSnapshots
  else
    Opts.resolver.sonatypeStaging
)

lazy val `play-json-traits` = project
  .in(file("."))
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.play" %% "play-json"   % "2.7.3",
      "org.joda"          % "joda-convert" % "1.8.2" % "provided",
      "org.scalatest"     %% "scalatest"   % "3.0.8" % "test"
    )
  )
