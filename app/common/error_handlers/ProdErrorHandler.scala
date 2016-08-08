package common.error_handlers

import javax.inject._

import play.api.http.{DefaultHttpErrorHandler, HttpErrorHandler}
import play.api._
import play.api.mvc._
import play.api.mvc.Results._
import play.api.routing.Router

import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class ProdErrorHandler extends HttpErrorHandler {

    def onClientError(request: RequestHeader, statusCode: Int, message: String) = {
        Future.successful(
            Status(statusCode)("A client error occurred: " + message)
        )
    }

    def onServerError(request: RequestHeader, exception: Throwable) = {
        Future.successful(
            InternalServerError("A server error occurred: " + exception.getMessage)
        )
    }
}
