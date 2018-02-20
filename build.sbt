import com.typesafe.sbt.SbtAspectj._

name := "workflow-engine"
organization := "vandebron"
version := "1.0.0"
scalaVersion := Version.Scala

libraryDependencies ++= {
  Seq(
    Library.swagger,
    Library.swaggerAkka,
    Library.akkaActor,
    Library.akkaHttp,
    Library.akkaHttpCors,
    Library.akkaHttpJson4s,
    Library.akkaStream,
    Library.akkaJwt,
    Library.log4jCore,
    Library.slf4jLog4jBridge,
    Library.akkaLog4j,
    Library.slick,
    Library.slickHikaricp,
    Library.postgresql,
    Library.slickPg,
    Library.slickPgJson4s,
    Library.jodaTime,
    Library.jodaConvert,
    Library.scalate,
    Library.akkaHttpCaching,
    Library.sttp,
    Library.akkaHttpBackend,
    Library.sttpJson4s,
    Library.enumeratumJson4s,
    Library.json4sExt,
    Library.kamonCore,
    Library.kamonAkka,
    Library.kamonStadsd,
    Library.kamonLogReporter,
    Library.kamonSystemMetrics,
    Library.aspectjWeaver,
    TestLibrary.akkaTestkit,
    TestLibrary.akkaHttpTestkit,
    TestLibrary.postgresqlEmbedded,
    TestLibrary.scalaTest,
    TestLibrary.specs2Core,
    TestLibrary.specs2Mock
  )
}

Revolver.settings
enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)

dockerExposedPorts := Seq(9090)
dockerEntrypoint := Seq("bin/%s" format executableScriptName.value, "-Dconfig.resource=docker.conf")

aspectjSettings

javaOptions <++= AspectjKeys.weaverOptions in Aspectj

fork in run := true
