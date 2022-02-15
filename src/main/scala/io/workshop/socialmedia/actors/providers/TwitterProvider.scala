package io.workshop.socialmedia.actors.providers

import akka.actor.typed.{ ActorRef, Behavior }
import akka.actor.typed.scaladsl.Behaviors
import io.workshop.socialmedia.actors.{ ActorException, DataFetchError }
import io.workshop.socialmedia.actors.SocialMediaManager.UserMessagesRetrieveFailed
import io.workshop.socialmedia.actors.manipulators.{ ManipulateTweets, ManipulatorCommand }
import io.workshop.socialmedia.model.twitter.repositories.TweetRepository
import io.workshop.socialmedia.model.twitter.Tweet

import scala.concurrent.Future
import scala.util.{ Failure, Success }

object TwitterProvider {

  sealed trait TwitterProviderCommand extends ProviderCommand
  sealed trait TwitterProviderResponse

  private final case class SelfMessageResult(
    result: TwitterProviderResponse,
    forwardTo: ActorRef[ManipulatorCommand],
    replyTo: ActorRef[ProviderResponse]
  ) extends TwitterProviderCommand

  final case class FetchSuccess(tweets: Seq[Tweet]) extends TwitterProviderResponse

  final case class FetchFailure(error: ActorException) extends TwitterProviderResponse

  def apply(repository: TweetRepository[Future]): Behavior[ProviderCommand] =
    Behaviors.receive { (context, message) =>
      message match {
        case RetrieveUserMessages(username, limit, forwardTo, replyTo) =>
          context.log.trace(s"retrieve messages for user: $username with limit: $limit")
          val futureMessages = repository.searchByUserName(username, limit)
          context.pipeToSelf(futureMessages) {
            case Success(value) =>
              SelfMessageResult(FetchSuccess(value), forwardTo, replyTo)
            case Failure(exception) =>
              val error = DataFetchError("An exception occurred while fetching data", Some(exception.getCause))
              SelfMessageResult(FetchFailure(error), forwardTo, replyTo)
          }
          Behaviors.same
        case SelfMessageResult(result, forwardTo, replyTo) =>
          result match {
            case FetchSuccess(result) =>
              context.log.trace("messages retrieved forwarding to manipulator to manipulate it")
              forwardTo ! ManipulateTweets(result, replyTo)
            case FetchFailure(error) =>
              context.log.error("error occurred while getting messages")
              replyTo ! UserMessagesRetrieveFailed(error, replyTo)
          }
          Behaviors.stopped
      }
    }
}
