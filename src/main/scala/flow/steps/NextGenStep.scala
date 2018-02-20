package flow.steps

import java.sql.Timestamp

import akka.actor.ActorRef
import models.StepResult
import org.joda.time.DateTime
import org.json4s.{DefaultFormats, Extraction}
import persistence.entities.{CustomData, Flow}
import utils._
import workflow.Step

import scala.concurrent.Future
import scala.util.Random


class NextGenStep1(parent: ActorRef, flow: Flow, stepName: String, modules: Configuration with ServiceModule) extends Step(parent, flow, stepName, modules) {

  implicit val formats = DefaultFormats
  import context.dispatcher

  override def execute(work: Any): Future[StepResult] = {
    log.info(s"NextGenStep1 talking")

    //    println(modules.templateEngine.layout("templates/email-template.ssp",
    //        Map("person" -> Person(firstName = "Mehdi", lastName = "Radhouani", signature = "My Sign", location = "Amsterdam")
    //        )))
    //    val future = for {
    //      r <- modules.salesforce.getRenewalInfo
    //    } yield {
    //      r
    //    }


    for {
      r <- Future{Thread.sleep(Random.nextInt(2)); AnnualStandardUsage(SingleUsage = Some(123), PeakUsage = Some(456), OffPeakUsage = Some(789))}
      upserted <- modules.customData.upsert(CustomData(None, flowId = flow.id.get, key = "someKey", Extraction.decompose(r), createdAt = Some(new Timestamp(new DateTime().getMillis))))
    } yield {
      log.info("Save Custom Data {}", r)
      StepResult(stepName, Some("COMPLETE"))
    }
  }

  override def shouldExecute: Boolean = true
}

class NextGenStep2(parent: ActorRef, flow: Flow, stepName: String, modules: Configuration with ServiceModule) extends Step(parent, flow, stepName, modules) {

  implicit val formats = DefaultFormats
  import context.dispatcher

  override def execute(work: Any): Future[StepResult] = {
    log.info(s"NextGenStep2 talking")
    modules.customData.find(flowId = flow.id.get, key = "someKey").map {
      case Some(customData) => {
        log.info("find Flow Data {}", customData.value.extract[AnnualStandardUsage])
        StepResult(stepName, Some("COMPLETE"))
      }
      case None => throw new IllegalArgumentException("Not found")
    }
  }

  override def shouldExecute: Boolean = true
}

class NextGenStep3(parent: ActorRef, flow: Flow, stepName: String, modules: Configuration with ServiceModule) extends Step(parent, flow, stepName, modules) {

  implicit val formats = DefaultFormats
  import context.dispatcher

  override def execute(work: Any): Future[StepResult] = {
    log.info(s"NextGenStep3 talking")

    if(Random.nextBoolean())
      throw new IllegalArgumentException("Failure in NextGenStep3")

    if(Random.nextBoolean()) {
      for {
        ip <- Future(Thread.sleep(Random.nextInt(204)))
      } yield {
        StepResult(stepName, Some("COMPLETE"))
      }
    } else {
      Future(StepResult(stepName, Some("COMPLETE")))
    }
  }

  override def shouldExecute: Boolean = true
}

class NextGenStep4(parent: ActorRef, flow: Flow, stepName: String, modules: Configuration with ServiceModule) extends Step(parent, flow, stepName, modules) {

  implicit val formats = DefaultFormats
  import context.dispatcher

  override def execute(work: Any): Future[StepResult] = {
    log.info(s"NextGenStep4 talking")

    if(Random.nextBoolean())
      throw new IllegalArgumentException("Failure in NextGenStep4")

    if(Random.nextBoolean()) {
      for {
        ip <- Future(Thread.sleep(Random.nextInt(2)))
      } yield {
        StepResult(stepName, Some("COMPLETE"))
      }
    } else {
      Future(StepResult(stepName, Some("COMPLETE")))
    }
  }

  override def shouldExecute: Boolean = true
}

case class AnnualStandardUsage(
                                SingleUsage: Option[BigDecimal] = None,
                                PeakUsage: Option[BigDecimal] = None,
                                OffPeakUsage: Option[BigDecimal] = None
                              )