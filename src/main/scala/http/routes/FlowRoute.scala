package http.routes

import javax.ws.rs.Path

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import de.innfactory.akka.{AuthService, JwtAuthDirectives}
import io.swagger.annotations._
import org.json4s.{DefaultFormats, native}
import persistence.entities.Flow
import utils._

@Path("flow")
@Api(value = "/flow", produces = "application/json")
class FlowRoute(val modules: Configuration with PersistenceModule with OrchestratorModule with ServiceModule with ActorModule) extends JwtAuthDirectives {

  import Directives._
  import Json4sSupport._

  implicit val serialization = native.Serialization // or native.Serialization
  implicit val formats       = DefaultFormats

  val authService: AuthService = modules.authService
  //val flowOrchestratorActor = modules.system.actorOf(Props(new FlowOrchestratorActor(modules)))


  val route = pathPrefix("flow") {
    /*routeGetAll ~ */routeProcessFlow ~ routeResumeFlow ~ routeDeleteAll
  }


/*  @ApiOperation(value = "Get all flows", notes = "", nickname = "getFlows", httpMethod = "GET")
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Array of Flows", response = classOf[Array[Flow]]),
    new ApiResponse(code = 403, message = "Auth failed"),
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  def routeGetAll = pathEndOrSingleSlash {
    get {
      complete(modules.flowOrchestrator.getAll.map(_.asJson))
    }
  }*/

/*  @Path("/{flowId}")
  @ApiOperation(value = "Get one flow", notes = "", nickname = "getFlow", httpMethod = "GET")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "flowId", value = "flow id", required = true, dataType = "integer", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Flow", response = classOf[Flow]),
    new ApiResponse(code = 403, message = "Auth failed"),
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  def routeGetOne =
    get {
      path(IntNumber) { flowId =>
        authenticate { u =>
          complete {
            modules.flowOrchestrator.getOne(flowId).map(_.asJson)
          }
        }
      }
    }*/


  @ApiOperation(value = "process flow", notes = "", nickname = "processFlow", httpMethod = "POST")
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "processed Flow", response = classOf[Flow]),
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  def routeProcessFlow = pathEndOrSingleSlash {
    post {
      entity(as[Flow]) { flow =>
        modules.flowOrchestrator.processFlow(flow)
        //flowOrchestratorActor ! ProcessFlow(flow)
        complete(StatusCodes.Accepted)
      }
    }
  }

  @ApiOperation(value = "resume flow", notes = "", nickname = "resumeFlow", httpMethod = "GET")
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "resumed Flow", response = classOf[Flow]),
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  def routeResumeFlow =
    get {
      path(LongNumber) { flowId =>
        complete {
          modules.flowOrchestrator.resumeFlow(flowId)
          //flowOrchestratorActor ! ResumeFlow(flowId)
          StatusCodes.Accepted
        }
      }
    }

  @Path("/clearOldScenario")
  @ApiOperation(value = "empty all database", notes = "", nickname = "deleteAll", httpMethod = "GET")
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "emptied database", response = classOf[Unit]),
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  def routeDeleteAll =
    get {
      path("clearOldScenario") {
        complete {
          modules.flowOrchestrator.deleteAll()
          //flowOrchestratorActor ! ClearOldScenario
          StatusCodes.Accepted
        }
      }
    }

}
