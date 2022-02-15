package io.workshop.socialmedia.model.twitter.repositories

import io.workshop.socialmedia.model.twitter.Tweet

trait TweetRepository[P[_]] {
  def searchByUserName(username: String, limit: Int): P[Seq[Tweet]]
}
