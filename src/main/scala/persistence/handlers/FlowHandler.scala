package persistence.handlers

import java.sql.Timestamp

import logging.AppLogger
import models.{FlowNotFoundException, StepResult}
import org.joda.time.DateTime
import persistence.entities.{CustomData, Flow, FlowStep}
import utils.{Configuration, PersistenceModule}
import workflow.Step

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by mehdiradhouani on 26/11/2017.
  */
trait FlowHandler {
  def getAll : Future[Seq[Flow]]
  def getOne(id : Long) : Future[Option[Flow]]
  def deleteAll : Unit
  def saveFlow(flow : Flow) : Future[Long]
  def getFlow(id: Long): Future[Option[Flow]]
  def updateFlowStatusForFlow(flow: Flow, status: String, executionTime: Long = 0): Future[Long]
  def getNextFlowStep(flow: Flow, allSteps: List[Class[_ <: Step]]): Future[Option[(Class[_ <: Step], StepResult)]]
  def updateFlowStep(flow: Flow, stepStatus: StepResult): Future[Long]
}

class FlowHandlerImpl(val modules: Configuration with PersistenceModule)(implicit ec: ExecutionContext) extends FlowHandler with AppLogger {

  override def getAll : Future[Seq[Flow]] = {
    modules.flowsDal.findAll()
  }

  override def getOne(id : Long) : Future[Option[Flow]] = {
    modules.flowsDal.findById(id)
  }

  override def deleteAll : Unit = {
    modules.deleteAll
  }

  override def saveFlow(flow : Flow) : Future[Long] = {
    modules.flowsDal.insert(flow)
  }

  override def getFlow(id: Long): Future[Option[Flow]] = {
    modules.flowsDal.findById(id)
  }

  override def updateFlowStatusForFlow(flow: Flow, status: String, executionTime: Long = 0): Future[Long] = {
    modules.flowsDal.update(flow.copy(status = Some(status), executionTime = Some(executionTime)))
  }

  override def getNextFlowStep(flow: Flow, allSteps: List[Class[_ <: Step]]): Future[Option[(Class[_ <: Step], StepResult)]] = {
    val flowId = flow.id.getOrElse(throw new FlowNotFoundException(-1L))
    for {
      flowSteps <- getFlowStepsForFlow(flowId)
    } yield {
      allSteps
        .map(pairUp(flowId, flowSteps))
        .find(unfinishedStep)
        .map(step => (step._1, StepResult(stepName = step._2.stepName, status = Some(step._2.status))))
    }
  }

  private def pairUp(flowId: Long, flowSteps: List[FlowStep]): (Class[_ <: Step]) => (Class[_ <: Step], FlowStep) = {
    stepClass => {
      flowSteps.find(_.stepName == stepClass.getSimpleName) match {
        case Some(flowStep) => (stepClass, flowStep)
        case None => (stepClass, FlowStep(flowId = flowId, stepName = stepClass.getSimpleName, status = "TODO"))
      }
    }
  }

  private def unfinishedStep: ((Class[_], FlowStep)) => Boolean = {
    step => step._2.status != "COMPLETE" && step._2.status != "SKIPPED"
  }

  override def updateFlowStep(flow: Flow, stepStatus: StepResult): Future[Long] = {
    val flowId = flow.id.getOrElse(throw new FlowNotFoundException(-1L))
    modules.flowStepsDal.findByFlowIdAndStepName(flowId, stepStatus.stepName).flatMap {
      case Some(flowStep) => modules.flowStepsDal.update(flowStep.copy(status = stepStatus.status.get, executionTime = stepStatus.executionTime))
      case None => modules.flowStepsDal.insert(FlowStep(id = None, flowId = flowId, stepName = stepStatus.stepName, status = stepStatus.status.getOrElse(""), executionTime = stepStatus.executionTime, createdAt = Some(new Timestamp(new DateTime().getMillis))))
    }

  }

  private def hasFlowStatus(flow: Flow, status: String): Boolean = {
    flow.status == status
  }


  private def getFlowStepsForFlow(flowId: Long): Future[List[FlowStep]] = {
    for {
      flowSteps <- modules.flowStepsDal.findByFlowId(flowId)
    } yield {
      log.debug(s"Flow steps for Flow [$flowId]: " + flowSteps)
      flowSteps.toList
    }
  }

}