package orchestrators

import java.sql.Timestamp

import logging.AppLogger
import models.FlowNotFoundException
import org.joda.time.DateTime
import persistence.entities.Flow
import utils._
import workflow.MasterWorkerProtocol.Workload

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by mehdiradhouani on 26/11/2017.
  */

trait FlowOrchestrator {
  def processFlow(flow: Flow): Future[Unit]
  def resumeFlow(flowId: Long): Future[Unit]
  def getAll : Future[Seq[Flow]]
  def getOne(id : Long) : Future[Option[Flow]]
  def deleteAll() : Unit
}

class FlowOrchestratorImpl(val modules: Configuration with ServiceModule with ActorModule)(implicit ec: ExecutionContext) extends FlowOrchestrator with AppLogger {

  override def processFlow(flow: Flow): Future[Unit] = {
    for {
      savedFlow <- modules.flowHandler.saveFlow(flow.copy(status = Some("NOT STARTED"), executionTime = Some(0), createdAt = Some(new Timestamp(new DateTime().getMillis))))
    } yield {
      log.debug("Flow processed from the Orchestrator {}", savedFlow)
      modules.master ! Workload(savedFlow)
    }
  }

  override def resumeFlow(flowId: Long): Future[Unit] = {
    for {
      flow <- modules.flowHandler.getOne(flowId)
    } yield {
      log.debug("Flow resumed from the Orchestrator {}", flow)
      val flowId = flow.getOrElse(throw new FlowNotFoundException(-1L)).id.getOrElse(throw new FlowNotFoundException(-1L))
      modules.master ! Workload(flowId)
    }
  }

  override def getAll : Future[Seq[Flow]] = {
    modules.flowHandler.getAll
  }

  override def getOne(id : Long) : Future[Option[Flow]] = {
    modules.flowHandler.getOne(id)
  }

  override def deleteAll() : Unit = {
    modules.flowHandler.deleteAll
  }
}