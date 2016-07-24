package models

import play.api.data.Form
import play.api.data._
import play.api.data.Forms._

case class SlashCommandIn(
                           token: String,
                           team_id: String,
                           team_domain: String,
                           channel_id: String,
                           channel_name: String,
                           user_id: String,
                           user_name: String,
                           command: String,
                           text: String,
                           response_url: String
                         )

object SlashCommandIn {
    val slackForm = Form(
        mapping(
            "token" -> nonEmptyText,
            "team_id" -> nonEmptyText,
            "team_domain" -> nonEmptyText,
            "channel_id" -> nonEmptyText,
            "channel_name" -> nonEmptyText,
            "user_id" -> nonEmptyText,
            "user_name" -> nonEmptyText,
            "command" -> nonEmptyText,
            "text" -> nonEmptyText,
            "response_url" -> nonEmptyText
        )(SlashCommandIn.apply)(SlashCommandIn.unapply)
    )
}

