import sbt._

object Version {
  final val akka = "2.5.3"
  final val akkaHttp = "10.0.8"
  final val akkaHttpJson4s = "1.17.0"
  final val akkaJwt = "1.1.1"
  final val Scala = "2.11.8"
  final val AkkaLog4j = "1.4.0"
  final val Log4j = "2.8.2"
  final val swagger = "1.5.14"
  final val swaggerAkka = "0.9.2"
  final val akkaHttpCors = "0.2.1"
  final val slickRepo = "1.4.3"
  final val postgresql = "42.2.0"
  final val slick = "3.2.1"
  final val slickPg = "0.15.5"
  final val flyway = "3.2.1"
  final val http4s = "0.15.16a"
  final val kamon = "0.6.1"
}

object Library {

  val swagger = "io.swagger" % "swagger-jaxrs" % Version.swagger
  val swaggerAkka = "com.github.swagger-akka-http" %% "swagger-akka-http" % Version.swaggerAkka

  val akkaHttp = "com.typesafe.akka" %% "akka-http" % Version.akkaHttp
  val akkaActor = "com.typesafe.akka" %% "akka-actor" % Version.akka
  val akkaStream = "com.typesafe.akka" %% "akka-stream" % Version.akka
  val akkaHttpCors = "ch.megard" %% "akka-http-cors" % Version.akkaHttpCors
  val akkaHttpJson4s =   "de.heikoseeberger" %% "akka-http-json4s" % Version.akkaHttpJson4s
  val akkaJwt = "de.innfactory" %% "akka-jwt" % Version.akkaJwt
  val akkaHttpCaching = "com.typesafe.akka" %% "akka-http-caching" % "10.1.0-RC1"


  val log4jCore = "org.apache.logging.log4j" % "log4j-core" % Version.Log4j
  val slf4jLog4jBridge = "org.apache.logging.log4j" % "log4j-slf4j-impl" % Version.Log4j
  val akkaLog4j = "de.heikoseeberger" %% "akka-log4j" % Version.AkkaLog4j

  val slick = "com.typesafe.slick" %% "slick" % Version.slick
  val slickHikaricp = "com.typesafe.slick" %% "slick-hikaricp" % Version.slick
  val postgresql =  "org.postgresql" % "postgresql" % Version.postgresql
  val slickPg = "com.github.tminglei" %% "slick-pg" % Version.slickPg
  val slickPgJson4s = "com.github.tminglei" %% "slick-pg_json4s" % Version.slickPg

  val jodaTime = "joda-time" % "joda-time" % "2.9.4"
  val jodaConvert = "org.joda" % "joda-convert" % "1.8"

  val scalate = "org.scalatra.scalate" %% "scalate-core" % "1.8.0"

  val sttp = "com.softwaremill.sttp" %% "core" % "1.1.3"
  val akkaHttpBackend = "com.softwaremill.sttp" %% "akka-http-backend" % "1.1.3"
  val sttpJson4s = "com.softwaremill.sttp" %% "json4s" % "1.1.3"
  val enumeratumJson4s = "com.beachape" %% "enumeratum-json4s" % "1.5.13"
  val json4sExt = "org.json4s" %% "json4s-ext" % "3.5.3"

  val kamonCore = "io.kamon" %% "kamon-core" % Version.kamon
  val kamonAkka = "io.kamon" %% "kamon-akka" % Version.kamon
  val kamonStadsd = "io.kamon" %% "kamon-statsd" % Version.kamon
  val kamonLogReporter = "io.kamon" %% "kamon-log-reporter" % Version.kamon
  val kamonSystemMetrics = "io.kamon" %% "kamon-system-metrics" % Version.kamon
  val aspectjWeaver = "org.aspectj" % "aspectjweaver" % "1.8.9"
}

object TestVersion {
  final val akkaTestkit = "2.5.3"
  final val akkaHttpTestkit =  "10.0.9"
  final val postgresqlEmbedded = "2.2"
  final val scalaTest = "3.0.1"
}

object TestLibrary {
  val akkaTestkit = "com.typesafe.akka" %% "akka-testkit" % TestVersion.akkaTestkit % "test"
  val akkaHttpTestkit = "com.typesafe.akka" %% "akka-http-testkit" % TestVersion.akkaHttpTestkit % "test"
  val postgresqlEmbedded = "ru.yandex.qatools.embed" % "postgresql-embedded" % TestVersion.postgresqlEmbedded % "test"
  val scalaTest = "org.scalatest" %% "scalatest" % TestVersion.scalaTest % "test"
  val mockito = "org.mockito" % "mockito-core" % "1.9.5"
  val specs2Core = "org.specs2"          %%  "specs2-core"   % "2.3.11" % "test"
  val specs2Mock = "org.specs2"          %%  "specs2-mock"   % "2.3.11"
}

