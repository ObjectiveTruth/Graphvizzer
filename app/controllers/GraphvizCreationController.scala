package controllers

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, File}
import javax.inject._

import akka.actor.ActorSystem
import akka.stream.scaladsl.{FileIO, Source}
import models.{SlackChannelUserResponse, SlackPrivateUserResponse, SlashCommandIn}
import play.api.mvc._
import play.api.libs.json.Json
import com.typesafe.config.ConfigFactory
import play.api.Logger
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.mvc.MultipartFormData.{DataPart, FilePart}

import scala.sys.process._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

@Singleton
class GraphvizCreationController @Inject() (ws: WSClient)(actorSystem: ActorSystem)(implicit exec: ExecutionContext)
  extends Controller {
    val config = ConfigFactory.load("strings.conf")
    val BAD_FORM_DATA_MSG = config.getString("SystemMessages.BadFormDataFromSlack")
    val BAD_TOKEN_MSG = config.getString("SystemMessages.BadTokenFromSlack")
    val PROCESSING_MSG = config.getString("BusinessMessages.ProcessingYourRequest")
    val BAD_DOT_FORMAT_MSG = config.getString("BusinessMessages.BadDOTFormatFromSlack")
    val SLACK_EXPECTED_TOKEN = "sf0Rq4MMxUUSnTK29cknMRHI"
    val / = File.separator

    def foo = Action.async { implicit request =>
        _doImageCreationAndGetImgurLink(SlashCommandIn("", "", "", "", "", "", "", "", "digraph G{a->b;}", ""))
        Future{Ok("good")}
    }

    def createGraphvizDotStringAndReturnImgurLink = Action.async{ implicit request =>
        import SlashCommandIn._

        slackForm.bindFromRequest.fold(
            formWithErrors => {
                Logger.warn(s"Incorrect Form Format: ${request.body.asText.getOrElse("<No Body>")}}")
                Future{Ok(Json.toJson(SlackPrivateUserResponse(BAD_FORM_DATA_MSG)))}
            },

            goodValidatedSlackRequest => {
                if(goodValidatedSlackRequest.token.contentEquals(SLACK_EXPECTED_TOKEN)) {
                    _doImageCreationAndGetImgurLink(goodValidatedSlackRequest)
                    Future{Ok(Json.toJson(SlackPrivateUserResponse(PROCESSING_MSG)))}

                }else {
                    Future{Ok(Json.toJson(SlackPrivateUserResponse(BAD_TOKEN_MSG)))}
                }
            }
        )
    }

    def _doImageCreationAndGetImgurLink(slackInput: SlashCommandIn): Unit = {
        val SYSTEM_SUCCESS_CODE = 0
        val userTextWithoutNewLines = slackInput.text.replaceAll("\\\\r|\\\\n", " ")

        Logger.debug(s"DOT String(whitespace stripped): $userTextWithoutNewLines")
        val userTextAsIS = new ByteArrayInputStream(userTextWithoutNewLines.getBytes("UTF-8"))
        val temporaryFileName = s"tmp${/}${System.currentTimeMillis()}.png"

        val out = (s"dot -Tpng -o $temporaryFileName" #< userTextAsIS).!

        if (out != SYSTEM_SUCCESS_CODE) {
            Logger.warn(s"Bad Dot Format: $userTextWithoutNewLines")
            sendResponseToUser(slackInput.response_url, SlackPrivateUserResponse(BAD_DOT_FORMAT_MSG))
        } else {
/*            uploadFileToImgur(temporaryFileName, slackInput).onComplete(tryResponse => {
                _cleanupTemporaryFile(temporaryFileName)

                if(tryResponse.isFailure) {
                    Logger.debug("Bad imgur response", tryResponse.failed.get)
                }else{
                    Logger.debug("Good imgur response: " + tryResponse.get.toString)
                }
            })*/
        }
    }

    def sendResponseToUser(slackResponseUrl: String, slackPrivateUserResponse: SlackPrivateUserResponse): Unit = {
        ws.url(slackResponseUrl)
            .withHeaders("CONTENT-TYPE" -> "application/json")
          .withBody(Json.toJson(slackPrivateUserResponse))
    }

    def uploadFileToImgur(temporaryFilename: String,
                          slackInput: SlashCommandIn): Future[WSResponse] = {
        ws.url(slackInput.response_url)
          .post(Source(
              FilePart("image", temporaryFilename, Option("image/png"), FileIO.fromFile(new File(temporaryFilename)))
                :: DataPart("type", "file")
                :: List()))
    }

    def _cleanupTemporaryFile(tempFilename: String): Unit = {
        val file = new File(tempFilename)
        if(file.exists() && file.isFile) {
            if(!file.delete()) {
                Logger.error(s"Could not delete temporary file: ${tempFilename}")
            }
        }
    }





/*        val imgurResponse: HttpResponse[String] = Http(IMGUR_API_UPLOAD_ENDPOINT)
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
