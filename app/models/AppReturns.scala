package models

import play.api.libs.json._

object AppReturns {
    case class OK(text: String, response_type: Option[String] = Some("in_channel"))


    case class Unauthorized(text: String)

    case class BadRequest(text: String)

}
