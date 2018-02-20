package workflow

import akka.actor.SupervisorStrategy.{Resume, Restart, Stop}
import akka.actor.{Actor, PoisonPill, Props, _}
import flow.definition.FlowDefinition
import models.{FlowNotFoundException, StepResult}
import persistence.entities.Flow
import utils._
import workflow.MasterWorkerProtocol._
import workflow.StepProtocol.Work
import workflow.WorkflowProtocol._

import scala.collection.mutable
import scala.concurrent.duration._
import scala.language.postfixOps

object WorkflowProtocol {
  case class ProcessFlowById(id: Long)
  case class RegisterWorkflowSteps(flow: Flow)
  case class StartWorkflow(flow: Flow)
  case class GetNextWorkflowStep(flow: Flow)
  case class StartNextWorkflowStep(flow: Flow, step: Class[_ <: Step], stepResult: StepResult)
  case class UpdateWorkflowStep(flow: Flow, updatedStepResult: StepResult)
  case class CompleteWorkflow(flow: Flow)
  case class WorkComplete(result: Any)
}

/**
 * The workflow actor is responsible for:
 *
 * a) Registering a workflow for a Flow, i.e. the workflow actor delegates to some component
 *    that understands what steps that a given Flow must go through.  Those steps together
 *    compose the "workflow" for that Flow.  And the workflow is saved to a database
 * b) Advancing the workflow for a given Flow
 * c) Maintaining the state of the workflow in the database for a given Flow as the Flow
 *    advances through the various steps of the workflow
 * d) Understanding the mapping between a step/job name
 *    and the actor that performs that job.
 *
 */

class Worker(master: ActorRef, val modules: Configuration with ServiceModule with ActorModule) extends Actor with ActorLogging {

  import context.dispatcher

  var startTime: Long = 0
  var flowId: Option[Long] = None
  var currentStepResult: Option[StepResult] = None

  // This is the state we're in when we're working on something.
  // In this state we can deal with messages in a much more
  // reasonable manner
  def working(work: Any): Receive = {
    case WorkToBeDone(_) =>
      log.error("Yikes. Master told me to do work, while I'm working.")
    // Our derivation has completed its task
    case p: ProcessFlowById =>
      log.debug(s"Process Flow with id: [${p.id}]")
      startTime = System.nanoTime()
      flowId = Some(p.id)
      modules.flowHandler.getFlow(p.id).map {
        case Some(flow) =>
          for {
            flowId <- modules.flowHandler.updateFlowStatusForFlow(flow, "STARTED")
          } yield {
            self ! GetNextWorkflowStep(flow)
          }
        case None => throw new FlowNotFoundException(-1L)
      }
    case GetNextWorkflowStep(flow) =>
      log.debug("GetNextWorkflowStep")
      modules.flowHandler.getNextFlowStep(flow, FlowDefinition.steps) map {
        case None => self ! CompleteWorkflow(flow)
        case Some(flowStep) => self ! StartNextWorkflowStep(flow, flowStep._1, flowStep._2)
      }
    case sn: StartNextWorkflowStep =>
      currentStepResult = Some(sn.stepResult)
      val stepName = sn.stepResult.stepName
      log.debug("StartNextWorkflowStep: {}", stepName)
      val stepClass = sn.step
      context.actorOf(Props(stepClass, self, sn.flow, stepName, modules), s"${sn.flow.id.get}-$stepName") ! Work
    case UpdateWorkflowStep(flow, updatedStepResult) =>
      log.debug("UpdateWorkflowStep: {} to status {}", updatedStepResult.stepName, updatedStepResult.status.get)
      for {
        flowStepId <- modules.flowHandler.updateFlowStep(flow, updatedStepResult)
      } yield {
        self ! GetNextWorkflowStep(flow)
      }
    case CompleteWorkflow(flow) =>
      log.debug("CompleteWorkflow")
      for {
        flowId <- modules.flowHandler.updateFlowStatusForFlow(flow, "COMPLETE", System.nanoTime() - startTime)
      } yield {
        log.info("Completed Flow {} for organization {}", flowId, flow.organizationId)
        self ! WorkComplete("Complete")
      }
    case WorkComplete(result) =>
      log.info("Work is complete. Result {}.", result)
      master ! WorkIsDone(self)
      master ! WorkerRequestsWork(self)
      // We're idle now
      context.become(idle)
    case _ =>
      log.error("Nothing happened")
  }

  // In this state we have no work to do.  There really are only
  // two messages that make sense while we're in this state, and
  // we deal with them specially here
  def idle: Receive = {
    // Master says there's work to be done, let's ask for it
    case WorkIsReady =>
      log.info("Requesting work")
      master ! WorkerRequestsWork(self)
    // Send the work off to the implementation
    case WorkToBeDone(work) =>
      log.info(s"{} Got work {}", self.path.name, work)
      self ! ProcessFlowById(work)
      context.become(working(work))
    // We asked for it, but either someone else got it first, or
    // there's literally no work to be done
    case NoWorkToBeDone =>
  }


  def receive = idle

  override def preStart {
    log.info(s"WorkflowActor: preStart")
    master ! WorkerCreated(self)
  }
  override def postStop { log.info("WorkflowActor: postStop") }

  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 30 seconds) {
      case e: Exception =>
        flowId match {
          case Some(someFlowId) =>
            for {
              flow <- modules.flowHandler.getFlow(someFlowId)
              _ <- modules.flowHandler.updateFlowStatusForFlow(flow.get, "ERROR", System.nanoTime() - startTime)
              if currentStepResult.isDefined
              _ <- modules.flowHandler.updateFlowStep(flow.get, currentStepResult.get.copy(status = Some("ERROR")))
            } yield {
              if (log.isInfoEnabled) {
                log.info("Error happened during the flow {}", e.getMessage)
              }
              if (log.isDebugEnabled) {
                log.debug("Error happened during the flow {}", e)
              }
              self ! WorkComplete("Error")
            }
            Stop

          case None => Stop
        }
    }
}