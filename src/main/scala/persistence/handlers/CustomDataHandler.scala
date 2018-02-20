package persistence.handlers

import logging.AppLogger
import models.CustomDataNotFoundException
import persistence.entities.CustomData
import utils.{Configuration, PersistenceModule}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by mehdiradhouani on 26/11/2017.
  */
trait CustomDataHandler {
  def upsert(flow : CustomData) : Future[Long]
  def find(flowId: Long, key: String): Future[Option[CustomData]]
  def get(flowId: Long, key: String): Future[CustomData]
}

class CustomDataHandlerImpl(val modules: Configuration with PersistenceModule)(implicit executionContext: ExecutionContext) extends CustomDataHandler with AppLogger {

  override def upsert(flow : CustomData): Future[Long] = {
    modules.customDataDal.upsert(flow)
  }

  override def find(flowId: Long, key: String): Future[Option[CustomData]] = {
    modules.customDataDal.findByFlowIdAndName(flowId, key)
  }

  override def get(flowId: Long, key: String): Future[CustomData] = {
    find(flowId, key) map {
      case Some(customData) => customData
      case None => throw new CustomDataNotFoundException(key, flowId)
    }
  }
}