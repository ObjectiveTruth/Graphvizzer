package controllers;

import javax.inject._

import play.api.mvc._
import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class HealthCheckController @Inject() (ws: WSClient)(actorSystem: ActorSystem)(implicit exec: ExecutionContext)
  extends Controller{

    def isAlive =  Action.async {
        Future{Ok("Yup, it's alive:")}
    }

    def code = Action.async {
        val SLACK_EXPECTED_TOKEN = ConfigFactory.load().getString("SLACK_AUTHENTICATION_TOKEN")
        Future{Ok(SLACK_EXPECTED_TOKEN)}
    }
}

