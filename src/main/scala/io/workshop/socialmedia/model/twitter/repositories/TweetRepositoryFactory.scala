package io.workshop.socialmedia.model.twitter.repositories

import io.workshop.socialmedia.model.RepositoryNotFound

import scala.concurrent.{ ExecutionContext, Future }

object TweetRepositoryType extends Enumeration {
  type TweetRepositoryType = Value
  val InMemory, Cassandra = Value
}

object TweetRepositoryFactory {

  import TweetRepositoryType._

  def apply(repository: TweetRepositoryType)(implicit ec: ExecutionContext): TweetRepository[Future] =
    repository match {
      case InMemory => new TweetRepositoryInMemory()
      case _        => throw RepositoryNotFound(s"Repository not implemented yet: ${repository}")
    }
}
