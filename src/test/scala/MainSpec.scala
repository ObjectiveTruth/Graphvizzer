import java.io.{ByteArrayOutputStream, InputStream, OutputStream}

import com.objectivetruth.graphvizslackapp.Main
import com.objectivetruth.graphvizslackapp.models.AppReturns.{BadRequest, OK, Unauthorized}
import org.scalamock.scalatest.MockFactory
import org.scalatest._

class MainSpec extends FlatSpec with Matchers with MockFactory {
/*    "Receiver" should "Return Imgur Url when everything is good" in {
      val testInputStream = TestHelpers.getTestGatewayJSON
      val testOutputStream = new ByteArrayOutputStream(64)

      Main.receiver(testInputStream, testOutputStream)
      val returnFromReceiver = testOutputStream.toString

      println(returnFromReceiver)
      val imgurResponseJSON = Main.scalaMapper.readValue(returnFromReceiver, classOf[OK])

      imgurResponseJSON.text should include ("http://i.imgur.com/")
    }*/

/*    "Receiver" should "Return Bad Request when bad DOT format" in {
        val testInputStream = TestHelpers.getTestGatewayJSONBadRequest
        val testOutputStream = new ByteArrayOutputStream(64)

        Main.receiver(testInputStream, testOutputStream)
        val returnFromReceiver = testOutputStream.toString

        println(returnFromReceiver)
        val imgurResponseJSON = Main.scalaMapper.readValue(returnFromReceiver, classOf[BadRequest])

        imgurResponseJSON.text should equal  ("BadRequest: Incorrect DOT Formatting")
    }*/

    "Receiver" should "Return Bad Request when bad JSON format" in {
        val testInputStream = TestHelpers.getTestGatewayJSONBadJsonFormat
        val testOutputStream = new ByteArrayOutputStream(64)

        Main.receiver(testInputStream, testOutputStream)
        val returnFromReceiver = testOutputStream.toString

        println(returnFromReceiver)
        val imgurResponseJSON = Main.scalaMapper.readValue(returnFromReceiver, classOf[BadRequest])

        imgurResponseJSON.text should equal  ("BadRequest: Error while parsing JSON from Slack")
    }

    "Receiver" should "Return Unauthorized when incorrect Slack Token" in {
        val testInputStream = TestHelpers.getTestGatewayJSONBadSlackToken
        val testOutputStream = new ByteArrayOutputStream(64)

        Main.receiver(testInputStream, testOutputStream)
        val returnFromReceiver = testOutputStream.toString

        println(returnFromReceiver)
        val imgurResponseJSON = Main.scalaMapper.readValue(returnFromReceiver, classOf[Unauthorized])

        imgurResponseJSON.text should equal  ("Unauthorized: Wrong slack token")
    }
}

