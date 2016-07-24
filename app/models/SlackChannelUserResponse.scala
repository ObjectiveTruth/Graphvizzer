package models

import play.api.libs.json.Json

case class SlackChannelUserResponse (
                                    text: String,
                                    response_type: Option[String] = Some("in_channel"))

object SlackChannelUserResponse {
    implicit val slackChannelUserResponseWrites = Json.writes[SlackChannelUserResponse]

}
