package common.filters.AccessLogAndTimerFilter

import javax.inject.Inject

import akka.stream.Materializer
import play.api.mvc.{Filter, RequestHeader, Result}
import play.api.Logger
import play.api.routing.Router.Tags

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.util.Try

class AccessLogAndTimerFilter @Inject() (implicit val mat: Materializer) extends Filter{
    def apply(nextFilter: RequestHeader => Future[Result])
             (requestHeader: RequestHeader): Future[Result] = {

        val startTime = System.currentTimeMillis

        nextFilter(requestHeader).map { result =>

            val action = Try{
                requestHeader.tags(Tags.RouteController) +
                "." + requestHeader.tags(Tags.RouteActionMethod)
            }.getOrElse("<No Controller maps to path>")

            val endTime = System.currentTimeMillis
            val requestTime = endTime - startTime

            Logger.info(
                s"ACCESS: ${requestHeader.method} ${requestHeader.path}, " +
                s"Invoked: $action, " +
                s"Took: ${requestTime}ms, " +
                s"Returned: ${result.header.status}")

            result
        }
    }
}
