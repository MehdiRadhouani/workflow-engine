package persistence.dals

import persistence.entities.FlowStep
import persistence.entities.SlickTables.FlowStepsTable
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{ExecutionContext, Future}

trait FlowStepsDal extends BaseDalImpl[FlowStepsTable,FlowStep] {
  def findByFlowId(flowId: Long): Future[Seq[FlowStep]]
  def findByFlowIdAndStepName(id : Long, stepName: String) : Future[Option[FlowStep]]
}

class FlowStepsDalImpl()(implicit override val db: JdbcProfile#Backend#Database, implicit override val ec: ExecutionContext) extends FlowStepsDal {
  override def findByFlowId(flowId: Long): Future[Seq[FlowStep]] = {
    findByFilter(flowStep => flowStep.flowId === flowId)
  }

  override def findByFlowIdAndStepName(flowId: Long, stepName: String): Future[Option[FlowStep]] = {
    findByFilter(flowStep => flowStep.flowId === flowId && flowStep.stepName === stepName).map(_.headOption)
  }
}