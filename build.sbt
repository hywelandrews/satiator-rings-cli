// *****************************************************************************
// Build settings
// *****************************************************************************

inThisBuild(
  Seq(
    organization := "com.owlandrews",
    organizationName := "owlandrews",
    version := "0.0.3",
    startYear := Some(2021),
    licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0")),
    scalaVersion := "2.13.10",
    scalacOptions ++= Seq(
      "-unchecked",
      "-deprecation",
      "-language:_",
      "-encoding",
      "UTF-8",
      "-Xlint"
    ),
    testFrameworks += new TestFramework("munit.Framework"),
    scalafmtOnCompile := true,
    dynverSeparator := "_", // the default `+` is not compatible with docker tags,
  )
)

// *****************************************************************************
// Projects
// *****************************************************************************

lazy val `satiator-rings-cli` =
  project
    .in(file("."))
    .settings(assembly / assemblyJarName := "satiator-rings-cli-assembly.jar")
    .settings(commonSettings)
    .settings(
      libraryDependencies ++= Seq(
        library.catsCore,
        library.catsEffect,
        library.decline,
        library.declineEffect,
        library.imageio,
        library.logback,
        library.scalaLogging,
        library.munit           % Test,
        library.munitScalaCheck % Test,
      ),
    )

// *****************************************************************************
// Project settings
// *****************************************************************************

lazy val commonSettings =
  Seq(
    // Also (automatically) format build definition together with sources
    Compile / scalafmt := {
      val _ = (Compile / scalafmtSbt).value
      (Compile / scalafmt).value
    },
  )

// *****************************************************************************
// Library dependencies
// *****************************************************************************

lazy val library =
  new {
    object Version {
      val munit        = "0.7.29"
      val cats         = "2.9.0"
      val catsEffect   = "3.4.4"
      val decline      = "2.4.1"
      val imageio      = "3.9.4"
      val logback      = "1.4.5"
      val slf4j        = "2.0.5"
      val scalaLogging = "3.9.5"
    }
    val munit           = "org.scalameta"              %% "munit"            % Version.munit
    val munitScalaCheck = "org.scalameta"              %% "munit-scalacheck" % Version.munit
    val catsCore        = "org.typelevel"              %% "cats-core"        % Version.cats
    val catsEffect      = "org.typelevel"              %% "cats-effect"      % Version.catsEffect
    val decline         = "com.monovore"               %% "decline"          % Version.decline
    val declineEffect   = "com.monovore"               %% "decline-effect"   % Version.decline
    val imageio         = "com.twelvemonkeys.imageio"   % "imageio-tga"      % Version.imageio
    val logback         = "ch.qos.logback"              % "logback-classic"  % Version.logback
    val scalaLogging    = "com.typesafe.scala-logging" %% "scala-logging"    % Version.scalaLogging
  }

ThisBuild / assemblyMergeStrategy := {
  case PathList("module-info.class")         => MergeStrategy.discard
  case x if x.endsWith("/module-info.class") => MergeStrategy.discard
  case x =>
    val oldStrategy = (ThisBuild / assemblyMergeStrategy).value
    oldStrategy(x)
}
