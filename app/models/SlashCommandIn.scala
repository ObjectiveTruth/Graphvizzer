package models

import play.api.data.Form
import play.api.data._
import play.api.data.Forms._

case class SlashCommandIn(
                           token: String,
                           teamId: String,
                           teamDomain: String,
                           channelId: String,
                           channelName: String,
                           userId: String,
                           userName: String,
                           command: String,
                           text: String,
                           responseUrl: String
                         )

object SlashCommandIn {
    val slackForm = Form(
        mapping(
            "token" -> nonEmptyText,
            "teamId" -> nonEmptyText,
            "teamDomain" -> nonEmptyText,
            "channelId" -> nonEmptyText,
            "channelName" -> nonEmptyText,
            "userId" -> nonEmptyText,
            "userName" -> nonEmptyText,
            "command" -> nonEmptyText,
            "text" -> nonEmptyText,
            "responseUrl" -> nonEmptyText
        )(SlashCommandIn.apply)(SlashCommandIn.unapply)
    )
}

