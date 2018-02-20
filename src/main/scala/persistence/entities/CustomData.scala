package persistence.entities

import java.sql.Timestamp

import org.json4s.JValue

case class CustomData(id: Option[Long],
                      flowId: Long,
                      key: String,
                      value: JValue,
                      createdAt: Option[Timestamp]) extends BaseEntity