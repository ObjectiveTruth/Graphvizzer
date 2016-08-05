package controllers

import javax.inject.Inject

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.mvc.{Action, Controller, Result}
import slick.driver.JdbcProfile
import common.constants._
import models.SlackCodeForTokenResponse
import play.api.Logger
import play.api.libs.json.JsSuccess
import models.database.TeamInfo

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class RegisterController @Inject()(dbConfigProvider: DatabaseConfigProvider)(ws: WSClient)(actorSystem: ActorSystem)
                                  (implicit exec: ExecutionContext)
  extends Controller {
    val config = ConfigFactory.load("strings.conf")
    val UNEXPECTED_SYSTEM_ERROR = config.getString("SystemMessages.UnexpectedSystemError")
    val REGISTER_SUCCESS = config.getString("BusinessMessages.SuccessfulRegister")

    val dbConfig = dbConfigProvider.get[JdbcProfile]

    def registerTeam(code: String) = Action.async { implicit request =>
        exchangeCodeForToken(code)
    }

    def teamStatus = Action.async { implicit request =>
        Future{Ok("To Implement")}
    }

    def exchangeCodeForToken(code: String): Future[Result] = {
        ws.url(SLACK.OAUTH_ENDPOINT)
            .withQueryString(
              "client_id" -> SLACK.CLIENT_ID,
              "client_secret" -> SLACK.APP_SECRET,
              "code" -> code
            )
            .get()
            .flatMap{ s =>
              Logger.info(s"Received response from Slack oauth endpoint with status: ${s.status}, body: ${s.body}")
              if(s.status >= 200 && s.status < 300 ) {
                  Future{Ok("")}
                  s.json.validate[SlackCodeForTokenResponse] match {
                      case slackCodeForTokenResponse: JsSuccess[SlackCodeForTokenResponse] => {

                          if(slackCodeForTokenResponse.value.ok) {
                              Logger.info("Token Received Successfully!")
                              Future{Ok(REGISTER_SUCCESS)}
                          }else {
                              Logger.error(s"Slack rejected the code when trying to exchange for token: " +
                                slackCodeForTokenResponse.value)
                              Future{Ok(UNEXPECTED_SYSTEM_ERROR)}
                          }

                      }
                      case _ =>
                          Logger.error("Invalid JSON when exchanging code for token with Slack")
                          Future{Ok(UNEXPECTED_SYSTEM_ERROR)}
                  }
              }else {
                  Logger.error("Unexpected response Slack when exchanging code for access token")
                  Future{Ok(UNEXPECTED_SYSTEM_ERROR)}
              }
        }
    }
}
