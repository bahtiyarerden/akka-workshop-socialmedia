package io.workshop.socialmedia.actors

import akka.actor.typed.{ ActorRef, Behavior }
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.Status.Failure
import com.typesafe.config.ConfigFactory
import io.workshop.socialmedia.actors.manipulators.{ ManipulateAlgorithm, ResponseManipulator }
import io.workshop.socialmedia.actors.providers.{
  ProviderCommand,
  ProviderResponse,
  RetrieveUserMessages,
  TwitterProvider
}
import io.workshop.socialmedia.actors.providers.Providers.{ Providers, Twitter }
import io.workshop.socialmedia.model.twitter.repositories.{ TweetRepositoryFactory, TweetRepositoryType }

object SocialMediaManager {

  sealed trait Command
  sealed trait ProviderCreated

  final case class CreateProvider(provider: Providers, replyTo: ActorRef[ProviderCreated]) extends Command

  final case class TwitterProviderCreated(provider: ActorRef[ProviderCommand]) extends ProviderCreated
  final case class UserMessagesRetrieveFailed(error: ActorException, replyTo: ActorRef[ProviderResponse])
      extends ProviderResponse
      with Command

  final case class RequestFailed(error: ActorException) extends ProviderResponse

  final case class GetMessages(
    username: String,
    limit: Int,
    provider: ActorRef[ProviderCommand],
    replyTo: ActorRef[ProviderResponse]
  ) extends Command

  def apply(): Behavior[Command] =
    Behaviors.setup { context =>
      val repositoryName = ConfigFactory.load().getString("repository.twitter.name")
      val repositoryType = TweetRepositoryType.withName(repositoryName)
      val repository     = TweetRepositoryFactory.apply(repositoryType)(context.executionContext)

      Behaviors.receiveMessage {
        case CreateProvider(provider, replyTo) =>
          provider match {
            case Twitter =>
              val twitterActor = context.spawnAnonymous(TwitterProvider(repository))
              replyTo ! TwitterProviderCreated(twitterActor)
              context.log.debug(s"provider created: ${twitterActor.path.name}")
              Behaviors.same
          }
        case GetMessages(username, limit, provider, replyTo) =>
          context.log.debug(s"get messages from ${provider.path.name}")
          val manipulator =
            context.spawnAnonymous(
              ResponseManipulator((text: String) => ManipulateAlgorithm.makeUpperCaseWithExclamationMark(text))
            )
          provider ! RetrieveUserMessages(username, limit, manipulator, replyTo)
          Behaviors.same
        case UserMessagesRetrieveFailed(error, replyTo) =>
          replyTo ! RequestFailed(error)
          Behaviors.same
      }
    }
}
