package io.workshop.socialmedia.actors.manipulators

import akka.actor.typed.{ ActorRef, ActorSystem, Behavior }
import akka.actor.typed.scaladsl.Behaviors
import akka.stream.scaladsl.{ Flow, Keep, Sink, Source }
import akka.NotUsed
import io.workshop.socialmedia.actors.{ ActorException, ManipulationError }
import io.workshop.socialmedia.actors.SocialMediaManager.UserMessagesRetrieveFailed
import io.workshop.socialmedia.actors.providers.{ ProviderResponse, UserMessagesRetrieved }
import io.workshop.socialmedia.model.twitter.Tweet

import scala.concurrent.Future
import scala.util.{ Failure, Success }

sealed trait ManipulatorCommand

sealed trait ManipulatorResponse

final case class ManipulateTweets(result: Seq[Tweet], replyTo: ActorRef[ProviderResponse]) extends ManipulatorCommand

private final case class SelfManipulate(message: ManipulatorResponse, replyTo: ActorRef[ProviderResponse])
    extends ManipulatorCommand

final case class ManipulateSuccess(tweets: Seq[Tweet]) extends ManipulatorResponse

final case class ManipulateFailure(error: ActorException) extends ManipulatorResponse

object ResponseManipulator {

  def apply(algorithm: String => String): Behavior[ManipulatorCommand] =
    Behaviors.setup { context =>
      implicit val system: ActorSystem[Nothing] = context.system

      Behaviors.receiveMessage {
        case ManipulateTweets(result, replyTo) =>
          context.log.trace("messages manipulating with given algorithm")

          val flow                           = Flow[Tweet].map(m => Tweet(algorithm(m.text)))
          val source: Source[Tweet, NotUsed] = Source(result)
          val sumSink: Sink[Tweet, Future[Seq[Tweet]]] = Sink.fold[Seq[Tweet], Tweet](Nil) { case (list, element) =>
            list.:+(element)
          }
          val manipulatedResult: Future[Seq[Tweet]] =
            source.via(flow).toMat(sumSink)(Keep.right).run()

          context.pipeToSelf(manipulatedResult) {
            case Success(value) =>
              SelfManipulate(ManipulateSuccess(value), replyTo)
            case Failure(exception) =>
              val failure = ManipulationError("Error occurred while manipulating tweets", Some(exception.getCause))
              SelfManipulate(ManipulateFailure(failure), replyTo)
          }
          Behaviors.same

        case SelfManipulate(message, replyTo) =>
          message match {
            case ManipulateSuccess(value) =>
              context.log.debug("messages manipulated and returned to the requester")
              replyTo ! UserMessagesRetrieved(value)
            case ManipulateFailure(err) =>
              context.log.error("error occurred while manipulating messages")
              replyTo ! UserMessagesRetrieveFailed(err, replyTo)
          }
          Behaviors.stopped
      }
    }
}
