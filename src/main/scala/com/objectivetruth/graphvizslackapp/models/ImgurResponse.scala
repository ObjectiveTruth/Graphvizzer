package com.objectivetruth.graphvizslackapp.models

case class ImgurResponse(
                        data: ImgurImageInformation
                        )

sealed case class ImgurImageInformation(link: String)

