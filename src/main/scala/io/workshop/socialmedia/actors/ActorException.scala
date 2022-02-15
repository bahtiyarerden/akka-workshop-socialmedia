package io.workshop.socialmedia.actors

sealed trait ActorException extends Throwable

case class DataFetchError(private val message: String = "", private val cause: Option[Throwable] = None)
    extends RuntimeException(message)
    with ActorException {
  cause.foreach(initCause)
}

case class ManipulationError(private val message: String = "", private val cause: Option[Throwable] = None)
    extends RuntimeException(message)
    with ActorException {
  cause.foreach(initCause)
}
