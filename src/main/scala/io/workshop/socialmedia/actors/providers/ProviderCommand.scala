package io.workshop.socialmedia.actors.providers

import akka.actor.typed.ActorRef
import io.workshop.socialmedia.actors.manipulators.ManipulatorCommand
import io.workshop.socialmedia.model.Model

trait ProviderCommand

trait ProviderResponse

final case class RetrieveUserMessages(
  username: String,
  limit: Int,
  forwardTo: ActorRef[ManipulatorCommand],
  replyTo: ActorRef[ProviderResponse]
) extends ProviderCommand

final case class UserMessagesRetrieved(messages: Seq[Model]) extends ProviderResponse
