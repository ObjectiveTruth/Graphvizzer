package controllers

import javax.inject.Inject

import akka.actor.ActorSystem
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, Controller}
import slick.driver.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class RegisterController @Inject()(dbConfigProvider: DatabaseConfigProvider)(ws: WSClient)(actorSystem: ActorSystem)
                                  (implicit exec: ExecutionContext)
  extends Controller {
    val dbConfig = dbConfigProvider.get[JdbcProfile]

    def registerTeam = Action.async { implicit request =>
        Future{Ok("hello")}
    }
}
