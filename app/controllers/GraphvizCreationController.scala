package controllers

import javax.inject._
import akka.actor.ActorSystem
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future, Promise}

@Singleton
class GraphvizCreationController @Inject()(actorSystem: ActorSystem)(implicit exec: ExecutionContext)
  extends Controller {

    def createGraphvizDotStringAndReturnImgurLink = Action.async{
        val futureThatAlwaysSuceeds: Future[Result] = Future{
            Ok("Hello World")
        }
        futureThatAlwaysSuceeds
    }
/*    object Main {
        val CLIENT_ID = "34b1e110bd71758"
        val IMGUR_API_UPLOAD_ENDPOINT = "https://api.imgur.com/3/image"

        var scalaMapper = {
            import com.fasterxml.jackson.databind.ObjectMapper
            import com.fasterxml.jackson.module.scala.DefaultScalaModule
            val mapper = new ObjectMapper().registerModule(new DefaultScalaModule)
            mapper.setPropertyNamingStrategy(new SnakeCaseStrategy())
            mapper
        }
        def receiver(input: InputStream, output: OutputStream): Unit = {
            println("Receiving raw input...")
            println(input.toString)
            val awsGatewayInput = Try(scalaMapper.readValue(input, classOf[AWSGatewayInput]))
            println(s"JSON Parse Complete: ${awsGatewayInput.toString}")


            awsGatewayInput match{
                case command:Success[AWSGatewayInput] => {
                    if(command.get.body.token.contentEquals(command.get.officialSlackToken)) {
                        output.close()

                        val userTextWithoutNewLines = command.get.body.text.replaceAll("\r|\n", " ")
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
                        }
                    }else {
                        val returnUnAuthorized = Unauthorized("Unauthorized: Wrong slack token")
                        println(returnUnAuthorized.toString)
                        scalaMapper.writeValue(output, returnUnAuthorized)
                    }

                }
                case Failure(e) => {
                    e.printStackTrace()
                    scalaMapper.writeValue(output, BadRequest("BadRequest: Error while parsing JSON from Slack"))
                }
            }
        }*/
}
