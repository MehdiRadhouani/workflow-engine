package utils

import akka.actor.{ActorRef, ActorSystem, Props}
import workflow.{Master, Worker}

trait ActorModule {
  val system: ActorSystem
  val master: ActorRef
}

trait ActorModuleImpl extends ActorModule {
  this: Configuration with ServiceModule =>

  override val system = ActorSystem("workflow-engine-poc", config)

  override val master = system.actorOf(Props[Master], name = "master")
  //val workflowActor = system.actorOf(new RoundRobinPool(5).props(Props(new WorkflowActor(master, this))), name = "workerPool")
  val worker1 = system.actorOf(Props(new Worker(master, this)), name = "worker1")
  val worker2 = system.actorOf(Props(new Worker(master, this)), name = "worker2")
  val worker3 = system.actorOf(Props(new Worker(master, this)), name = "worker3")
  val worker4 = system.actorOf(Props(new Worker(master, this)), name = "worker4")
  val worker5 = system.actorOf(Props(new Worker(master, this)), name = "worker5")
}