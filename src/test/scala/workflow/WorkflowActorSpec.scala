package workflow

import java.sql.Timestamp
import java.util.concurrent.TimeUnit

import akka.actor.PoisonPill
import akka.testkit.TestActorRef
import akka.util.Timeout
import org.joda.time.DateTime
import persistence.entities.Flow
import utils._
import workflow.WorkflowProtocol.ProcessFlowById

import scala.concurrent.Await
import scala.concurrent.duration._

class WorkflowActorSpec extends BaseActorSpec {

  implicit val timeout = Timeout(100 seconds)

  "The workflow Actor " should "return a COMPLETE status" in {
    // Given
    val master = TestActorRef(new Master)
    val modules = new ConfigurationModuleImpl  with ActorModuleImpl with PersistenceModuleImpl with OrchestratorModuleImpl with ServiceModuleImpl
    val actorRef = TestActorRef(new Worker(master, modules))
    val flow = Flow(id = None, organizationId = "SomeOrgId", parameters = Map("clusterReference" -> "SomeClusterReference"), status = Some("NOT STARTED"), executionTime = Some(0), createdAt = Some(new Timestamp(new DateTime().getMillis)))
    val savedFlow: Long = Await.result(modules.flowHandler.saveFlow(flow), Duration.Inf)

    // When
    actorRef ! ProcessFlowById(savedFlow)

    // Then
    val expectedResult = PoisonPill
    expectMsg(Duration.apply(10, TimeUnit.SECONDS), expectedResult)
  }

}
