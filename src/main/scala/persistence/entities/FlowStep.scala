package persistence.entities

import java.sql.Timestamp

case class FlowStep(id: Option[Long] = None,
                    flowId: Long,
                    stepName: String,
                    status: String,
                    executionTime: Long = 0,
                    createdAt: Option[Timestamp] = None) extends BaseEntity