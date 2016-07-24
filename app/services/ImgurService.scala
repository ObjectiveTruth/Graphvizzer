package services

import javax.inject._

import play.api.libs.ws.WSClient
import scala.concurrent.ExecutionContext

@Singleton
class ImgurService @Inject() (ws: WSClient) {

    def uploadToImgur() ={

    }
}
