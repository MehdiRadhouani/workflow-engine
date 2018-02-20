package persistence.entities

import java.sql.Timestamp

case class Flow(id: Option[Long],
                organizationId: String,
                status: Option[String],
                parameters: Map[String, String],
                executionTime: Option[Long],
                createdAt: Option[Timestamp]
                ) extends BaseEntity