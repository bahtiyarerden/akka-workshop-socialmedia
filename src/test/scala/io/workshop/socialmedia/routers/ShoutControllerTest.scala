package io.workshop.socialmedia.routers

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.ValidationRejection
import akka.http.scaladsl.testkit.{ RouteTestTimeout, ScalatestRouteTest }
import akka.testkit.TestDuration
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import io.workshop.socialmedia.routers.stubs.ShoutControllerStub
import org.mockito.MockitoSugar
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.JsArray

import scala.concurrent.duration.DurationInt

class ShoutControllerTest extends AnyWordSpec with Matchers with ScalatestRouteTest with MockitoSugar with ApiRoutes {
  import ApiRoutes._
  import PlayJsonSupport._

  implicit val timeout: RouteTestTimeout = RouteTestTimeout(6.seconds.dilated)

  "Shout controller" should {
    "get 1 tweet" in {
      Get("/shout/bahtiyar?limit=1") ~> apiRoutes ~> check {
        val response: JsArray = responseAs[JsArray]
        response.value should have size 1
      }
    }
    "get 3 tweets" in {
      Get("/shout/bahtiyar?limit=3") ~> apiRoutes ~> check {
        val response: JsArray = responseAs[JsArray]
        response.value should have size 3
      }
    }
    "get 2 tweets from cache" in {
      val firstRequest  = Get("/shout/bahtiyar?limit=2") ~> apiRoutes
      val secondRequest = Get("/shout/bahtiyar?limit=2") ~> apiRoutes

      firstRequest ~> check {
        val firstResponse: JsArray = responseAs[JsArray]
        secondRequest ~> check {
          val secondResponse: JsArray = responseAs[JsArray]
          firstResponse shouldEqual secondResponse
        }
      }
    }
    "handle exception if the desired number of tweets exceeds 10" in {
      Get("/shout/bahtiyar?limit=11") ~> apiRoutes ~> check {
        rejection shouldEqual ValidationRejection("The number of tweets requested must be between 1 and 10")
      }
    }
    "handle exception if the desired number of tweets is below 1" in {
      Get("/shout/bahtiyar?limit=0") ~> apiRoutes ~> check {
        rejection shouldEqual ValidationRejection("The number of tweets requested must be between 1 and 10")
      }
    }
    "handle custom data fetch error exception" in {
      val stubRoute = new ShoutControllerStub().route
      Get("/fetch") ~> stubRoute ~> check {
        status shouldBe StatusCodes.InternalServerError
      }
    }

    "handle custom manipulate error exception" in {
      val stubRoute = new ShoutControllerStub().route
      Get("/manipulate") ~> stubRoute ~> check {
        status shouldBe StatusCodes.InternalServerError
      }
    }
  }
}
