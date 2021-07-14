// *****************************************************************************
// Build settings
// *****************************************************************************

inThisBuild(
  Seq(
    organization := "com.owlandrews",
    organizationName := "owlandrews",
    version := "0.0.1",
    startYear := Some(2021),
    licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0")),
    scalaVersion := "2.12.10",
    scalacOptions ++= Seq(
      "-unchecked",
      "-deprecation",
      "-language:_",
      "-target:jvm-1.8",
      "-encoding",
      "UTF-8",
      "-Ypartial-unification",
      "-Ywarn-unused-import"
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
      val munit        = "0.7.26"
      val cats         = "2.6.1"
      val catsEffect   = "2.1.4"
      val decline      = "1.3.0"
      val imageio      = "3.7.0"
      val logback      = "1.2.3"
      val scalaLogging = "3.9.3"
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
