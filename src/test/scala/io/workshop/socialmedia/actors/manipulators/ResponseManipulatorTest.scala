package io.workshop.socialmedia.actors.manipulators

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import io.workshop.socialmedia.actors.SocialMediaManager.UserMessagesRetrieveFailed
import io.workshop.socialmedia.actors.providers.{ ProviderResponse, UserMessagesRetrieved }
import io.workshop.socialmedia.actors.ManipulationError
import io.workshop.socialmedia.model.twitter.Tweet
import org.mockito.MockitoSugar
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ResponseManipulatorTest extends AnyWordSpec with BeforeAndAfterAll with Matchers with MockitoSugar {
  val testKit: ActorTestKit     = ActorTestKit()
  override def afterAll(): Unit = testKit.shutdownTestKit()

  "Response manipulator" should {
    "manipulate response with upperCase algorithm" in {
      // setup
      val list            = Seq(Tweet("Hi there"), Tweet("How are you?"))
      val manipulatedList = Seq(Tweet("HI THERE!"), Tweet("HOW ARE YOU?!"))
      val manipulatorActor =
        testKit.spawn(ResponseManipulator(text => ManipulateAlgorithm.makeUpperCaseWithExclamationMark(text)))
      val probe = testKit.createTestProbe[ProviderResponse]()

      manipulatorActor ! ManipulateTweets(list, probe.ref)

      probe.expectMessage(UserMessagesRetrieved(manipulatedList))
    }

    "manipulate response with custom algorithm" in {
      // setup
      val list            = Seq(Tweet("Hi there"), Tweet("How are you?"))
      val manipulatedList = Seq(Tweet("Hello there"), Tweet("How are you?"))
      val manipulatorActor =
        testKit.spawn(ResponseManipulator(text => text.replace("Hi", "Hello")))
      val probe = testKit.createTestProbe[ProviderResponse]()

      manipulatorActor ! ManipulateTweets(list, probe.ref)

      probe.expectMessage(UserMessagesRetrieved(manipulatedList))
    }

    "throw error while manipulating" in {
      // setup
      val list  = Seq(Tweet("Hi there"), Tweet("How are you?"))
      val error = new Error("Error failed").getCause
      val manipulatorActor =
        testKit.spawn(ResponseManipulator(text => throw error))
      val probe = testKit.createTestProbe[ProviderResponse]()

      manipulatorActor ! ManipulateTweets(list, probe.ref)

      probe.expectMessage(
        UserMessagesRetrieveFailed(
          ManipulationError("Error occurred while manipulating tweets", Some(error)),
          probe.ref
        )
      )
    }
  }
}
