import org.scalatestplus.play._
import play.api.libs.json.JsString
import play.api.test._
import play.api.test.Helpers._

import scala.util.parsing.json.JSONObject

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
class ApplicationSpec extends PlaySpec with OneAppPerTest {

  "Routes" should {

    "send 404 when the resource is not found" in  {
      route(app, FakeRequest(GET, "/boum")).map(status(_)) mustBe Some(NOT_FOUND)
    }

  }

    "GraphvizCreationController" should {

        "Fail if JSON validation fails" in {
            val badlyFormattedJSON = JsString("Incorrect Format")
            val fakeRequest = FakeRequest(POST, "/createImgurLinkForDOTString")
              .withJsonBody(badlyFormattedJSON)

            val response = route(app, fakeRequest).get

            status(response) mustBe Some(OK)
            contentType(response) mustBe Some("application/json")
            contentAsJson(response) mustBe Some(JsString("Hello"))
    }

  }
}
