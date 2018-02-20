package service.salesforce

import java.text.SimpleDateFormat

import akka.http.caching.LfuCache
import akka.http.caching.scaladsl.{Cache, CachingSettings}
import com.softwaremill.sttp._
import com.softwaremill.sttp.akkahttp.AkkaHttpBackend
import com.softwaremill.sttp.json4s._
import logging.AppLogger
import org.json4s.DefaultFormats
import utils.{ActorModule, Configuration}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

trait SalesforceService {
  def getCase(caseId: String): Future[Case]
}

class SalesforceServiceImpl(val modules: Configuration with ActorModule)(implicit ec: ExecutionContext) extends SalesforceService with AppLogger {

  implicit def formats = customDateFormat

  def customDateFormat = new DefaultFormats {
    override def dateFormatter = new SimpleDateFormat("yyyy-MM-dd")
  }

  implicit val backend = new CustomHttpBackend(
    AkkaHttpBackend.usingActorSystem(modules.system),
    shouldRetry,
    1,
    new CloudMetricsServer()
  )

  val defaultCachingSettings = CachingSettings(modules.system)
  val lfuCacheSettings = defaultCachingSettings.lfuCacheSettings
    .withInitialCapacity(1)
    .withMaxCapacity(1)
    .withTimeToLive(200 seconds)
  val cachingSettings = defaultCachingSettings.withLfuCacheSettings(lfuCacheSettings)
  val loginCache: Cache[String, LoginResponse] = LfuCache(cachingSettings)


  private def cachedToken: Future[LoginResponse] = {
    loginCache("cached", () => login)
  }

  private def login: Future[LoginResponse] = {
    val queryParams = Map(
      "grant_type" -> "password",
      "client_id" -> modules.salesforceSettings.clientId,
      "client_secret" -> modules.salesforceSettings.clientSecret,
      "username" -> modules.salesforceSettings.username,
      "password" -> modules.salesforceSettings.password
    )

    sttp
      .post(uri"https://test.salesforce.com/services/oauth2/token?$queryParams")
      .response(asJson[LoginResponse])
      .send().map(_.unsafeBody)
  }

  def shouldRetry = (request: Request[_, _], response: Either[Throwable, Response[_]]) => {
    response match {
      case Left(l) => true
      case Right(r) if r.toString.contains("Session expired") =>
        loginCache.clear()
        true
      case Right(r) => false
    }
  }

  override def getCase(caseId: String): Future[Case] = {
    val queryParams = Map("fields" -> "CaseNumber")
    val endpoint = s"/services/data/v32.0/sobjects/Case/$caseId?$queryParams"

    performAuthenticatedRequest(doGet[Case](endpoint))
  }

  def doGet[T](endpoint: String)(loginResponse: LoginResponse)(implicit m: Manifest[T]): Future[Response[T]] = {
    sttp.get(uri"${loginResponse.instance_url}$endpoint")
      .auth.bearer(loginResponse.access_token)
      .response(asJson[T])
      .send()
  }

  def performAuthenticatedRequest[T](getResponse: (LoginResponse) => Future[Response[T]]): Future[T] = {
    for {
      loginResponse <- cachedToken
      response <- getResponse(loginResponse)
    } yield {
      response.unsafeBody
    }
  }
}

trait MetricsServer {
  def reportDuration(request: String, response: String, duration: Long): Unit
}

class CloudMetricsServer extends MetricsServer {
    override def reportDuration(request: String, response: String, duration: Long): Unit = {
      println("======== REQUEST ========")
      println(request)
      println("======== REQUEST ========")
      println("======== RESPONSE ======== : " + duration)
      println(response)
      println("======== RESPONSE ========")

    }
}

class CustomHttpBackend[R[_], S](delegate: SttpBackend[R, S],
                                 shouldRetry: (Request[_, _], Either[Throwable, Response[_]]) => Boolean,
                                 maxRetries: Int,
                                 metrics: MetricsServer) extends SttpBackend[R, S] {

  implicit val jsonFormats = DefaultFormats

  override def send[T](request: Request[T, S]): R[Response[T]] = {
    sendWithRetryCounter(request, 0)
  }

  private def sendWithRetryCounter[T](request: Request[T, S], retries: Int): R[Response[T]] = {
    val start = System.currentTimeMillis()

    def report(response: Either[Throwable, Response[T]], status: String): Unit = {
      val metricPrefix = request
      val end = System.currentTimeMillis()
      metrics.reportDuration(metricPrefix.toString, response.toString, end - start)
    }

    val r = responseMonad.handleError(delegate.send(request)) {
      case t if shouldRetry(request, Left(t)) && retries < maxRetries =>
        report(Left(t), "exception")
        sendWithRetryCounter(request, retries + 1)
    }

    responseMonad.flatMap(r) { resp =>
      if (shouldRetry(request, Right(resp)) && retries < maxRetries) {
        report(Right(resp), "notok")
        sendWithRetryCounter(request, retries + 1)
      } else {
        report(Right(resp), "ok")
        responseMonad.unit(resp)
      }
    }
  }

  override def close(): Unit = delegate.close()

  override def responseMonad: MonadError[R] = delegate.responseMonad
}