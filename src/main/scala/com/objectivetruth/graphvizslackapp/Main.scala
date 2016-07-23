package com.objectivetruth.graphvizslackapp

import java.io.{InputStream, OutputStream}
import java.net.URLEncoder

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.PropertyNamingStrategy.SnakeCaseStrategy
import com.objectivetruth.graphvizslackapp.models._
import com.objectivetruth.graphvizslackapp.models.AppReturns._

import scala.util.{Failure, Success, Try}
import scalaj.http.{Http, HttpResponse}



object Main {
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

                    if(imgurResponse.isCodeInRange(199, 300)) {
                        scalaMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                        val imgurResponseJSON = scalaMapper.readValue(imgurResponse.body, classOf[ImgurResponse])
                        val returnResponse = OK(imgurResponseJSON.data.link)

                        scalaMapper.writeValue(output, returnResponse)



/*                        val sendingBackToSlackUser = Http(command.get.body.responseUrl)
                          .header("Content-type", "application/json")
                          .method("POST")
                          .postData(scalaMapper.writeValueAsString(returnResponse))
                          .asString

                        println(sendingBackToSlackUser)*/
                    }else{
                        val returnBadRequest = BadRequest("BadRequest: Incorrect DOT Formatting")
                        println(returnBadRequest.toString)
                        scalaMapper.writeValue(output, returnBadRequest)
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
    }
}
