package com.objectivetruth.graphvizslackapp.models

import play.api.libs.json.Json

case class ImgurResponse(
                        data: ImgurImageInformation
                        )

sealed case class ImgurImageInformation(link: String)

object ImgurImageInformation {
    implicit val imgurImageInformationWrites = Json.writes[ImgurImageInformation]
}

object ImgurResponse {
    implicit val imgurResponseWrites = Json.writes[ImgurResponse]
}

