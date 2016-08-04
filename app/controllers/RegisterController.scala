package controllers

import javax.inject.Inject

import akka.actor.ActorSystem
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.mvc.{Action, Controller}
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
    val dbConfig = dbConfigProvider.get[JdbcProfile]

    def registerTeam(code: Int) = Action.async { implicit request =>
      //https://slack.com/api/oauth.access?
        // client_id=29667068068.63519026177
        // &client_secret=25172e4a0342ca40fffeed99ad8ea416
        // &code=32435995891.66111737364.f4a625b2e7
      exchangeCodeForTokenAndWriteToDatabase(code)
        Future{Ok(s"hello, ${code}")}
    }

    def teamStatus = Action.async { implicit request =>
        Future{Ok("fjeklsfje")}
    }

    def exchangeCodeForTokenAndWriteToDatabase(code: Int): Unit = {
        ws.url(SLACK.OAUTH_ENDPOINT)
          .withQueryString(
              "client_id" -> SLACK.CLIENT_ID,
              "client_secret" -> SLACK.APP_SECRET,
              "code" -> code.toString
          )
          .get()
          .onComplete({
              case Success(s: WSResponse) => {
                  Logger.info(s"Received response from Slack oauth endpoint with status: ${s.status}, body: ${s.body}")
                  if(s.status >= 200 && s.status < 300 ) {
                      s.json.validate[SlackCodeForTokenResponse] match {
                          case slackCodeForTokenResponse: JsSuccess[SlackCodeForTokenResponse] => {

                              if(slackCodeForTokenResponse.value.ok) {
                                  Logger.info("Token Received Successfully!")
                              }else {
                                  Logger.error(s"Slack rejected the code when trying to exchange for token: " +
                                    slackCodeForTokenResponse)
                              }

                          }
                          case _ => { Logger.error("Invalid JSON when exchanging code for token with Slack") }
                      }
                  }else { Logger.error("Bad response Slack when exchanging code for access token") }
              }
              case Failure(f: Throwable) => { Logger.error("Could not exchange Slack code for access token", f) }
          })
    }
}
