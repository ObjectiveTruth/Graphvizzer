import java.io.File

import com.typesafe.config.ConfigFactory
import models.SlackPrivateUserResponse
import org.scalatestplus.play._
import play.api.libs.json.{JsString, Json}
import play.api.test.Helpers._
import play.api.test._

import scala.io.Source

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
class ApplicationSpec extends PlaySpec with OneAppPerTest {
    val / = File.separator
    def getStringFromResourcePath(relativeResourceFilePath: String): String = {
        val stringStream = getClass.getResourceAsStream(relativeResourceFilePath)
        Source.fromInputStream(stringStream).mkString
    }

  "Routes" should {

    "send 404 when the resource is not found" in  {
      route(app, FakeRequest(GET, "/boum")).map(status(_)) mustBe Some(NOT_FOUND)
    }

  }

    "GraphvizCreationController" should {

        "Fail if JSON validation fails" in {
            val BAD_FORM_DATA_MSG = ConfigFactory.load("strings.conf")
              .getString("ErrorMessage.BadFormDataFromSlack")

            val fakeRequest = FakeRequest(POST, "/createImgurLinkForDOTString")
                .withFormUrlEncodedBody(("hello", "yes"))

            val response = route(app, fakeRequest).get

            status(response) mustBe OK
            contentType(response) mustBe Some("application/json")
            contentAsString(response) mustBe Json.toJson(SlackPrivateUserResponse(BAD_FORM_DATA_MSG)).toString()
    }
  }
}
