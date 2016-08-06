package models.slack

import play.api.libs.json.Json

object SlackCodeForTokenResponse {
    implicit val slackCodeForTokenResponseReads = Json.reads[SlackCodeForTokenResponse]
}

/**
  * Represents the JSOn returned when trying to exchange a code for an auth token
  */
case class SlackCodeForTokenResponse(
                                      ok: Boolean,
                                      error: Option[String],
                                      access_token: Option[String],
                                      scope: Option[String],
                                      user_id: Option[String],
                                      team_name: Option[String],
                                      team_id: Option[String])
