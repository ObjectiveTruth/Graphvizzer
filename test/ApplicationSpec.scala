import java.io.File

import com.typesafe.config.ConfigFactory
import models.{SlackChannelUserResponse, SlackPrivateUserResponse}
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
    val config = ConfigFactory.load("strings.conf")

  "Routes" should {

    "send 404 when the resource is not found" in  {
      route(app, FakeRequest(GET, "/boum")).map(status) mustBe Some(NOT_FOUND)
    }

  }

    "GraphvizCreationController" should {

        "Fail if form data received, fails validation" in {
            val BAD_FORM_DATA_MSG = config.getString("ErrorMessage.BadFormDataFromSlack")

            val fakeRequest = FakeRequest(POST, "/createImgurLinkForDOTString")
                .withFormUrlEncodedBody(("hello", "yes"))

            val response = route(app, fakeRequest).get

            status(response) mustBe OK
            contentType(response) mustBe Some("application/json")
            contentAsString(response) mustBe Json.toJson(SlackPrivateUserResponse(BAD_FORM_DATA_MSG)).toString()
        }

        "Fail if token received doesn't match expected" in {
            val BAD_TOKEN_MSG = config.getString("ErrorMessage.BadTokenFromSlack")

            val fakeRequest = FakeRequest(POST, "/createImgurLinkForDOTString")
                  .withHeaders("CONTENT-TYPE" -> "application/x-www-form-urlencoded")
                  .withTextBody(getStringFromResourcePath(s"slack${/}BadSlackToken.txt"))

            val response = route(app, fakeRequest).get

            status(response) mustBe OK
            contentType(response) mustBe Some("application/json")
            contentAsString(response) mustBe Json.toJson(SlackPrivateUserResponse(BAD_TOKEN_MSG)).toString()
        }
        "Succeed if request is correct" in {
            val fakeRequest = FakeRequest(POST, "/createImgurLinkForDOTString")
              .withHeaders("CONTENT-TYPE" -> "application/x-www-form-urlencoded")
              .withTextBody(getStringFromResourcePath(s"slack${/}GoodRequest.txt"))

            val response = route(app, fakeRequest).get

            status(response) mustBe OK
            contentType(response) mustBe Some("application/json")
            contentAsString(response) mustBe Json.toJson(SlackChannelUserResponse("http://imgur.com/1234")).toString()
        }
  }

    def getStringFromResourcePath(relativeResourceFilePath: String): String = {
        val stringStream = getClass.getResourceAsStream(relativeResourceFilePath)
        Source.fromInputStream(stringStream).mkString
    }
}
