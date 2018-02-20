package http

import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.server.Directives._
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings
import http.routes.{FlowRoute, SwaggerDocRoute, SwaggerUIRoute}
import utils._
import scala.concurrent.ExecutionContext

class HttpService(val modules: Configuration with PersistenceModule with OrchestratorModule with ServiceModule with ActorModule)(implicit ec: ExecutionContext) {

  val flowRouter = new FlowRoute(modules)
  val swaggerRouter = new SwaggerUIRoute()
  val swaggerDocService = new SwaggerDocRoute(modules.httpHost, modules.httpPort, modules.system)

  val settings = CorsSettings.defaultSettings.copy(allowedMethods = List(GET, POST, PUT, HEAD, OPTIONS, DELETE))

  val routes =
    pathPrefix("v1") {
      cors(settings) {
        swaggerDocService.routes ~
        swaggerRouter.route ~
        flowRouter.route
      }
    }

}
