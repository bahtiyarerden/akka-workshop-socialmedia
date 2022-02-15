package io.workshop.socialmedia.model

sealed trait RepositoryException

case class RepositoryNotFound(private val message: String = "", private val cause: Option[Throwable] = None)
    extends RuntimeException(message)
    with RepositoryException {
  cause.foreach(initCause)
}
