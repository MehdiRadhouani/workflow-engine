package persistence.entities

trait BaseEntity {
  val id : Option[Long]
  def isValid : Boolean = true
}