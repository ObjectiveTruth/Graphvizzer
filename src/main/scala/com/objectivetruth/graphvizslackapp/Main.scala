package com.objectivetruth.graphvizslackapp

import java.io.{InputStream, OutputStream}
import java.net.URLEncoder

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.PropertyNamingStrategy.SnakeCaseStrategy
import com.objectivetruth.graphvizslackapp.models._
import com.objectivetruth.graphvizslackapp.models.AppReturns._
import com.amazonaws.services.lambda.runtime.Context

import scala.util.{Failure, Success, Try}
import scalaj.http.{Http, HttpResponse}



object Main {
    val SLACK_TOKEN = "sf0Rq4MMxUUSnTK29cknMRHI"
    val CLIENT_ID = "34b1e110bd71758"
    val IMGUR_API_UPLOAD_ENDPOINT = "https://api.imgur.com/3/image"

    var scalaMapper = {
        import com.fasterxml.jackson.databind.ObjectMapper
        import com.fasterxml.jackson.module.scala.DefaultScalaModule
        val mapper = new ObjectMapper().registerModule(new DefaultScalaModule)
        mapper.setPropertyNamingStrategy(new SnakeCaseStrategy())
        mapper
    }
    def receiver(input: InputStream, output: OutputStream, context: Context): Unit = {
        println("Receiving raw input...")
        println(input.toString)
        val slashCommand = Try(scalaMapper.readValue(input, classOf[SlashCommandIn]))
        println(s"JSON Parse Complete: ${slashCommand.toString}")


        slashCommand match{
            case command:Success[SlashCommandIn] => {
                if(command.get.token.contentEquals(SLACK_TOKEN)) {
                    val URLEncodedDotStringFromUser = URLEncoder.encode(command.get.text.replaceAll("\\n", ""), "UTF-8")
                    val googleAPIsURL = s"https://chart.googleapis.com/chart?chl=$URLEncodedDotStringFromUser&cht=gv"
                    println(s"Text from user Parsed, will send this: $googleAPIsURL")

                    val imgurResponse: HttpResponse[String] = Http(IMGUR_API_UPLOAD_ENDPOINT)
                      .header("AUTHORIZATION", s"Client-ID ${CLIENT_ID}")
                      .method("POST")
                      .param("image", googleAPIsURL)
                      .asString

                    println(s"ImgurResponse: $imgurResponse")

                    if(imgurResponse.isCodeInRange(199, 300)) {
                        scalaMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                        val imgurResponseJSON = scalaMapper.readValue(imgurResponse.body, classOf[ImgurResponse])
                        val returnResponse = OK(imgurResponseJSON.data.link)

                        scalaMapper.writeValue(output, returnResponse)
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
