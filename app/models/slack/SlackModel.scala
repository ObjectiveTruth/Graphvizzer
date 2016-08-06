package models.slack

import javax.inject._

import play.api.Logger
import play.api.libs.json.Json
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext

class SlackModel @Inject() (ws: WSClient) (implicit exec: ExecutionContext) {
    def sendPrivateResponseToUser(slackResponseUrl: String, slackPrivateUserResponse: SlackPrivateUserResponse): Unit = {
        ws.url(slackResponseUrl)
            .withHeaders("CONTENT-TYPE" -> "application/json")
            .post(Json.toJson(slackPrivateUserResponse))
            .map{ result =>
                if(result.status > 199 && result.status < 300) {
                    Logger.info(s"Successfully sent private response to slack: ${result.toString}, body: ${result.body}")
                }else{
                    Logger.error(s"Unexpected response when returning private response to slack: ${result.toString}, " +
                        s"body: ${result.body}")
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
                    Logger.error(s"Unexpected response when returning channel response to slack: ${result.toString}, " +
                        s"body: ${result.body}")
                }
            }
    }
}
