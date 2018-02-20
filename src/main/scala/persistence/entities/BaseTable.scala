package persistence.entities

import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag
import java.sql.Timestamp

abstract class BaseTable[T](tag: Tag, name: String) extends Table[T](tag, name) {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def createdAt = column[Timestamp]("createdAt")
}