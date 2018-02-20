package utils

import com.typesafe.config.{Config, ConfigFactory}

trait Configuration {
  val config: Config
  val httpHost: String
  val httpPort: Int
  val salesforceSettings: SalesforceSettings
}

trait ConfigurationModuleImpl extends Configuration {
  override val config : Config = ConfigFactory.load()

  override val httpHost: String = config.getString("http.interface")
  override val httpPort: Int = config.getInt("http.port")
  override val salesforceSettings = new SalesforceSettings(config)

}

class SalesforceSettings(config : Config) {
  val clientId = config.getString("salesforce.client_id")
  val clientSecret = config.getString("salesforce.client_secret")
  val username = config.getString("salesforce.username")
  val password = config.getString("salesforce.password")
}