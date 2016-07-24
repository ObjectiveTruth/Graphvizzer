package controllers

import javax.inject._

import akka.actor.ActorSystem
import models.{SlackChannelUserResponse, SlackPrivateUserResponse, SlashCommandIn}
import play.api.mvc._
import play.api.libs.json.Json
import com.typesafe.config.ConfigFactory

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GraphvizCreationController @Inject()(actorSystem: ActorSystem)(implicit exec: ExecutionContext)
  extends Controller {
    val config = ConfigFactory.load("strings.conf")
    val BAD_FORM_DATA_MSG = config.getString("SystemMessages.BadFormDataFromSlack")
    val BAD_TOKEN_MSG = config.getString("SystemMessages.BadTokenFromSlack")
    val PROCESSING_MSG = config.getString("BusinessMessages.ProcessingYourRequest")
    val SLACK_EXPECTED_TOKEN = "sf0Rq4MMxUUSnTK29cknMRHI"

    def createGraphvizDotStringAndReturnImgurLink = Action.async{ implicit request =>
        import SlashCommandIn._


        slackForm.bindFromRequest.fold(

            formWithErrors => {
                Future{Ok(Json.toJson(SlackPrivateUserResponse(BAD_FORM_DATA_MSG)))}
            },

            goodValidatedSlackRequest => {
                if(goodValidatedSlackRequest.token.contentEquals(SLACK_EXPECTED_TOKEN)) {
/*                    Future{Ok(
                        Json.toJson(_doImageCreationAndGetImgurLink))
                    }*/
                    Future{Ok(Json.toJson(SlackPrivateUserResponse(PROCESSING_MSG)))}

                }else {
                    Future{Ok(Json.toJson(SlackPrivateUserResponse(BAD_TOKEN_MSG)))}
                }
            }
        )
    }

    def _doImageCreationAndGetImgurLink: SlackChannelUserResponse = {
        SlackChannelUserResponse("Hey")
/*        val userTextWithoutNewLines = command.get.body.text.replaceAll("\r|\n", " ")
        val URLEncodedDotStringFromUser = URLEncoder.encode(userTextWithoutNewLines, "UTF-8")
        val googleAPIsURL = s"https://chart.googleapis.com/chart?chl=$URLEncodedDotStringFromUser&cht=gv"
        println(s"Text from user Parsed, will send this: $googleAPIsURL")

        val imgurResponse: HttpResponse[String] = Http(IMGUR_API_UPLOAD_ENDPOINT)
          .header("AUTHORIZATION", s"Client-ID ${CLIENT_ID}")
          .method("POST")
          .param("image", googleAPIsURL)
          .param("type", "URL")
          .param("title", "Graphviz Graph made in Slack")
          .param("description", "Created using https://github.com/ObjectiveTruth/Graphviz-Slack-App")
          .asString

        println(s"ImgurResponse: $imgurResponse")

        scalaMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        if(imgurResponse.isCodeInRange(199, 300)) {
            val imgurResponseJSON = scalaMapper.readValue(imgurResponse.body, classOf[ImgurResponse])
            val returnResponse = OK(imgurResponseJSON.data.link)
            val sendingBackToSlackUser = Http(command.get.body.responseUrl)
              .header("Content-type", "application/json")
              .method("POST")
              .postData(scalaMapper.writeValueAsString(returnResponse))
              .asString

            println(sendingBackToSlackUser)
        }else{
            val returnBadRequest = BadRequest("Bad DOT Formatting")
            val sendingBackToSlackUser = Http(command.get.body.responseUrl)
              .header("Content-type", "application/json")
              .method("POST")
              .postData(scalaMapper.writeValueAsString(returnBadRequest))
              .asString

            println(sendingBackToSlackUser)
        }*/
    }
}
