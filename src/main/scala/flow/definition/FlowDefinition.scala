package flow.definition

import flow.steps._
import workflow.Step


object FlowDefinition {

  val steps: List[Class[_ <: Step]] = List (
    classOf[NextGenStep1],
    classOf[NextGenStep2],
    classOf[NextGenStep3],
    classOf[NextGenStep4]
  )
}