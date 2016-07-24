package models

import play.api.libs.json.{Json}

object SlackPrivateUserResponse {
    implicit val slackPrivateuserResponseWrites = Json.writes[SlackPrivateUserResponse]
}

case class SlackPrivateUserResponse(text: String, response_type: Option[String] = Some("ephemeral"))

