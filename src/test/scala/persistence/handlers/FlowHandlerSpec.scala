package persistence.handlers

import java.sql.Timestamp

import akka.actor.ActorRef
import com.typesafe.config.{Config, ConfigFactory}
import models.StepResult
import org.joda.time.DateTime
import org.specs2.mock.Mockito
import org.specs2.mutable._
import persistence.dals.FlowStepsDalImpl
import persistence.entities.{Flow, FlowStep}
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import utils._
import workflow.Step

import scala.concurrent.Future


trait BaseSpec extends Specification with Mockito {
  trait Modules extends ConfigurationModuleImpl with PersistenceModuleImpl with ActorModuleImpl with ServiceModuleImpl with DbModule {
    override implicit val db = mock[utils.CustomPostgresProfile.backend.Database]
    override val flowStepsDal = mock[FlowStepsDalImpl]
  }
}

object MockExampleSpec extends BaseSpec {

  val flow = Flow(id = Some(1), organizationId = "SomeOrgId", parameters = Map("clusterReference" -> "SomeClusterReference"), status = Some("STARTED"), executionTime = Some(0), createdAt = Some(new Timestamp(new DateTime().getMillis)))

  "flowHandler" should {
    "get next flow step with status error" in {

      val modules = new Modules {}
      val flowHandler = new FlowHandlerImpl(modules)
      modules.flowStepsDal.findByFlowId(1) returns Future.successful(List(FlowStep(Some(1), 1, "TestStep1", "COMPLETE", 1000, None), FlowStep(Some(2), 1, "TestStep2", "SKIPPED", 1000, None), FlowStep(Some(3), 1, "TestStep3", "ERROR", 1000, None)))

      val result = flowHandler.getNextFlowStep(flow, List(classOf[TestStep1], classOf[TestStep2], classOf[TestStep3], classOf[TestStep4]))

      result must be_==(Some(classOf[TestStep3], StepResult("TestStep3",Some("ERROR"),0))).await
    }

    "get next flow step with status todo" in {

      val modules = new Modules {}
      val flowHandler = new FlowHandlerImpl(modules)
      modules.flowStepsDal.findByFlowId(1) returns Future.successful(List(FlowStep(Some(1), 1, "TestStep1", "COMPLETE", 1000, None), FlowStep(Some(2), 1, "TestStep2", "SKIPPED", 1000, None)))

      val result = flowHandler.getNextFlowStep(flow, List(classOf[TestStep1], classOf[TestStep2], classOf[TestStep3], classOf[TestStep4]))

      result must be_==(Some(classOf[TestStep3], StepResult("TestStep3",Some("TODO"),0))).await
    }

    "get None since all steps were executed" in {

      val modules = new Modules {}
      val flowHandler = new FlowHandlerImpl(modules)
      modules.flowStepsDal.findByFlowId(1) returns Future.successful(List(FlowStep(Some(1), 1, "TestStep1", "COMPLETE", 1000, None), FlowStep(Some(2), 1, "TestStep2", "SKIPPED", 1000, None)))

      val result = flowHandler.getNextFlowStep(flow, List(classOf[TestStep1], classOf[TestStep2]))

      result must be_==(None).await
    }
  }

  class TestStep1(parent: ActorRef, flow: Flow, stepName: String, modules: Configuration with ServiceModule) extends Step(parent, flow, stepName, modules) {
    override def execute(work: Any): Future[StepResult] = ???
    override def shouldExecute: Boolean = ???
  }

  class TestStep2(parent: ActorRef, flow: Flow, stepName: String, modules: Configuration with ServiceModule) extends Step(parent, flow, stepName, modules) {
    override def execute(work: Any): Future[StepResult] = ???
    override def shouldExecute: Boolean = ???
  }

  class TestStep3(parent: ActorRef, flow: Flow, stepName: String, modules: Configuration with ServiceModule) extends Step(parent, flow, stepName, modules) {
    override def execute(work: Any): Future[StepResult] = ???
    override def shouldExecute: Boolean = ???
  }

  class TestStep4(parent: ActorRef, flow: Flow, stepName: String, modules: Configuration with ServiceModule) extends Step(parent, flow, stepName, modules) {
    override def execute(work: Any): Future[StepResult] = ???
    override def shouldExecute: Boolean = ???
  }
}