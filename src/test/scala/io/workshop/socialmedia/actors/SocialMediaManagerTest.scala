package io.workshop.socialmedia.actors

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import io.workshop.socialmedia.actors.SocialMediaManager._
import io.workshop.socialmedia.actors.providers.{ ProviderResponse, Providers, UserMessagesRetrieved }
import org.mockito.MockitoSugar
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class SocialMediaManagerTest extends AnyWordSpec with BeforeAndAfterAll with Matchers with MockitoSugar {
  val testKit: ActorTestKit     = ActorTestKit()
  override def afterAll(): Unit = testKit.shutdownTestKit()

  "Social media manager" should {

    "create twitter provider" in {
      // setup
      val guardian = testKit.spawn(SocialMediaManager())
      val probe    = testKit.createTestProbe[ProviderCreated]()

      // execute
      guardian ! CreateProvider(Providers.Twitter, probe.ref)

      // assert
      probe.expectMessageType[TwitterProviderCreated]
    }

    "get messages from provider" in {
      // setup
      val guardian      = testKit.spawn(SocialMediaManager())
      val probe         = testKit.createTestProbe[ProviderCreated]()
      val responseProbe = testKit.createTestProbe[ProviderResponse]()
      guardian ! CreateProvider(Providers.Twitter, probe.ref)
      val message: TwitterProviderCreated = probe.receiveMessage().asInstanceOf[TwitterProviderCreated]

      // execute
      guardian ! GetMessages("bahtiyar", 3, message.provider, responseProbe.ref)

      // assert
      val response = responseProbe.receiveMessage().asInstanceOf[UserMessagesRetrieved]

      response.messages should have size 3
      response.messages.head.text.last should equal('!')
    }

    "handle repository exceptions" in {
      val exception = new Exception("Error occurred")
      val guardian  = testKit.spawn(SocialMediaManager())
      val probe     = testKit.createTestProbe[ProviderResponse]()

      guardian ! UserMessagesRetrieveFailed(DataFetchError(exception.getMessage, Some(exception.getCause)), probe.ref)

      probe.expectMessage(RequestFailed(DataFetchError(exception.getMessage, Some(exception.getCause))))
    }

    "handle manipulating exceptions" in {
      val exception = new Exception("Error occurred")
      val guardian  = testKit.spawn(SocialMediaManager())
      val probe     = testKit.createTestProbe[ProviderResponse]()

      guardian ! UserMessagesRetrieveFailed(
        ManipulationError(exception.getMessage, Some(exception.getCause)),
        probe.ref
      )

      probe.expectMessage(RequestFailed(ManipulationError(exception.getMessage, Some(exception.getCause))))
    }
  }

}
