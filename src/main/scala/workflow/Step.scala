package workflow

import akka.actor.{ActorRef, Actor, ActorLogging}
import models.StepResult
import persistence.entities.Flow
import utils.{ServiceModule, Configuration}
import workflow.StepProtocol.{Error, Work}
import workflow.WorkflowProtocol.UpdateWorkflowStep

import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  *  After the GenericActor completes its work it sends a message to the WorkflowActor
  *  to update the workflow step to "COMPLETE" for the given Flow
  */

object StepProtocol {
  case object Work
  case class Error(throwable: Throwable)
}

abstract class Step(parent: ActorRef, flow: Flow, stepName: String, modules: Configuration with ServiceModule) extends Actor with ActorLogging {

  import context.dispatcher

  def execute(work: Any): Future[StepResult]
  def shouldExecute: Boolean

  override def receive: Receive = {
    case Work =>
      val startTime = System.nanoTime()
      shouldExecute match {
        case true =>
          log.debug(s"...${self.path.name}: [${flow.organizationId}] Executed")
          execute() onComplete {
            case Success(succ) => sendAndShutdown(UpdateWorkflowStep(flow, succ.copy(executionTime = getExecutionTime(startTime))))
            case Failure(e) => self ! Error(e)
          }
        case false =>
          log.debug(s"...${self.path.name}: [${flow.organizationId}] Skipped")
          sendAndShutdown(UpdateWorkflowStep(flow, StepResult(stepName, Some("SKIPPED"))))
      }

    case Error(e) => throw e
  }

  def sendAndShutdown(updateWorkflowStep: UpdateWorkflowStep) = {
    parent ! updateWorkflowStep
    context.stop(self)
  }

  def getExecutionTime(startTime: Long): Long = {
    System.nanoTime() - startTime
  }

  override def preStart { log.debug(s"Step ${self.path.name} started") }
  override def postStop { log.debug(s"Step ${self.path.name} stopped") }

}