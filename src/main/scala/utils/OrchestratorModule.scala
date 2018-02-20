package utils

import orchestrators.{FlowOrchestratorImpl, FlowOrchestrator}

trait OrchestratorModule {
  val flowOrchestrator: FlowOrchestrator
}

trait OrchestratorModuleImpl extends OrchestratorModule {
  this: Configuration with ServiceModule with ActorModule =>

  private implicit val executionContext = system.dispatcher

  override val flowOrchestrator = new FlowOrchestratorImpl(this)
}