package persistence.dals

import logging.AppLogger
import persistence.entities.{BaseEntity, BaseTable}
import slick.jdbc.JdbcProfile
import slick.lifted.{CanBeQueryCondition, TableQuery}
import utils.{CustomPostgresProfile, DbModule}

import scala.concurrent.{ExecutionContext, Future}

trait BaseDal[T,A] {
  def insert(row : A): Future[Long]
  def insert(rows : Seq[A]): Future[Seq[Long]]
  def update(row : A): Future[Long]
  def update(rows : Seq[A]): Future[Unit]
  def findAll(): Future[Seq[A]]
  def findById(id : Long): Future[Option[A]]
  def findByFilter[C : CanBeQueryCondition](f: (T) => C): Future[Seq[A]]
  def deleteById(id : Long): Future[Int]
  def deleteById(ids : Seq[Long]): Future[Int]
  def deleteByFilter[C : CanBeQueryCondition](f:  (T) => C): Future[Int]
  def deleteAll() : Future[Unit]
  def createTable() : Future[Unit]
}

class BaseDalImpl[T <: BaseTable[A], A <: BaseEntity]()(implicit val tableQ: TableQuery[T], implicit val db: JdbcProfile#Backend#Database, implicit val ec: ExecutionContext) extends BaseDal[T,A] with DbModule with AppLogger {

  import CustomPostgresProfile.api._

  override def insert(row: A): Future[Long] = {
    insert(Seq(row)).map(_.head) recover {
      case e: Exception =>
        log.error("Failed ...", e)
        throw new IllegalArgumentException("Failed ...", e)
    }
  }

  override def insert(rows: Seq[A]): Future[Seq[Long]] = {
      db.run(tableQ returning tableQ.map(_.id) ++= rows.filter(_.isValid))
  }

  override def update(row: A): Future[Long] = {
    if (row.isValid)
      db.run(tableQ.filter(_.id === row.id).update(row).map(_ => row.id.get))
    else
      Future {
        0
      }
  }

  override def update(rows: Seq[A]): Future[Unit] = {
    db.run(DBIO.seq(rows.filter(_.isValid).map(r => tableQ.filter(_.id === r.id).update(r)): _*))
  }

  override def findAll(): Future[Seq[A]] = {
    db.run(tableQ.result)
  }

  override def findById(id: Long): Future[Option[A]] = {
    db.run(tableQ.filter(_.id === id).result.headOption)
  }

  override def findByFilter[C: CanBeQueryCondition](f: (T) => C): Future[Seq[A]] = {
    db.run(tableQ.withFilter(f).result)
  }

  override def deleteById(id: Long): Future[Int] = {
    deleteById(Seq(id))
  }

  override def deleteById(ids: Seq[Long]): Future[Int] = {
    db.run(tableQ.filter(_.id.inSet(ids)).delete)
  }

  def deleteAll(): Future[Unit] = {
    db.run(DBIO.seq(tableQ.delete))
  }

  override def deleteByFilter[C : CanBeQueryCondition](f:  (T) => C): Future[Int] = {
    db.run(tableQ.withFilter(f).delete)
  }

  override def createTable() : Future[Unit] = {
    db.run(DBIO.seq(tableQ.schema.create)) recover {
      case e: Exception =>
        log.error("Failed ...", e)
        throw new IllegalArgumentException("Failed ...", e)
    }
  }

}