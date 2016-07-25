package com.objectivetruth.graphvizslackapp.models

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

case class ImgurResponse(
                        data: ImgurImageInformation
                        )

sealed case class ImgurImageInformation(link: String)

object ImgurImageInformation {
    implicit val imgurImageInformationReads: Reads[ImgurImageInformation] = Json.reads[ImgurImageInformation]
}

object ImgurResponse {
    implicit val imgurResponseReads: Reads[ImgurResponse] = Json.reads[ImgurResponse]
}

