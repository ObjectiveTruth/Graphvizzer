import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._


/**
  * To use this simply type `activator gatling:test`. You should take a look at the base URL value and the
  * atOnceUsers
  */
class GraphCreationLoadTest extends Simulation {
    val httpConf = http
        .baseURL("http://localhost:9001")

    val scn = scenario("BasicSimulation")
        .exec(http("request_1")
            .post("/createImgurLinkForDOTString")
                .body(RawFileBody("test/resources/slack/GoodRequest.txt"))
                .asFormUrlEncoded)

    setUp(
        scn.inject(atOnceUsers(5))
    ).protocols(httpConf)
}

