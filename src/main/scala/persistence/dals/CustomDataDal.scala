package persistence.dals

import persistence.entities.CustomData
import persistence.entities.SlickTables.CustomDataTable
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{ExecutionContext, Future}

trait CustomDataDal extends BaseDalImpl[CustomDataTable, CustomData] {
  def findByFlowIdAndName(flowId: Long, name: String): Future[Option[CustomData]]
  def upsert(row: CustomData): Future[Long]
}

class CustomDataDalImpl()(implicit override val db: JdbcProfile#Backend#Database, implicit override val ec: ExecutionContext) extends CustomDataDal {
  override def findByFlowIdAndName(flowId: Long, name: String): Future[Option[CustomData]] = {
    findByFilter(customData => customData.flowId === flowId && customData.key === name).map(_.headOption)
  }

  override def upsert(row: CustomData): Future[Long] = {
    findByFilter(customData => customData.flowId === row.flowId && customData.key === row.key).map(_.headOption).flatMap {
      case Some(customData) => update(customData.copy(value = row.value))
      case None => insert(row)
    }
  }
}