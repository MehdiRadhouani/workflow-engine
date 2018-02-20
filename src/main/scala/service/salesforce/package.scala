package service

import common.MarketSegmentEnum
import enumeratum._
import org.joda.time.LocalDateTime

/**
  * Created by mehdiradhouani on 07/01/2018.
  */
package object salesforce {

  case class LoginResponse(
                            id: String,
                            issued_at: String,
                            token_type: String,
                            instance_url: String,
                            signature: String,
                            access_token: String
                          )

  case class ObjectUpsertedResponse(
                                     objectId: String,
                                     objectClass: String
                          )

  case class Case(
                     attributes: Attributes,
                     CaseNumber: Long,
                     Id: String
                   )

  case class Attributes(
                                  name: String,
                                  url: String
                                )

}
