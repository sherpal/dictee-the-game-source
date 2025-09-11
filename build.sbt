import java.nio.charset.StandardCharsets

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.7.2"

Global / onLoad := {
  val scalaVersionValue = (root / scalaVersion).value
  val outputFile        = baseDirectory.value / "scala-metadata.js"
  IO.writeLines(
    outputFile,
    s"""
       |const scalaVersion = "$scalaVersionValue"
       |
       |export let scalaMetadata = {
       |  scalaVersion: scalaVersion
       |}
       |""".stripMargin.split("\n").toList,
    StandardCharsets.UTF_8
  )

  (Global / onLoad).value
}

val laminarVersion = "17.0.0"
val indigoVersion  = "0.22.0"

lazy val root = (project in file("."))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    scalacOptions ++= Seq(
      "-encoding",
      "utf8",
      "-Xfatal-warnings",
      "-deprecation",
      "-unchecked",
      "-language:higherKinds",
      "-feature",
      "-language:implicitConversions"
    ),
    name := "dictee-the-game",
    libraryDependencies ++= Seq(
      "io.indigoengine" %%% "indigo"             % indigoVersion,
      "io.indigoengine" %%% "indigo-extras"      % indigoVersion,
      "io.indigoengine" %%% "indigo-json-circe"  % indigoVersion,
      "com.raquo"       %%% "laminar"            % laminarVersion,
      "be.doeraene"     %%% "url-dsl"            % "0.7.0",
      "be.doeraene"     %%% "web-components-ui5" % "2.12.1"
    ),
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.ESModule) },
    scalaJSUseMainModuleInitializer := true
  )
