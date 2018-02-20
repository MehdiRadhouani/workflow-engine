package models

case class CustomDataNotFoundException(key: String, id: Long) extends RuntimeException(s"Custom Data with key $key for flow with id $id was not found")