package persistence.entities

import org.json4s.JValue
import utils.CustomPostgresProfile.api._

object SlickTables {

  class FlowsTable(tag : Tag) extends BaseTable[Flow](tag, "flows") {
    def organizationId = column[String]("organizationId")
    def status = column[String]("status")
    def parameters = column[Map[String, String]]("parameters")
    def executionTime = column[Long]("executionTime")
    def * = (id.?, organizationId, status.?, parameters, executionTime.?, createdAt.?) <> (Flow.tupled, Flow.unapply)
  }

  implicit val flowsTableQ = TableQuery[FlowsTable]

  class FlowStepsTable(tag : Tag) extends BaseTable[FlowStep](tag,"flowSteps") {
    def flowId = column[Long]("flowId")
    def stepName = column[String]("stepName")
    def status = column[String]("status")
    def executionTime = column[Long]("executionTime")
    def * = (id.?, flowId, stepName, status, executionTime, createdAt.?) <> (FlowStep.tupled, FlowStep.unapply)

    def flowFk = foreignKey(
      "flowStepFlowFk",
      flowId,
      flowsTableQ)(_.id)
  }

  implicit val FlowStepsTableQ = TableQuery[FlowStepsTable]

  class CustomDataTable(tag : Tag) extends BaseTable[CustomData](tag,"customData") {
    def flowId = column[Long]("flowId")
    def key = column[String]("key")
    def value = column[JValue]("value")
    def * = (id.?, flowId, key, value, createdAt.?) <> (CustomData.tupled, CustomData.unapply)
    def flowFk = foreignKey(
      "customDataFlowFk",
      flowId,
      flowsTableQ)(_.id)
  }

  implicit val CustomDataTableQ = TableQuery[CustomDataTable]
}