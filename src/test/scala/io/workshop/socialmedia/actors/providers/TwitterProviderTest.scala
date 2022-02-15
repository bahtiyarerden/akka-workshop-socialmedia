package io.workshop.socialmedia.actors.providers

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.scaladsl.Behaviors
import io.workshop.socialmedia.actors.SocialMediaManager.UserMessagesRetrieveFailed
import io.workshop.socialmedia.actors.manipulators.{ ManipulateTweets, ManipulatorCommand }
import io.workshop.socialmedia.actors.DataFetchError
import io.workshop.socialmedia.model.twitter.repositories.TweetRepositoryInMemory
import io.workshop.socialmedia.model.twitter.Tweet
import org.mockito.ArgumentMatchersSugar.any
import org.mockito.MockitoSugar
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.Future

class TwitterProviderTest extends AnyWordSpec with BeforeAndAfterAll with Matchers with MockitoSugar {
  val testKit: ActorTestKit     = ActorTestKit()
  override def afterAll(): Unit = testKit.shutdownTestKit()

  def mockBehavior(): Behaviors.Receive[ManipulatorCommand] =
    Behaviors.receive { (_, message) =>
      message match {
        case ManipulateTweets(result, replyTo) =>
          replyTo ! UserMessagesRetrieved(result)
          Behaviors.same
        case _ =>
          Behaviors.stopped
      }
    }

  "Twitter provider" should {
    "provide user messages" in {
      // setup
      val tweetList = Seq(Tweet("Hi there"), Tweet("How are you?"))

      val mockRepository = mock[TweetRepositoryInMemory]
      when(mockRepository.searchByUserName(any[String], any[Int])).thenReturn(Future.successful(tweetList))

      val twitterActor     = testKit.spawn(TwitterProvider(mockRepository))
      val manipulatorActor = testKit.spawn(mockBehavior())

      val guardianProbe = testKit.createTestProbe[ProviderResponse]()

      // execute
      twitterActor ! RetrieveUserMessages("bahtiyar", 2, manipulatorActor, guardianProbe.ref)

      // assert
      guardianProbe.expectMessage(UserMessagesRetrieved(messages = tweetList))
    }

    "handle data fetch exception" in {
      // setup
      val exception       = new Exception("failed").getCause
      val mockedException = mock[Exception]
      when(mockedException.getCause).thenReturn(exception)

      val mockRepository = mock[TweetRepositoryInMemory]
      when(mockRepository.searchByUserName(any[String], any[Int]))
        .thenReturn(Future.failed(mockedException))

      val twitterActor     = testKit.spawn(TwitterProvider(mockRepository))
      val probe            = testKit.createTestProbe[ProviderResponse]()
      val manipulatorProbe = testKit.createTestProbe[ManipulatorCommand]()

      // execute
      twitterActor ! RetrieveUserMessages("bahtiyar", 2, manipulatorProbe.ref, probe.ref)

      // assert
      probe.expectMessage(
        UserMessagesRetrieveFailed(
          DataFetchError("An exception occurred while fetching data", Some(exception)),
          probe.ref
        )
      )
    }
  }
}
