package controllers

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, File}
import javax.inject._

import akka.actor.ActorSystem
import akka.stream.scaladsl.{FileIO, Source}
import com.objectivetruth.graphvizslackapp.models.ImgurResponse
import models.{SlackChannelUserResponse, SlackPrivateUserResponse, SlashCommandIn}
import play.api.mvc._
import play.api.libs.json.{JsSuccess, Json}
import com.typesafe.config.ConfigFactory
import org.asynchttpclient.AsyncHttpClient
import org.asynchttpclient.request.body.multipart.StringPart
import play.api.Logger
import play.api.libs.ws.{WS, WSClient, WSResponse}
import play.api.mvc.MultipartFormData.{DataPart, FilePart}

import scala.sys.process._
import scala.concurrent.{ExecutionContext, Future}
import common.constants._

@Singleton
class GraphvizCreationController @Inject() (ws: WSClient)(actorSystem: ActorSystem)(implicit exec: ExecutionContext)
  extends Controller {
    val stringConfig = ConfigFactory.load("strings.conf")
    val applicationConfig = ConfigFactory.load()

    val BAD_FORM_DATA_MSG = stringConfig.getString("SystemMessages.BadFormDataFromSlack")
    val BAD_TOKEN_MSG = stringConfig.getString("SystemMessages.BadTokenFromSlack")
    val PROCESSING_MSG = stringConfig.getString("BusinessMessages.ProcessingYourRequest")
    val BAD_DOT_FORMAT_MSG = stringConfig.getString("BusinessMessages.BadDOTFormatFromSlack")
    val DOT_STRING_TOO_LONG_MSG = stringConfig.getString("BusinessMessages.BadDOTLengthFromSlack")
    val SLACK_EXPECTED_TOKEN = ConfigFactory.load().getString("SLACK_AUTHENTICATION_TOKEN")
    val MAXIMUM_STRING_LENGTH = applicationConfig.getInt("MAXIMUM_DOT_STRING_LENGTH_NOT_INCLUDING_NEW_LINES")
    val UNEXPECTED_SYSTEM_ERROR = stringConfig.getString("SystemMessages.UnexpectedSystemError")
    val TEMPORARY_GRAPH_FILE_DIRECTORY = applicationConfig.getString("TEMPORARY_GRAPH_FILE_DIRECTORY")
      .replaceAll("/", File.separator)

    def createGraphvizDotStringAndReturnImgurLink = Action.async{ implicit request =>
        import SlashCommandIn._

        slackForm.bindFromRequest.fold(
            formWithErrors => {
                Logger.warn(s"Incorrect Form Format: ${request.body.asText.getOrElse("<No Body>")}}")
                Future{Ok(Json.toJson(SlackPrivateUserResponse(BAD_FORM_DATA_MSG)))}
            },

            goodValidatedSlackRequest => {
                if(goodValidatedSlackRequest.token.contentEquals(SLACK_EXPECTED_TOKEN)) {
                    Logger.info("Validation Succeeded")
                    _doImageCreationAndGetImgurLink(goodValidatedSlackRequest)
                    Future{Ok(Json.toJson(SlackPrivateUserResponse(PROCESSING_MSG + "\n>>>" +
                      goodValidatedSlackRequest.text)))}

                }else {
                    Logger.warn(s"Incorrect slack token: ${goodValidatedSlackRequest.token}")
                    Future{Ok(Json.toJson(SlackPrivateUserResponse(BAD_TOKEN_MSG)))}
                }
            }
        )
    }

    def _doImageCreationAndGetImgurLink(slackInput: SlashCommandIn): Unit = Future {
        val userTextWithoutNewLines = slackInput.text.replaceAll("\\\\r|\\\\n", " ")

        Logger.info(s"DOT String(whitespace stripped): $userTextWithoutNewLines")
        if(userTextWithoutNewLines.length > MAXIMUM_STRING_LENGTH) {
            sendPrivateResponseToUser(slackInput.response_url, SlackPrivateUserResponse(DOT_STRING_TOO_LONG_MSG))
        }else{
            _createImageAndSendResponseToUser(slackInput, userTextWithoutNewLines)
        }
    }

    def _createImageAndSendResponseToUser(slackInput: SlashCommandIn, userTextWithoutNewLines: String): Unit = {
        val SYSTEM_SUCCESS_CODE = 0
        val userTextAsIS = new ByteArrayInputStream(userTextWithoutNewLines.getBytes("UTF-8"))
        val temporaryFileName = TEMPORARY_GRAPH_FILE_DIRECTORY + System.currentTimeMillis() + ".png"

        val out = (s"dot -Tpng -o $temporaryFileName" #< userTextAsIS).!

        if (out != SYSTEM_SUCCESS_CODE) {
            if(!new File(TEMPORARY_GRAPH_FILE_DIRECTORY).canWrite) {
                Logger.error(s"No permission to write to: $TEMPORARY_GRAPH_FILE_DIRECTORY")
                sendPrivateResponseToUser(slackInput.response_url,
                    SlackPrivateUserResponse(UNEXPECTED_SYSTEM_ERROR))
            }else{
                Logger.warn(s"Non-zero exit code from graphviz, assuming Bad Dot Format: $userTextWithoutNewLines")
                sendPrivateResponseToUser(slackInput.response_url,
                    SlackPrivateUserResponse(BAD_DOT_FORMAT_MSG))
            }
        } else {
            Logger.warn(s"DOT generation successful")
            uploadFileToImgur(temporaryFileName, slackInput).onComplete(tryResponse => {
                _cleanupTemporaryFileSilently(temporaryFileName)

                if(tryResponse.isFailure) {
                    Logger.error("Error From Imgur", tryResponse.failed.get)
                }else{
                    val imgurResponseBody = tryResponse.get.body

                    Logger.info(s"Good imgur response: $imgurResponseBody")

                    Json.parse(imgurResponseBody).validate[ImgurResponse] match {
                        case imgurData: JsSuccess[ImgurResponse] =>  {
                            Logger.info(s"Sending imgur link to user: ${imgurData.get.data.link}")
                            sendChannelResponseToUser(slackInput.response_url,
                                SlackChannelUserResponse(imgurData.get.data.link))
                        }
                        case _ => Logger.error("Unexpected response from Imgur")
                    }
                }
            })
        }
    }

    def sendPrivateResponseToUser(slackResponseUrl: String, slackPrivateUserResponse: SlackPrivateUserResponse): Unit = {
        ws.url(slackResponseUrl)
          .withHeaders("CONTENT-TYPE" -> "application/json")
          .post(Json.toJson(slackPrivateUserResponse))
          .map{ result =>
            if(result.status > 199 && result.status < 300) {
                Logger.info(s"Successfully sent private response to slack: ${result.toString}, body: ${result.body}")
            }else{
                Logger.error(s"Unexpected response when returning private response to slack: ${result.toString}, body: ${result.body}")
            }
        }
    }

    def sendChannelResponseToUser(slackResponseUrl: String, slackChannelUserResponse: SlackChannelUserResponse): Unit = {
        ws.url(slackResponseUrl)
          .withHeaders("CONTENT-TYPE" -> "application/json")
          .post(Json.toJson(slackChannelUserResponse))
          .map{ result =>
            if(result.status > 199 && result.status < 300) {
                Logger.info(s"Successfully sent channel response to slack: ${result.toString}, body: ${result.body}")
            }else{
                Logger.error(s"Unexpected response when returning channel response to slack: ${result.toString}, body: ${result.body}")
            }
        }
    }

    def uploadFileToImgur(temporaryFilename: String,
                          slackInput: SlashCommandIn): Future[WSResponse] = {
        ws.url(IMGUR.UPLOAD_IMAGE_ENDPOINT)
            .withHeaders("AUTHORIZATION" -> s"Client-ID ${IMGUR.CLIENT_ID}")
            .post(Source(
              FilePart("image", temporaryFilename, Option("image/png"), FileIO.fromFile(new File(temporaryFilename)))
                :: List()))
    }

    def _cleanupTemporaryFileSilently(tempFilename: String): Unit = {
        val file = new File(tempFilename)
        if(file.exists() && file.isFile) {
            if(!file.delete()) {
                Logger.error(s"Could not delete temporary file: $tempFilename")
            }
        }
    }
}
