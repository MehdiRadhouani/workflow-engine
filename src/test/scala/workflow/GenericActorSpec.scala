package workflow

import java.sql.Timestamp

import akka.actor.{Props, ActorRef}
import akka.testkit.{TestProbe, TestActorRef}
import akka.util.Timeout
import models.StepResult
import org.joda.time.DateTime
import org.mockito.Matchers.anyLong
import org.mockito.Mockito._
import persistence.entities.Flow
import utils._
import workflow.StepProtocol.Work
import workflow.WorkflowProtocol.UpdateWorkflowStep

import scala.concurrent.Future
import scala.concurrent.duration._


class TestStep(parent: ActorRef, flow: Flow, stepName: String, modules: Configuration with ServiceModule) extends Step(parent, flow, stepName, modules) {
  import context.dispatcher
  override def shouldExecute: Boolean = true
  override def execute(work: Any): Future[StepResult] = Future(StepResult(stepName, Some("COMPLETED")))
}

class GenericActorSpec extends BaseActorSpec {

  implicit val askTimeout = Timeout(1 second)

  "TestStepActor" should "return a COMPLETE status for someStep" in {
    val modules = new ConfigurationModuleImpl  with ActorModuleImpl with PersistenceModuleImpl with OrchestratorModuleImpl with ServiceModuleImpl
    val workflowActorRef = TestActorRef(new Worker(TestActorRef[Master], modules))
    val flow = Flow(id = Some(1), organizationId = "SomeOrgId", parameters = Map("clusterReference" -> "SomeClusterReference"), status = Some("STARTED"), executionTime = Some(0), createdAt = Some(new Timestamp(new DateTime().getMillis)))
    val actorRef = TestActorRef(spy(new TestStep(workflowActorRef, flow, "someStep", modules)))
    val testActorSpy = actorRef.underlyingActor
    when(testActorSpy.getExecutionTime(anyLong)).thenReturn(1000)

    // When
    actorRef ! Work

    // Then
    val expectedResult = UpdateWorkflowStep(flow, StepResult("someStep", Some("COMPLETE"), 1000))
    expectMsg(expectedResult)
  }

}
