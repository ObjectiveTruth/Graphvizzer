package controllers

import javax.inject._

import akka.actor.ActorSystem
import akka.stream.scaladsl.{FileIO, Source}
import play.api.mvc._
import play.api.libs.json.{JsSuccess, Json}
import com.typesafe.config.ConfigFactory
import org.asynchttpclient.AsyncHttpClient
import org.asynchttpclient.request.body.multipart.StringPart
import play.api.Logger
import play.api.libs.ws.{WS, WSClient, WSResponse}
import play.api.mvc.MultipartFormData.{DataPart, FilePart}

import scala.concurrent.{ExecutionContext, Future}
import common.constants._
import models.imgur.{ImgurModel, ImgurResponse}
import models.slack.SlackPrivateUserResponse

@Singleton
class GraphvizCreationController @Inject()(imgurModel: ImgurModel)(actorSystem: ActorSystem)(implicit exec: ExecutionContext)
  extends Controller {
    val stringConfig = ConfigFactory.load("strings.conf")
    val applicationConfig = ConfigFactory.load()

    val BAD_FORM_DATA_MSG = stringConfig.getString("SystemMessages.BadFormDataFromSlack")
    val BAD_TOKEN_MSG = stringConfig.getString("SystemMessages.BadTokenFromSlack")
    val PROCESSING_MSG = stringConfig.getString("BusinessMessages.ProcessingYourRequest")
    val SLACK_EXPECTED_TOKEN = ConfigFactory.load().getString("SLACK_AUTHENTICATION_TOKEN")

    def createGraphvizDotStringAndReturnImgurLink = Action.async{ implicit request =>
        import models.slack.SlashCommandIn._

        slackForm.bindFromRequest.fold(
            formWithErrors => {
                Logger.warn(s"Incorrect Form Format: ${request.body.asText.getOrElse("<No Body>")}}")
                Future{Ok(Json.toJson(SlackPrivateUserResponse(BAD_FORM_DATA_MSG)))}
            },

            goodValidatedSlackRequest => {
                if(goodValidatedSlackRequest.token.contentEquals(SLACK_EXPECTED_TOKEN)) {
                    Logger.info("Validation Succeeded")
                    imgurModel.doGraphCreationAndRespondToUserAsynchronously(goodValidatedSlackRequest)
                    Future{Ok(Json.toJson(SlackPrivateUserResponse(PROCESSING_MSG + "\n>>>" +
                      goodValidatedSlackRequest.text)))}

                }else {
                    Logger.warn(s"Incorrect slack token: ${goodValidatedSlackRequest.token}")
                    Future{Ok(Json.toJson(SlackPrivateUserResponse(BAD_TOKEN_MSG)))}
                }
            }
        )
    }
}
