package workflow

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}

abstract class BaseActorSpec extends TestKit(ActorSystem("workflow"))
                                with FlatSpecLike
                                with Matchers
                                with MockitoSugar
                                with BeforeAndAfterAll
                                with ImplicitSender {

  override def afterAll = TestKit.shutdownActorSystem(system)
}