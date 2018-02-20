package utils

import com.github.tminglei.slickpg._
import org.json4s._
import persistence.dals._
import persistence.handlers.{FlowHandler, FlowHandlerImpl}
import slick.jdbc.{PostgresProfile, JdbcProfile}
import slick.basic.Capability
import slick.jdbc.JdbcCapabilities

trait CustomPostgresProfile extends PostgresProfile
	with PgJson4sSupport
	with PgHStoreSupport
	with array.PgArrayJdbcTypes {
	/// for json support
	override val pgjson = "jsonb"
	type DOCType = org.json4s.native.Document
	override val jsonMethods = org.json4s.native.JsonMethods.asInstanceOf[JsonMethods[DOCType]]

	override protected def computeCapabilities: Set[Capability] = super.computeCapabilities + JdbcCapabilities.insertOrUpdate

	override val api: API = new API {}

	val plainAPI = new API with Json4sJsonPlainImplicits

	trait API extends super.API with JsonImplicits with HStoreImplicits {
		implicit val strListTypeMapper = new SimpleArrayJdbcType[String]("text").to(_.toList)
		implicit val json4sJsonArrayTypeMapper =
			new AdvancedArrayJdbcType[JValue](pgjson,
				(s) => utils.SimpleArrayUtils.fromString[JValue](jsonMethods.parse(_))(s).orNull,
				(v) => utils.SimpleArrayUtils.mkString[JValue](j=>jsonMethods.compact(jsonMethods.render(j)))(v)
			).to(_.toList)
	}
}

object CustomPostgresProfile extends CustomPostgresProfile

trait DbModule {
	val db: JdbcProfile#Backend#Database
}

trait PersistenceModule {
	val flowsDal: FlowsDal
	val flowStepsDal: FlowStepsDal
	val customDataDal: CustomDataDal
	def generateDDL : Unit
	def deleteAll : Unit
}


trait PersistenceModuleImpl extends PersistenceModule with DbModule{
	this: Configuration with ActorModule  =>

	import CustomPostgresProfile.api._
	override implicit val db = Database.forConfig("pgdb")
	implicit val ec = system.dispatcher

	override val flowsDal = new FlowsDalImpl
	override val flowStepsDal = new FlowStepsDalImpl
	override val customDataDal = new CustomDataDalImpl

	override def generateDDL(): Unit = {
		flowsDal.createTable()
		flowStepsDal.createTable()
		customDataDal.createTable()
	}

	override def deleteAll(): Unit = {
		flowsDal.deleteAll()
		flowStepsDal.deleteAll()
		customDataDal.deleteAll()
	}

}
