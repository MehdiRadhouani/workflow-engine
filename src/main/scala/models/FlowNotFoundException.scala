package models

case class FlowNotFoundException(id: Long) extends RuntimeException(s"Flow with $id was not found")