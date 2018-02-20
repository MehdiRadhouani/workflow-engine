package models

case class StepResult(stepName: String, status: Option[String], executionTime: Long = 0)