package models.imgur

import java.io.{ByteArrayInputStream, File}
import javax.inject._

import akka.stream.scaladsl.{FileIO, Source}

import scala.sys.process._
import com.typesafe.config.ConfigFactory
import common.constants.IMGUR
import models.slack.{SlackChannelUserResponse, SlackModel, SlackPrivateUserResponse, SlashCommandIn}
import play.api.Logger
import play.api.libs.json.{JsSuccess, Json}
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.mvc.MultipartFormData.FilePart

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ImgurModel @Inject() (slackModel: SlackModel)(ws: WSClient) (implicit exec: ExecutionContext) {
    val applicationConfig = ConfigFactory.load()
    val MAXIMUM_STRING_LENGTH = applicationConfig.getInt("MAXIMUM_DOT_STRING_LENGTH_NOT_INCLUDING_NEW_LINES")
    val TEMPORARY_GRAPH_FILE_DIRECTORY = applicationConfig.getString("TEMPORARY_GRAPH_FILE_DIRECTORY")
        .replaceAll("/", File.separator)

    val stringConfig = ConfigFactory.load("strings.conf")
    val UNEXPECTED_SYSTEM_ERROR = stringConfig.getString("SystemMessages.UnexpectedSystemError")
    val BAD_DOT_FORMAT_MSG = stringConfig.getString("BusinessMessages.BadDOTFormatFromSlack")
    val DOT_STRING_TOO_LONG_MSG = stringConfig.getString("BusinessMessages.BadDOTLengthFromSlack")


    def doGraphCreationAndRespondToUserAsynchronously(slackInput: SlashCommandIn): Unit = Future {
        val userTextWithoutNewLines = slackInput.text.replaceAll("\\\\r|\\\\n", " ")

        Logger.info(s"DOT String(whitespace stripped): $userTextWithoutNewLines")
        if(userTextWithoutNewLines.length > MAXIMUM_STRING_LENGTH) {
            slackModel
                .sendPrivateResponseToUser(slackInput.response_url, SlackPrivateUserResponse(DOT_STRING_TOO_LONG_MSG))
        }else{
            _createImageAndSendResponseToUser(slackInput, userTextWithoutNewLines)
        }
    }

    private def _createImageAndSendResponseToUser(slackInput: SlashCommandIn, userTextWithoutNewLines: String): Unit = {
        val SYSTEM_SUCCESS_CODE = 0
        val userTextAsIS = new ByteArrayInputStream(userTextWithoutNewLines.getBytes("UTF-8"))
        val temporaryFileName = TEMPORARY_GRAPH_FILE_DIRECTORY + System.currentTimeMillis() + ".png"

        val out = (s"dot -Tpng -o $temporaryFileName" #< userTextAsIS).!

        if (out != SYSTEM_SUCCESS_CODE) {
            if(!new File(TEMPORARY_GRAPH_FILE_DIRECTORY).canWrite) {
                Logger.error(s"No permission to write to: $TEMPORARY_GRAPH_FILE_DIRECTORY")
                slackModel.sendPrivateResponseToUser(slackInput.response_url,
                    SlackPrivateUserResponse(UNEXPECTED_SYSTEM_ERROR))
            }else{
                Logger.warn(s"Non-zero exit code from graphviz, assuming Bad Dot Format: $userTextWithoutNewLines")
                slackModel.sendPrivateResponseToUser(slackInput.response_url,
                    SlackPrivateUserResponse(BAD_DOT_FORMAT_MSG))
            }
        } else {
            Logger.info(s"DOT generation successful")
            _uploadFileToImgur(temporaryFileName, slackInput)
                .onComplete(tryResponse => {
                    _cleanupTemporaryFileSilently(temporaryFileName)

                    if (tryResponse.isFailure) {
                        Logger.error("Error From Imgur", tryResponse.failed.get)
                    } else {
                        val imgurResponseBody = tryResponse.get.body

                        Logger.info(s"Good imgur response: $imgurResponseBody")

                        Json.parse(imgurResponseBody).validate[ImgurResponse] match {
                            case imgurData: JsSuccess[ImgurResponse] => {
                                val slackChannelMessage = SlackChannelUserResponse(imgurData.get.data.link)
                                Logger.info(s"Sending imgur link to user: ${Json.toJson(slackChannelMessage).toString()}")
                                slackModel.sendChannelResponseToUser(slackInput.response_url, slackChannelMessage)
                            }
                            case _ => Logger.error("Unexpected response from Imgur")
                        }
                    }
                })
        }
    }

    private def _uploadFileToImgur(temporaryFilename: String,
                          slackInput: SlashCommandIn): Future[WSResponse] = {
        ws.url(IMGUR.UPLOAD_IMAGE_ENDPOINT)
            .withHeaders("AUTHORIZATION" -> s"Client-ID ${IMGUR.CLIENT_ID}")
            .post(Source(
                FilePart("image", temporaryFilename, Option("image/png"), FileIO.fromFile(new File(temporaryFilename)))
                    :: List()))
    }

    private def _cleanupTemporaryFileSilently(tempFilename: String): Unit = {
        val file = new File(tempFilename)
        if(file.exists() && file.isFile) {
            if(!file.delete()) {
                Logger.error(s"Could not delete temporary file: $tempFilename")
            }
        }
    }
}

