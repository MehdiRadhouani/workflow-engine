import java.sql.Timestamp

import akka.http.scaladsl.Http
import akka.http.scaladsl.server.RouteConcatenation
import akka.stream.ActorMaterializer
import http.HttpService
import kamon.Kamon
import org.joda.time.DateTime
import utils._

object Main extends App {

  Kamon.start()

  // configuring modules for application, cake pattern for DI
  val modules = new ConfigurationModuleImpl
                with ActorModuleImpl
                with PersistenceModuleImpl
                with OrchestratorModuleImpl
                with ServiceModuleImpl
  implicit val system = modules.system
  implicit val ec = modules.system.dispatcher
  implicit val materializer = ActorMaterializer()

  modules.generateDDL()
  modules.deleteAll()

  println("Processors "+Runtime.getRuntime().availableProcessors())

  val bindingFuture = Http().bindAndHandle(new HttpService(modules).routes, modules.httpHost, modules.httpPort)

  println(s"Server online at http://localhost:9090/")

}