package utils

import de.innfactory.akka.AuthService
import de.innfactory.akka.jwt.AutoValidator
import org.fusesource.scalate.TemplateEngine
import persistence.handlers.{CustomDataHandler, CustomDataHandlerImpl, FlowHandler, FlowHandlerImpl}
import service.salesforce.{SalesforceServiceImpl, SalesforceService}

trait ServiceModule {
  val flowHandler: FlowHandler
  val customData: CustomDataHandler
  val authService: AuthService
  val templateEngine: TemplateEngine
  val salesforce: SalesforceService
}

trait ServiceModuleImpl extends ServiceModule {
  this: Configuration with PersistenceModule with ActorModule =>

  private implicit val ec = system.dispatcher

  override val authService = new AuthService(new AutoValidator)
  override val templateEngine = new TemplateEngine
  override val flowHandler = new FlowHandlerImpl(this)
  override val customData = new CustomDataHandlerImpl(this)
  override val salesforce = new SalesforceServiceImpl(this)
}