package persistence.dals

import persistence.entities.Flow
import persistence.entities.SlickTables.FlowsTable
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{ExecutionContext, Future}

trait FlowsDal extends BaseDalImpl[FlowsTable,Flow] {
  def findByFlowId(id : Long) : Future[Option[Flow]] = findById(id)
}

class FlowsDalImpl()(implicit override val db: JdbcProfile#Backend#Database, implicit override val ec: ExecutionContext) extends FlowsDal {

}