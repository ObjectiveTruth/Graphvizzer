package models.slack

import play.api.libs.json.Json

object SlackPrivateUserResponse {
    implicit val slackPrivateuserResponseWrites = Json.writes[SlackPrivateUserResponse]
}

/**
  * Represents the JSOn to return to Slack. By default it has the `response_type` set to ephemeral so only the user
  * sees the response. NOT posted to channel
  * @see SlackChannelUserResponse
  */
case class SlackPrivateUserResponse(text: String, response_type: Option[String] = Some("ephemeral"))

